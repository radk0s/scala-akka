package pl.edu.agh.reactive.actors

sealed trait AuctionManagementSystemMessage

case class OpenSystem() extends AuctionManagementSystemMessage
case class Bid(newOffer: Int)
case class Start(name: String, price: Int)
case class Relist()
case class DeleteTimerExpired()
case class BidTimerExpired()
case class AuctionResponse(message: String)
case class BidSomething()