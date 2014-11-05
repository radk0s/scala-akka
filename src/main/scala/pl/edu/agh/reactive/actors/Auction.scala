package pl.edu.agh.reactive.actors

import akka.actor.{Cancellable, ActorRef, FSM, Actor}
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit

case class Auction(name: String) extends Actor with FSM[State, Data]{
  import context._

  val r = scala.util.Random
  val scheduler = context.system.scheduler
  var lastEvent: Option[Cancellable] = None

  startWith(NonExisted, Uninitialized)

  when(NonExisted) {
    case Event(Start(minPrice), Uninitialized) =>
      log.debug("Initialize auction.")
      scheduler.scheduleOnce(Duration.create(15, TimeUnit.SECONDS), self, BidTimerExpired())
      goto(Created) using Item(minPrice, context.parent)
  }

  when(Created) {
    case Event(Bid(newOffer), Item( offer, winner)) =>
      log.debug(s"New bid recieved ${sender().path.name} - price: $newOffer")
      sender ! AuctionResponse(name, "You are first.")
      goto(Activated) using Item( newOffer, sender())

    case Event(BidTimerExpired(), i: Item) =>
      log.debug("Auction Expired.")
      scheduler.scheduleOnce(Duration.create(r.nextInt(20), TimeUnit.SECONDS), self, DeleteTimerExpired())
      goto(Ignored) using i.copy()
  }

  when(Ignored) {
    case Event(Relist(), i: Item) =>
      log.debug("Auction relisted.")
      scheduler.scheduleOnce(Duration.create(15, TimeUnit.SECONDS), self, BidTimerExpired())
      goto(Created) using i.copy()
    case Event(DeleteTimerExpired(), i: Item) =>
      log.debug("AuctionDeleted - END")
      stay()
  }
  when(Activated) {
    case Event(Bid(newOffer), Item(offer, winner)) =>
      log.debug("Next bid recieved.")
      if (newOffer <= offer) {
        sender ! AuctionResponse(name, s"Your offer is too low ($newOffer), current: $offer")
        goto(Activated) using Item( offer, winner)
      }
      else {
        sender ! AuctionResponse(name, "Your offer is highest now.")
        winner ! LostLeadNotification(newOffer)
        goto(Activated) using Item(newOffer, sender())
      }
    case Event(BidTimerExpired(), i: Item) =>
      log.debug("Item sold.")
      i.buyer ! AuctionResponse(name, "You won auction")
      scheduler.scheduleOnce(Duration.create(5, TimeUnit.SECONDS), self, DeleteTimerExpired())
      goto(Sold) using i.copy()
  }

  when(Sold) {
    case Event(DeleteTimerExpired(), i: Item) =>
      log.debug("Item sold - END")
      stay()
  }
}


sealed trait State
case object NonExisted extends State
case object Created extends State
case object Activated extends State
case object Ignored extends State
case object Sold extends State

sealed trait Data
case object Uninitialized extends Data
case class Item(currentPrice: Int, buyer: ActorRef) extends Data
