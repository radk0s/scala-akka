package pl.edu.agh.reactive.actors

import akka.actor.{Props, ActorRef, Actor}
import scala.concurrent.duration.{FiniteDuration, Duration}
import java.util.concurrent.TimeUnit
import akka.event.Logging
import java.sql.Time
import java.util.{Date, Calendar}
import scala.concurrent.duration._

case class Buyer(max: Int) extends Actor{
  import context._

  val log = Logging(context.system, this)
  val scheduler = context.system.scheduler
  val r = scala.util.Random
  val phrases = List("mazda", "polonez", "seat", "mercedes", "fiat", "ibiza","tdi", "1992", "2006", "2005", "petrol", "diesel")
  var startTime: Long = 0

  def receive = {
    case BidSomething() =>
      val auctionSearch = context.actorSelection("akka://AuctionSystem/user/manager/masterSearch")
      auctionSearch ! SearchAuctions(phrases(r.nextInt(phrases.length)))
      startTime = System.currentTimeMillis()

    case FoundAuctions(auctions) =>
      val endTime = System.currentTimeMillis()
      log.info(s"_${(endTime.millis - startTime.millis).toMillis}_ millis")
      log.debug(auctions.toString())
      auctions.foreach(auction => {
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
