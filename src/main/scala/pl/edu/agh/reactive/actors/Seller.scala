package pl.edu.agh.reactive.actors

import akka.actor.{Props, Actor}

class Seller extends Actor {

  def receive = {
    case CreateAuction(name) =>
      val auctionSearch = context.actorSelection("akka://AuctionSystem/user/manager/masterSearch")
      val auction = context.actorOf(Props(classOf[Auction], name))
      auction ! Start(40)
      auctionSearch ! RegisterAuction(name, auction)
  }
}

