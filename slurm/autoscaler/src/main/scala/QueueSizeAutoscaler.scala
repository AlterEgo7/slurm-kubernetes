import java.net.{Socket, UnknownHostException}
import java.time.LocalDateTime
import util.control.Breaks._

import http_client.HttpClient
import play.api.libs.json.{Json, _}
import scopt.OptionParser

import scala.io.Source
import scala.util.{Failure, Success, Try}

object QueueSizeAutoscaler {

  def main(args: Array[String]): Unit = {
    import scala.concurrent.ExecutionContext.Implicits._

    val parser = new OptionParser[Config]("slurm_autoscaler") {
      head("slurm_autoscaler", "0.1")
      opt[Int]("min") required() action { (min, config) => config.copy(min = min) }
      opt[Int]("max") required() action { (max, config) => config.copy(max = max) }
      opt[Double]("period") required() action { (period, config) => config.copy(period = period) }
    }

    parser.parse(args, Config()) match {
      case Some(config) =>
        val namespace: String = Try({
          Source.fromFile("/var/run/secrets/kubernetes.io/serviceaccount/namespace").mkString
        }).getOrElse("default")

        val client = HttpClient()

        while(true) {
          breakable {
            val queueSocket = try {
              new Socket("slurm-master-0.slurm", 22347)
            } catch {
              case _: Exception => break
            }

            val slurmInfo = Json.parse(Source.fromInputStream(queueSocket.getInputStream).mkString)

            val queueSize = (slurmInfo \ "queue_size").get.as[Int]
            val allocNodes = (slurmInfo \ "alloc_nodes").get.as[Int]

            val responseFuture = client.url(s"http://localhost:8001/apis/apps/v1beta1/namespaces/$namespace/statefulsets/slurm")
              .addQueryStringParameters("export" -> "true")
              .get()

            val replicasFuture = responseFuture map { response =>
              if (response.status == 200) {
                val jsonBody = Json.parse(response.body)
                val jsonTransformer = (__ \ "spec" \ "replicas").json.pick
                Success(Json.stringify(jsonBody.transform(jsonTransformer).asInstanceOf[JsSuccess[JsValue]].value).trim.toInt)
              } else {
                Failure(new Exception("Resource not found"))
              }
            }

            replicasFuture map {
              case Success(replicas) =>
                val newReplicas = if ((queueSize + allocNodes) > 0) math.min(config.max, allocNodes + queueSize) else math.max(config.min, allocNodes + 1)
                if (newReplicas != replicas) {
                  println(s"${LocalDateTime.now().toString}: Scaling now to $newReplicas")
                }
                val newReplicaBody = Json.obj("spec" -> Json.obj("replicas" -> newReplicas))

                client.url(s"http://localhost:8001/apis/apps/v1beta1/namespaces/$namespace/statefulsets/slurm")
                  .addHttpHeaders("Content-Type" -> "application/merge-patch+json")
                  .patch(newReplicaBody.toString())

              case Failure(ex) =>
                Failure(ex)
            }

            replicasFuture onComplete {
              case Success(_) =>
              case Failure(ex) =>
                System.err.println(s"${LocalDateTime.now().toString}: ERROR (see below)")
                ex.printStackTrace()
            }
          }
          Thread.sleep((config.period * 1000).toLong)
        }


      case None =>
    }

  }

  case class Config(min: Int = 1, max: Int = 3, period: Double = 60) {}

}
