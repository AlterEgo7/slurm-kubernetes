package http_client


import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.libs.ws.StandaloneWSRequest
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.Future

class HttpClient(client: StandaloneAhcWSClient) {
  def url(url: String): StandaloneWSRequest = {
    client.url(url)
  }

  def get(url: String): Future[StandaloneWSRequest#Response] = {
    client.url(url).get()
  }

  def close(): Unit = {
    client.close()
  }

}

object HttpClient {
  implicit val system = ActorSystem()

  def apply(): StandaloneAhcWSClient = {
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    StandaloneAhcWSClient()
  }

  def terminate(): Unit = system.terminate()
}