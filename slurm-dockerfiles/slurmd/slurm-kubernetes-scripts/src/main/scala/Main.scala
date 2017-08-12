object Main {
  def main(args: Array[String]): Unit = args match {
    case Array("preStart") => PreStart()
    case Array("preDestroy") => PreDestroy()
    case _ => println("Usage: <filename> <preStart|preDestroy>")
  }
}
