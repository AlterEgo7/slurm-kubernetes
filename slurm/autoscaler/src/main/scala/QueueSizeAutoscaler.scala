import java.net.Socket

import http_client.HttpClient
import play.api.libs.json.{Json, _}

import scala.io.Source
import scala.util.{Failure, Success, Try}

object QueueSizeAutoscaler {

  def main(args: Array[String]): Unit = {
    import scala.concurrent.ExecutionContext.Implicits._

    //TODO: Options parser for --min --max

    val namespace: String = Try({
      Source.fromFile("/var/run/secrets/kubernetes.io/serviceaccount/namespace").mkString
    }).getOrElse("default")

    val queueSocket = new Socket("gluster1", 23796)
    val slurmInfo = Json.parse(Source.fromInputStream(queueSocket.getInputStream).mkString)

    val queueSize = (slurmInfo \ "queue_size").get.as[Int]
    val allocNodes = (slurmInfo \ "alloc_nodes").get.as[Int]


    val client = HttpClient()

    val responseFuture = client.url(s"http://gluster1:8001/apis/apps/v1beta1/namespaces/$namespace/statefulsets/slurm")
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
        val newReplicas = if (queueSize > 0) math.min(5, allocNodes + queueSize) else allocNodes + 1
        if (newReplicas != replicas) {
          println(s"Scaling now to $newReplicas")
        }
        val newReplicaBody = Json.obj("spec" -> Json.obj("replicas" -> newReplicas))

        client.url(s"http://gluster1:8001/apis/apps/v1beta1/namespaces/$namespace/statefulsets/slurm")
          .addHttpHeaders("Content-Type" -> "application/merge-patch+json")
          .patch(newReplicaBody.toString())
          .andThen{ case _ => client.close() }
          .andThen{ case _ => HttpClient.terminate() }

      case Failure(ex) =>
        client.close()
        HttpClient.terminate()
        Failure(ex)
    }
  }
}
