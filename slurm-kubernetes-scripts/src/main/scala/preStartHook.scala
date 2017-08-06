import java.io.{BufferedWriter, FileOutputStream, OutputStreamWriter}
import java.net.InetAddress

import scala.io.Source

object preStartHook {
  def main(args: Array[String]): Unit = {

    val fileLines = Source.fromResource("slurm-config.yaml").getLines().toVector
    val newLines = if (fileLines.exists { l => l.contains(InetAddress.getLocalHost.getHostName) }) fileLines else addPartitionName(addNodeName(fileLines))

    val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("output.yaml")))
    for (line <- newLines) {
      writer.write(line + "\n")
    }
    writer.close()

    //    println(endNodeNamesIndex)

    //    fileLines.foreach {
    //      println
    //    }
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