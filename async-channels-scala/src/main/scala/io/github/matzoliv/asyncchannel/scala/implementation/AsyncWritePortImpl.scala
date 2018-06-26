//   Copyright (c) Olivier Matz. All rights reserved.
//   The use and distribution terms for this software are covered by the
//   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
//   which can be found in the file epl-v10.html at the root of this distribution.
//   By using this software in any fashion, you are agreeing to be bound by
//   the terms of this license.
//   You must not remove this notice, or any other, from this software.

package io.github.matzoliv.asyncchannel.scala.implementation

import scala.concurrent.{Future, Promise, ExecutionContext}
import io.github.matzoliv.asyncchannel._
import io.github.matzoliv.asyncchannel.implementation._
import io.github.matzoliv.asyncchannel.implementation.results._
import io.github.matzoliv.asyncchannel.scala.AsyncWritePort

trait AsyncWritePortImpl extends AsyncWritePort {
  def underlying: WritePort

  override def putAsync(x: AnyRef): Future[Unit] = {
    val promise = Promise[Unit]()
    val putResult = put(x, new ConsumerHandler[AnyRef](_ => promise.success(())))
    if (putResult.isInstanceOf[PutSucceeded]) {
      Promise.successful(()).future
    } else {
      promise.future
    }
  }

  override def offer(x: AnyRef): Boolean = {
    val putResult = put(x, new ConsumerHandler[AnyRef](_ => (), false))
    putResult.isInstanceOf[PutSucceeded]
  }

  override def put(value: AnyRef, handler: Handler[AnyRef]): PutResult = underlying.put(value, handler)

  override def putAlts(value: AnyRef): ReadPort = underlying.putAlts(value)
}
