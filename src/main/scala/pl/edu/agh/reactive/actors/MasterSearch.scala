package pl.edu.agh.reactive.actors

/**
 * Created by Radek on 12.11.14.
 */
import akka.actor.{ActorRef, Props, Actor}
import akka.routing._
import akka.routing.Router
import akka.routing.ActorRefRoutee
import akka.event.Logging

class MasterSearch extends Actor {
  val log = Logging(context.system, this)

  val (registerRouter, searchRouter) = {
    val routees = List.fill(25) {
      val r = context.actorOf(Props[AuctionSearch])
      context watch r
      log.info(s"Path: /user/manager/masterSearch/${r.path.name}")

      "/user/manager/masterSearch/" + r.path.name
    }
    //wariant1
//    (Router(RoundRobinRoutingLogic(), routees),Router(BroadcastRoutingLogic(), routees))
    val search: ActorRef =context.actorOf(BroadcastGroup(routees).props(), "router3")
    val register: ActorRef =context.actorOf(RoundRobinGroup(routees).props(), "router30")

    //wariant2
//    (Router(BroadcastRoutingLogic(), routees),Router(RoundRobinRoutingLogic(5), routees))
//    val search: ActorRef =context.actorOf(RoundRobinGroup(routees).props(), "router3")
//    val register: ActorRef =context.actorOf(BroadcastGroup(routees).props(), "router30")

    //wariant3
//    val resizer = DefaultResizer(lowerBound = 2, upperBound = 15)
//    val search: ActorRef =context.actorOf(RoundRobinPool(5, Some(resizer)).props(Props[AuctionSearch]), "router3")
//    val register: ActorRef =context.actorOf(BroadcastPool(5, Some(resizer)).props(Props[AuctionSearch]), "router30")

    (register, search)
  }

  def receive = {
    case RegisterAuction(name, auction) =>
      registerRouter.tell(RegisterAuction(name, auction), sender())
    case SearchAuctions(phrase) =>
      searchRouter.tell(SearchAuctions(phrase), sender())

  }
}
