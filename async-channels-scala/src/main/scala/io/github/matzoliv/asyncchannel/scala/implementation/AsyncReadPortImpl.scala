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
import io.github.matzoliv.asyncchannel.scala.AsyncReadPort

trait AsyncReadPortImpl extends AsyncReadPort {
  def underlying: ReadPort

  override def readAsync(): Future[AnyRef] = {
    val promise = Promise[AnyRef]()
    val takeResult = take(new ConsumerHandler[AnyRef](x => promise.success(x)))
    if (takeResult.isInstanceOf[TakeSucceeded]) {
      Promise.successful(takeResult.asInstanceOf[TakeSucceeded].getResult()).future
    } else {
      promise.future
    }
  }

  override def poll(): Option[AnyRef] = {
    val takeResult = take(new ConsumerHandler[AnyRef](_ => (), false));
    if (takeResult.isInstanceOf[TakeSucceeded]) {
      Some(takeResult.asInstanceOf[TakeSucceeded].getResult())
    } else {
      None
    }
  }

  override def take(handler: Handler[AnyRef]): TakeResult = underlying.take(handler)
}
