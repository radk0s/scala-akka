import akka.testkit.{ImplicitSender, TestKit, TestActorRef, TestFSMRef}
import akka.actor.{Props, ActorSystem, FSM}
import org.scalatest.{WordSpecLike, BeforeAndAfterAll}
import pl.edu.agh.reactive.actors._
import scala.concurrent.Await
import scala.concurrent.duration._

class AuctionSpec extends TestKit(ActorSystem("ToggleSpec"))
with WordSpecLike with BeforeAndAfterAll with ImplicitSender {

    override def afterAll(): Unit = {
        system.shutdown()
    }

    val fsm = TestFSMRef(Auction("auto"))

    "flow" in {
        assert(fsm.stateName == NonExisted)
        assert(fsm.stateData == Uninitialized)

        fsm ! Start(40)

        assert(fsm.stateName == Created)
        assert(fsm.stateData != Uninitialized)

        fsm ! BidTimerExpired()

        assert(fsm.stateName == Ignored)

        fsm ! Relist()

        assert(fsm.stateName == Created)

        fsm ! Bid(50)

        expectMsg(AuctionResponse("auto", "You are first."))
        assert(fsm.stateName == Activated)

        fsm ! Bid(40)

        expectMsg(AuctionResponse("auto", "Your offer is too low (40), current: 50"))
        assert(fsm.stateName == Activated)

        fsm ! Bid(60)

        expectMsg(AuctionResponse("auto", "Your offer is highest now."))
        expectMsg(LostLeadNotification(60))
        assert(fsm.stateName == Activated)

        fsm ! BidTimerExpired()

        expectMsg(AuctionResponse("auto", "You won auction"))
        assert(fsm.stateName == Sold)
    }
}
