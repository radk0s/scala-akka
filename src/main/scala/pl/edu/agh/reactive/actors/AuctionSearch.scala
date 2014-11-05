package pl.edu.agh.reactive.actors

import akka.actor.{ActorRef, Actor}

class AuctionSearch extends Actor {

  val r = scala.util.Random
  var auctions: Map[String, ActorRef] = Map.empty

  def receive = {
    case RegisterAuction(name, auction) =>
      auctions += (name -> auction)

    case SearchAuctions(phrase) =>
      sender ! FoundAuctions(auctions.filterKeys(key => key.contains(phrase)).values)

  }
}
