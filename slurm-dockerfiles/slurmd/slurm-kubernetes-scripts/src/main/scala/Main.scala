object Main {
  def main(args: Array[String]): Unit = args match {
    case Array("preStart") => PreStart()
    case Array("preStop") => PreStop()
    case _ => println("Usage: <filename> <preStart|preStop>")
  }
}
