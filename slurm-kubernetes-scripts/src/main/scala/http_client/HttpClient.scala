package http_client


import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.libs.ws.StandaloneWSRequest
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.Future

class HttpClient(client: StandaloneAhcWSClient) {
  def get(url: String): Future[StandaloneWSRequest#Response] = {
    client.url(url).get()
  }

  def close(): Unit = {
    client.close()
  }

}

object HttpClient {

  implicit val system = ActorSystem()

  def apply(): HttpClient = {
    system.registerOnTermination {
      System.exit(0)
    }

    implicit val materializer = ActorMaterializer()

    new HttpClient(StandaloneAhcWSClient())
  }

  def terminate(): Unit = system.terminate()
}