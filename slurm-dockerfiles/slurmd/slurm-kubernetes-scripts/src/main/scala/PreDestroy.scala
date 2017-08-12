import java.net.InetAddress

import http_client.HttpClient
import play.api.libs.json.{Json, _}

import scala.io.Source
import scala.util.{Failure, Success, Try}

object PreDestroy {

  import scala.concurrent.ExecutionContext.Implicits._

  def apply(): Unit = {

    val client = HttpClient()

    val namespace: String = Try({
      Source.fromFile("/var/run/secrets/kubernetes.io/serviceaccount/namespace").mkString
    }).getOrElse("default")

    val responseFuture = client.url(s"http://localhost:8001/api/v1/namespaces/$namespace/configmaps/slurm-config")
      .addQueryStringParameters("export" -> "true")
      .get()


    val newLines = responseFuture map { response =>

      if (response.status == 200) {
        val body = response.body
        val jsonBody = Json.parse(body)
        val jsonTransformer = (__ \ "metadata" \ "annotations").json.prune
        val prunedBody = jsonBody.transform(jsonTransformer)
        val fileLines = Json.stringify(prunedBody.asInstanceOf[JsSuccess[JsValue]].value).split("\\\\n").toVector

        Success(removePartitionName(removeNodeName(fileLines)))
      } else {
        Failure(new Exception("Resource not found"))
      }

    }

    val postResponse = newLines.flatMap {
      case Success(lines) =>
        client.url(s"http://localhost:8001/api/v1/namespaces/$namespace/configmaps/slurm-config")
          .addHttpHeaders("Content-Type" -> "application/merge-patch+json")
          .patch(lines.mkString("\\n"))
    }.andThen { case _ => client.close() }.andThen { case _ => HttpClient.terminate() }

    postResponse onComplete {
      case Failure(f) =>
        client.close()
        HttpClient.terminate()
        println("Kubernetes API error")
        f.printStackTrace()
      case Success(response) =>
        println(response.status)
    }
  }


  def removeNodeName(lines: Seq[String]): Seq[String] = {
    val hostname = InetAddress.getLocalHost.getCanonicalHostName

    lines.filterNot{ _.trim.startsWith(s"NodeName=$hostname") }
  }

  def removePartitionName(lines: Seq[String]): Seq[String] = {
    val partitionLineIndex = lines.indexWhere { l => l.contains("PartitionName=") }

    val hostname = InetAddress.getLocalHost.getCanonicalHostName
    if (partitionLineIndex == -1) {
      lines
    } else {
      lines.updated(partitionLineIndex, lines(partitionLineIndex).replace(s"$hostname,", ""))
    }
  }
}