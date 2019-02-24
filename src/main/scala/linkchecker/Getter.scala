package linkchecker

import akka.actor.{Actor, ActorSystem, Status}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.pattern.pipe
import org.jsoup.Jsoup
import org.jsoup.select.Elements

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContextExecutor

class Getter(url: String, depth: Int) extends Actor {

  implicit val system: ActorSystem = ActorSystem()
  implicit val executor: ExecutionContextExecutor = context.dispatcher

  Http()
    .singleRequest(HttpRequest(uri = url))
    .pipeTo(self)

  override def receive: Receive = {
    case body: String =>
      for (link <- findLinks(body))
        context.parent ! Controller.Check(link, depth)
      context.stop(self)
    case _: Status.Failure => context.stop(self)
  }

  def findLinks(body: String): Iterator[String] = {
    val document = Jsoup.parse(body, url)
    val links: Elements = document.select("a[href]")

    for {
      link <- links.iterator().asScala
    } yield link.absUrl("href")

  }
}
