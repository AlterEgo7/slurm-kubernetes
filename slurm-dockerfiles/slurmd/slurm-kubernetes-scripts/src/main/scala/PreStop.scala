import java.io.{BufferedWriter, FileWriter}
import java.net.InetAddress

import scala.io.Source

object PreStop {

  def apply(): Unit = {

    val lines = Source.fromFile("/usr/local/etc/slurm.conf").getLines()

    val newLines = removePartitionName(removeNodeName(lines.toVector))

    val bw = new BufferedWriter(new FileWriter("/usr/local/etc/slurm.conf"))

    bw.write(newLines.mkString("\n"))
    bw.close()

  }


  def removeNodeName(lines: Seq[String]): Seq[String] = {
    val hostname = InetAddress.getLocalHost.getCanonicalHostName

    lines.filterNot {
      _.trim.startsWith(s"NodeName=$hostname")
    }
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