package pl.edu.agh.reactive.actors

import akka.actor.{Props, ActorRef, Actor}
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import akka.event.Logging

case class Buyer(auctions: List[ActorRef]) extends Actor{
  import context._

  val log = Logging(context.system, this)
  val scheduler = context.system.scheduler
  val r = scala.util.Random

  def receive = {
    case BidSomething() =>
      scheduler.scheduleOnce(Duration.create(r.nextInt(15), TimeUnit.SECONDS), auctions(r.nextInt(auctions.length)), Bid(r.nextInt(300)))
    case AuctionResponse(msg) =>
      log.debug(s"${self.path.name} from ${sender().path.name} msg: $msg")
  }

}
