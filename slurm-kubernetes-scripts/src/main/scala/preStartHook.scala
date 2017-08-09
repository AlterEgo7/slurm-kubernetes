import java.io.{BufferedWriter, FileOutputStream, OutputStreamWriter}
import java.net.InetAddress
import play.api.libs.json._

import http_client.HttpClient
import play.api.libs.json.Json

import scala.util.{Failure, Success}

object preStartHook {

  import scala.concurrent.ExecutionContext.Implicits._

  def main(args: Array[String]): Unit = {

    val client = HttpClient()

    val responseFuture = client.url("http://gluster1:8081/api/v1/namespaces/default/configmaps/slurm-config").addQueryStringParameters("export" -> "true").get()


    val newLines = responseFuture map { response =>
      val body = response.body

      val jsonBody = Json.parse(body)
      val jsonTransformer = (__ \ "metadata" \ "annotations").json.prune
      val prunedBody = jsonBody.transform(jsonTransformer)
      val fileLines = Json.stringify(prunedBody.asInstanceOf[JsSuccess[JsValue]].value).split("\\\\n").toVector

      if (fileLines.exists { l => l.contains(InetAddress.getLocalHost.getHostName) }) System.exit(0)

      addPartitionName(addNodeName(fileLines))
    }

    val postResponse = newLines.flatMap{ lines =>
      client.url("http://gluster1:8081/api/v1/namespaces/default/configmaps/slurm-config").addHttpHeaders("Content-Type" -> "application/merge-patch+json")
        .patch(lines.mkString("\\n"))
    }.andThen { case _ => client.close() }.andThen { case _ => HttpClient.terminate() }

    postResponse onComplete{
      case Failure(f) =>
        f.printStackTrace()
        println("Kubernetes API error")
        System.exit(1)
      case Success(response) => println(response.status)
    }
  }


  def addNodeName(lines: Seq[String]): Seq[String] = {
    val lastNodeNameIndex = if (lines.exists { l => l.contains("NodeName=") }) {
      lines.zipWithIndex.filter { case (line, _) => line.trim.startsWith("NodeName=") }.last._2 + 1
    } else {
      lines.size
    }

    val (filePrefix, fileSuffix) = lines.splitAt(lastNodeNameIndex)
    filePrefix ++
      List(s"NodeName=${InetAddress.getLocalHost.getHostName} CPUs=2 SocketsPerBoard=1 CoresPerSocket=2 ThreadsPerCore=1 RealMemory=2000 State=UNKNOWN") ++
      fileSuffix
  }

  def addPartitionName(lines: Seq[String]): Seq[String] = {
    val partitionLineIndex = lines.indexWhere { l => l.contains("PartitionName=") }

    val hostName = InetAddress.getLocalHost.getHostName
    if (partitionLineIndex == -1) {
      lines ++ List(s"PartitionName=debug Nodes=$hostName, Default=YES MaxTime=INFINITE State=UP")
    } else {
      val partitionNameRegex = """^\s*PartitionName=.*Nodes=(\S*,).*""".r
      val partitionNameRegex(previousHostnames) = lines(partitionLineIndex)
      lines.updated(partitionLineIndex, lines(partitionLineIndex).replace(previousHostnames, previousHostnames + hostName + ","))
    }
  }
}