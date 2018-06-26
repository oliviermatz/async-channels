//   Copyright (c) Olivier Matz. All rights reserved.
//   The use and distribution terms for this software are covered by the
//   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
//   which can be found in the file epl-v10.html at the root of this distribution.
//   By using this software in any fashion, you are agreeing to be bound by
//   the terms of this license.
//   You must not remove this notice, or any other, from this software.

package io.github.matzoliv.asyncchannel.scala

import io.github.matzoliv.asyncchannel._
import io.github.matzoliv.asyncchannel.implementation._
import io.github.matzoliv.asyncchannel.implementation.results._
import io.github.matzoliv.asyncchannel.scala.implementation.{AsyncChannelImpl, AsyncReadPortImpl}

import _root_.scala.concurrent.{Future, Promise}
import _root_.scala.concurrent.duration.Duration
import _root_.scala.annotation.tailrec

object Channels {

  def create(underlying: Channel): AsyncChannel = {
    new AsyncChannelImpl(underlying);
  }

  def create(buffer: Buffer): AsyncChannel = {
    new AsyncChannelImpl(new ManyToManyChannel(buffer))
  }

  def create(maxSize: Int): AsyncChannel = {
    create(new QueueBuffer(maxSize))
  }

  def create(): AsyncChannel = {
    create(maxSize = 0)
  }

  def timeout(duration: Duration): AsyncReadPort = {
    val timeoutChannel = Timers.createTimerChannel(duration.toMillis)
    new AsyncReadPortImpl {
      override def underlying: ReadPort = timeoutChannel
    }
  }

  def alts(portsAndWrappers: (ReadPort, AnyRef => AnyRef)*): Future[AnyRef] = {
    val promise = Promise[AnyRef]()
    val handler = new ConsumerHandler[AnyRef](_ => ())

    @tailrec
    def loop(portsAndWrappers: List[(ReadPort, AnyRef => AnyRef)]): Future[AnyRef] = portsAndWrappers match {
      case (port, wrapper)::rest =>
        val takeResult = port.take(new AltHandler[AnyRef](handler, x => promise.success(wrapper(x))))
        if (takeResult.isInstanceOf[TakeSucceeded]) {
          Promise.successful[AnyRef](wrapper(takeResult.asInstanceOf[TakeSucceeded].getResult())).future
        } else if (takeResult.isInstanceOf[TakeCancelled]) {
          promise.future
        } else {
          if (rest.isEmpty) {
            promise.future
          } else {
            loop(rest)
          }
        }
      case Nil => Promise.successful[AnyRef](null).future
    }
    loop(portsAndWrappers.toList)
  }
}
