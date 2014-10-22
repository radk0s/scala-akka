package pl.edu.agh.reactive.actors

import akka.actor.{Cancellable, ActorRef, FSM, Actor}
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit

class Auction extends Actor with FSM[State, Data]{
  import context._

  val r = scala.util.Random
  val scheduler = context.system.scheduler
  var lastEvent: Option[Cancellable] = None

  startWith(NonExisted, Uninitialized)

  when(NonExisted) {
    case Event(Start(name, minPrice), Uninitialized) =>
      log.debug("Initialize auction.")
      scheduler.scheduleOnce(Duration.create(15, TimeUnit.SECONDS), self, BidTimerExpired())
      goto(Created) using Item(name, minPrice, context.parent)
  }

  when(Created) {
    case Event(Bid(newOffer), Item(name, offer, winner)) =>
      log.debug(s"New bid recieved ${sender().path.name} - $newOffer")
      sender ! AuctionResponse("You are first.")
      goto(Activated) using Item(name, newOffer, sender())

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
    case Event(Bid(newOffer), Item(name, offer, winner)) =>
      log.debug("Next bid recieved.")
      if (newOffer >= offer) {
        sender ! AuctionResponse("Your offer is too low.")
        goto(Activated) using Item(name, offer, winner)
      }
      else {
        sender ! AuctionResponse("Your offer is highest now.")
        goto(Activated) using Item(name, newOffer, sender())
      }
    case Event(BidTimerExpired(), i: Item) =>
      log.debug("Item sold.")
      i.buyer ! AuctionResponse("You won auction")
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
case class Item(name: String, currentPrice: Int, buyer: ActorRef) extends Data
