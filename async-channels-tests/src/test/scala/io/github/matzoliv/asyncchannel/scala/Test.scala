package io.github.matzoliv.asyncchannel.scala

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

object Test {

  def main(args: Array[String]) = {

    def loop(id: String, loopLeft: Int, in: AsyncChannel, out: AsyncChannel): Future[Unit] = for {
      msg <- in.readAsync()
      _ <- {
        println(s"Task $id received $msg, left $loopLeft")
        out.putAsync(if (msg == "ping") { "pong" } else { "ping" })
      }
      _ <- if (loopLeft > 0) {
        loop(id, loopLeft - 1, in, out)
      } else {
        Future()
      }
    } yield ()

    val c1 = Channels.create()
    val c2 = Channels.create()

    c1.putAsync("ping")

    Await.result(Future.firstCompletedOf(Seq(
      loop("A", 100, c1, c2),
      loop("B", 100, c2, c1)
    )), atMost = 10 seconds)
  }
}
