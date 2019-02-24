package linkchecker

import akka.actor.{Actor, ActorRef, Props, SupervisorStrategy, Terminated}

object Receptionist {

  private case class Job(client: ActorRef, url: String)

  case class Get(url: String)

  case class Result(url: String, links: Set[String])

  case class Failed(url: String, reason: String)

}

class Receptionist extends Actor {

  import Controller.Check
  import Receptionist.{Failed, Get, Job, Result}

  def controllerProps: Props = Props[Controller]

  override def supervisorStrategy: SupervisorStrategy = SupervisorStrategy.stoppingStrategy

  var requestCount = 0

  override def receive: Receive = waiting

  val waiting: Receive = {
    case Get(url) =>
      context.become(runNext(Vector(Job(sender, url))))
  }

  def runNext(queue: Vector[Job]): Receive = {
    requestCount += 1
    if (queue.isEmpty) waiting
    else {
      val controller = context.actorOf(controllerProps, s"c$requestCount")
      context.watch(controller)
      controller ! Check(queue.head.url, 2)
      running(queue)
    }
  }

  def running(queue: Vector[Job]): Receive = {
    case Controller.Result(links) =>
      val job = queue.head
      job.client ! Result(job.url, links)
      context.stop(context.unwatch(sender))
      context.become(runNext(queue.tail))
    case Terminated(_) =>
      val job = queue.head
      job.client ! Failed(job.url, "controller failed unexpectedly")
      context.become(runNext(queue.tail))
    case Get(url) =>
      context.become(enqueueJob(queue, Job(sender, url)))
  }

  def enqueueJob(queue: Vector[Job], job: Job): Receive = {
    if (queue.size > 3) {
      sender ! Failed(job.url, "queue overflow")
      running(queue)
    }
    else {
      running(queue :+ job)
    }
  }
}
