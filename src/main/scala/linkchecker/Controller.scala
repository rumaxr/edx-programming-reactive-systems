package linkchecker

import akka.actor.{Actor, ActorLogging, ActorRef, Props, ReceiveTimeout, Terminated}

import scala.concurrent.duration._

object Controller {

  case class Check(url: String, depth: Int)

  case class Result(links: Set[String])

}

class Controller extends Actor with ActorLogging {

  import Controller.{Check, Result}

  var cache = Set.empty[String]

  context.setReceiveTimeout(10.seconds)

  def getterProps(url: String, depth: Int): Props = Props(new Getter(url, depth))

  override def receive: Receive = {
    case Check(url, depth) =>
      log.debug("{} checking {}", depth, url)
      if (!cache(url) && depth > 0) {
        context.watch(context.actorOf(getterProps(url, depth - 1)))
        cache += url
      }
    case Terminated(_) =>
      if (context.children.isEmpty)
        context.parent ! Result(cache)
    case ReceiveTimeout =>
      context.children foreach context.stop
  }
}
