import java.io.{BufferedWriter, FileWriter}
import java.net.InetAddress

import scala.io.Source

object PreStart {

  def apply(): Unit = {

    val lines = Source.fromFile("/usr/local/etc/slurm.conf").getLines()

    if (lines.exists{ l => l.contains(InetAddress.getLocalHost.getCanonicalHostName)}) return

    val newLines = addPartitionName(addNodeName(lines.toVector))

    val bw = new BufferedWriter(new FileWriter("/usr/local/etc/slurm.conf"))

    bw.write(newLines.mkString("\n"))
    bw.close()

  }


  def addNodeName(lines: Seq[String]): Seq[String] = {
    val lastNodeNameIndex = if (lines.exists { l => l.contains("NodeName=") }) {
      lines.zipWithIndex.filter { case (line, _) => line.trim.startsWith("NodeName=") }.last._2 + 1
    } else {
      lines.size
    }

    val (filePrefix, fileSuffix) = lines.splitAt(lastNodeNameIndex)
    val hostname = InetAddress.getLocalHost.getCanonicalHostName

    filePrefix ++
      List(s"NodeName=$hostname CPUs=2 SocketsPerBoard=1 CoresPerSocket=2 ThreadsPerCore=1 RealMemory=2000 State=UNKNOWN") ++
      fileSuffix
  }

  def addPartitionName(lines: Seq[String]): Seq[String] = {
    val partitionLineIndex = lines.indexWhere { l => l.contains("PartitionName=") }

    val hostName = InetAddress.getLocalHost.getCanonicalHostName
    if (partitionLineIndex == -1) {
      lines ++ List(s"PartitionName=debug Nodes=$hostName, Default=YES MaxTime=INFINITE State=UP")
    } else {
      val partitionNameRegex = """^\s*PartitionName=.*Nodes=(\S*,).*""".r
      val partitionNameRegex(previousHostnames) = lines(partitionLineIndex)
      lines.updated(partitionLineIndex, lines(partitionLineIndex).replace(previousHostnames, previousHostnames + hostName + ","))
    }
  }
}