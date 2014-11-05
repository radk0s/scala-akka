package pl.edu.agh.reactive.actors

import akka.actor.ActorRef

sealed trait AuctionManagementSystemMessage

case class OpenSystem() extends AuctionManagementSystemMessage
case class Bid(newOffer: Int)
case class Start(price: Int)
case class Relist()
case class DeleteTimerExpired()
case class BidTimerExpired()
case class AuctionResponse(name: String, message: String)
case class BidSomething()
case class LostLeadNotification(newOffer: Int)
case class CreateAuction(name: String)
case class RegisterAuction(name: String, auctionActor: ActorRef)
case class SearchAuctions(name: String)
case class FoundAuctions(auctions: Iterable[ActorRef])