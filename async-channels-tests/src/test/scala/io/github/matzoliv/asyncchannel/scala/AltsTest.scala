package io.github.matzoliv.asyncchannel.scala

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

@RunWith(classOf[JUnitRunner])
class AltsTest extends FlatSpec with Matchers {

  "Channels.alts" should "only activate one handler" in {
    val c = Channels.create()

    case class Ok(x: AnyRef)
    case object Timeout

    val futureProducer = for {
      _ <- c.putAsync(Int.box(10))
    } yield ()

    val futureConsumer = for {
      msg <- Channels.alts(
        (c -> (Ok(_))),
        (Channels.timeout(100 millis) -> (_ => Timeout))
      )

      _ <- Future { println(msg) }
    } yield ()

    Await.result(futureConsumer, 1 second)
  }

}
