import java.io.{BufferedWriter, FileOutputStream, OutputStreamWriter}
import java.net.InetAddress

import http_client.HttpClient

import scala.util.{Failure, Success}

object preStartHook {

  import scala.concurrent.ExecutionContext.Implicits._

  def main(args: Array[String]): Unit = {

    val client = HttpClient()

    val responseFuture = client.get("http://gluster1:8081/api/v1/namespaces/default/configmaps/slurm-config")
      .andThen{ case _ => client.close() }
      .andThen{ case _ => HttpClient.terminate() }

    responseFuture onComplete {
      case Success(response) => {
        val body = response.body
        val fileLines = body.split("\\\\n").toVector

        val newLines = if (fileLines.exists { l => l.contains(InetAddress.getLocalHost.getHostName) }) fileLines else addPartitionName(addNodeName(fileLines))

        val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("output.json")))

        writer.write(newLines.mkString("\\\\n"))
        writer.close()
      }
      case Failure(cause) => Failure(new Exception("Kubernetes API not found"))
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
      List(s"      NodeName=${InetAddress.getLocalHost.getHostName} CPUs=2 SocketsPerBoard=1 CoresPerSocket=2 ThreadsPerCore=1 RealMemory=2000 State=UNKNOWN\n") ++
      fileSuffix
  }

  def addPartitionName(lines: Seq[String]): Seq[String] = {
    val partitionLineIndex = lines.indexWhere { l => l.contains("PartitionName=") }

    val hostName = InetAddress.getLocalHost.getHostName
    if (partitionLineIndex == -1) {
      lines ++ List(s"      PartitionName=debug Nodes=$hostName, Default=YES MaxTime=INFINITE State=UP")
    } else {
      val partitionNameRegex = """^\s*PartitionName=.*Nodes=(\S*,).*""".r
      val partitionNameRegex(previousHostnames) = lines(partitionLineIndex)
      lines.updated(partitionLineIndex, lines(partitionLineIndex).replace(previousHostnames, previousHostnames + hostName + ","))
    }
  }
}