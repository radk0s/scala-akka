package pl.edu.agh.reactive

import akka.actor.ActorSystem
import akka.event.Logging
import akka.actor.Props
import pl.edu.agh.reactive.actors._

/**
  * Created by Radek on 19.10.14.
  */
object AuctionSystem {


   val system = ActorSystem("AuctionSystem")

   val log = Logging(system, AuctionSystem.getClass.getName)

   def main(args: Array[String]): Unit = run()

   def run() = {
     log.debug("Initializing auction system.")
     val manager = system.actorOf(Props[AuctionManager], "manager")
     manager ! OpenSystem()
   }

 }
