package pl.edu.agh.reactive.actors

import akka.actor.{ActorRef, Actor}
import akka.event.Logging

class AuctionSearch extends Actor {
  val log = Logging(context.system, this)
  val r = scala.util.Random
  var auctions: Map[String, ActorRef] = Map.empty

  def receive = {
    case RegisterAuction(name, auction) =>
      auctions += (name -> auction)

    case SearchAuctions(phrase) =>

      sender ! FoundAuctions(auctions.filterKeys(key => key.contains(phrase)).values)

  }
}
