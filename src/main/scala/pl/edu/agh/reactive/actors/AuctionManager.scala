package pl.edu.agh.reactive.actors

import akka.actor.{ActorRef, Props, Actor}
import akka.event.Logging
import scala.util.Random

/**
 * Created by Radek on 19.10.14.
 */
class AuctionManager extends Actor{
  val log = Logging(context.system, this)
  val NUM_OF_AUCTIONS = 9
  val NUM_OF_SELLERS = 3
  val NUM_OF_BUYERS = 30
  val auctionNames = List(
    "mazda diesel 2006", "polonez 1992 tdi", "mercedes petrol 2006",
    "mazda petrol 2004", "seat ibiza 2006", "ferrari 2000 tdi",
    "seat toledo tdi 2005", "fiat kangoo 2000", "fiat punto 2005"
  )
  val r = scala.util.Random

  def receive = {
    case message: AuctionManagementSystemMessage => message match {
      case OpenSystem() =>
        log.debug("Enable Auction Manager received")
        log.debug(context.parent.toString())

        val search = context.actorOf(Props[MasterSearch], "masterSearch")

        val sellersList = (1 to NUM_OF_SELLERS).map(num => context.actorOf(Props[Seller], "seller"+num)).toList

        auctionNames.zipWithIndex.foreach{
          case(name,i) =>
            sellersList(i%3) ! CreateAuction(name)
        }

        val buyersList = (1 to NUM_OF_BUYERS).map(num => context.actorOf(Props(classOf[Buyer], r.nextInt(400)), "Buyer-"+num)).toList
        buyersList.foreach((x: ActorRef) => x ! BidSomething())
    }
  }

}
