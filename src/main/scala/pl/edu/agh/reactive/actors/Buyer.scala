package pl.edu.agh.reactive.actors

import akka.actor.{Props, ActorRef, Actor}
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import akka.event.Logging

case class Buyer(max: Int) extends Actor{
  import context._

  val log = Logging(context.system, this)
  val scheduler = context.system.scheduler
  val r = scala.util.Random
  val phrases = List("mazda", "polonez", "seat", "mercedes", "fiat", "ibiza","tdi", "1992", "2006", "2005", "petrol", "diesel")

  def receive = {
    case BidSomething() =>
      val auctionSearch = context.actorSelection("akka://AuctionSystem/user/manager/auctionSearch")
      auctionSearch ! SearchAuctions(phrases(r.nextInt(phrases.length)))

    case FoundAuctions(auctions) =>
      auctions.foreach(auction => {
        println(auction)
        scheduler.scheduleOnce(Duration.create(r.nextInt(15), TimeUnit.SECONDS), auction, Bid(r.nextInt(300)))
      })

    case AuctionResponse(auctionName, msg) =>
      log.debug(s"auction: $auctionName msg: $msg")

    case LostLeadNotification(newOffer) =>
      if (newOffer >= max){
        log.debug(s"Reached price limit, limit: $max, current: $newOffer")
      } else {
        log.debug(s"Bid again, limit: $max, current: $newOffer")
        sender ! Bid(newOffer + r.nextInt(max- newOffer))
      }
  }

}
