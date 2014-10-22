package pl.edu.agh.reactive.actors

import akka.actor.{ActorRef, Props, Actor}
import akka.event.Logging
import scala.util.Random

/**
 * Created by Radek on 19.10.14.
 */
class AuctionManager extends Actor{
  val log = Logging(context.system, this)
  val NUM_OF_AUCTIONS = 10
  val NUM_OF_BUYERS = 10
  val r = scala.util.Random

  def receive = {
    case message: AuctionManagementSystemMessage => message match {
      case OpenSystem() =>
        log.debug("Enable Auction Manager received")
        log.debug(context.parent.toString())
        val auctionsList = (1 to NUM_OF_AUCTIONS).map(num => context.actorOf(Props[Auction], "Auction-"+num)).toList
        val buyersList = (1 to NUM_OF_BUYERS).map(num => context.actorOf(Props(classOf[Buyer], auctionsList), "Buyer-"+num)).toList

        auctionsList.foreach((x: ActorRef) => x ! Start(x.path.name, r.nextInt(40)))

        buyersList.foreach((x: ActorRef) => x ! BidSomething())
    }
  }

}
