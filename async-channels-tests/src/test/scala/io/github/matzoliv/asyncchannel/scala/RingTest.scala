//   Copyright (c) Olivier Matz. All rights reserved.
//   The use and distribution terms for this software are covered by the
//   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
//   which can be found in the file epl-v10.html at the root of this distribution.
//   By using this software in any fashion, you are agreeing to be bound by
//   the terms of this license.
//   You must not remove this notice, or any other, from this software.

package io.github.matzoliv.asyncchannel.scala

import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest._
import org.scalatest.junit.JUnitRunner

import scala.concurrent.{Await, Future, Promise}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Success

@RunWith(classOf[JUnitRunner])
class RingTest extends FlatSpec with Matchers {

  val random = new scala.util.Random()

  Seq(0, 10, 100, 1000).map { size =>
    s"Sending lots of messages in a ring with buffers size of $size" should "not loose messages" in {
      val channels = Seq.range(0, 1000).map { _ => Channels.create(size) }
      val resultPromise = Promise[Seq[Int]]()

      val pairWise = Seq.range(0, 999).zip(channels.dropRight(1).zip(channels.drop(1)))
      pairWise.map { case (id, (cur, next)) =>
        def loop(n: Int): Future[Unit] = {
          if (n > 0) {
            for {
              msg <- cur.readAsync()
              _   <- next.putAsync(msg)
              _   <- loop(n - 1)
            } yield ()
          } else {
            Future.unit
          }
        }
        loop(1000)
      }

      val first = channels.head
      val last = channels.last

      {
        def loop(n: Int): Future[Unit] = {
          if (n > 0) {
            for {
              _ <- first.putAsync(Int.box(n))
              _ <- loop(n - 1)
            } yield ()
          } else {
            Future.unit
          }
        }
        loop(1000)
      }

      {
        def loop(n: Int, acc: Seq[Int]): Future[Unit] =
          if (n > 0) {
            for {
              msg <- last.readAsync()
              _   <- loop(n - 1, acc :+ Int.unbox(msg))
            } yield ()
          } else {
            resultPromise.complete(Success(acc))
            Future.unit
          }
        loop(1000, Seq.empty)
      }

      val results = Await.result(resultPromise.future, 1 minute)
      results shouldEqual Seq.range(1, 1001).reverse
    }
  }
}
