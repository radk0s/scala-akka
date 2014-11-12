package pl.edu.agh.reactive.actors

/**
 * Created by Radek on 12.11.14.
 */

import akka.actor.{Props, Actor}
import akka.routing.{BroadcastRoutingLogic, ActorRefRoutee, Router, RoundRobinRoutingLogic}
import pl.edu.agh.reactive.actors.AuctionSearch

class MasterSearch extends Actor {
  var (registerRouter, searchRouter) = {
    val routees = Vector.fill(5) {
      val r = context.actorOf(Props[AuctionSearch])
      context watch r
      ActorRefRoutee(r)
    }
    (Router(RoundRobinRoutingLogic(), routees),
    Router(BroadcastRoutingLogic(), routees))
  }

  def receive = {
    case RegisterAuction(name, auction) =>
      registerRouter.route(RegisterAuction(name, auction), sender())
    case SearchAuctions(phrase) =>
      searchRouter.route(SearchAuctions(phrase), sender())

  }
}
