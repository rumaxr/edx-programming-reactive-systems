package linkchecker

import akka.actor.{Actor, ActorRef, Props, ReceiveTimeout}

import scala.concurrent.duration._

class LinkCheckerMain extends Actor {

  import Receptionist.{Failed, Get, Result}

  val receptionist: ActorRef = context.actorOf(Props[Receptionist], name = "receptionist")

  receptionist ! Get("https://www.nu.nl")

  context.setReceiveTimeout(10.seconds)

  override def receive: Receive = {
    case Result(url, set) =>
      println(set.toVector.sorted.mkString(s"Results for '$url':\n", "\n", "\n"))
    case Failed(url, reason) =>
      println(s"Failed to fetch '$url': $reason\n")
    case ReceiveTimeout =>
      context.stop(self)
  }
}
