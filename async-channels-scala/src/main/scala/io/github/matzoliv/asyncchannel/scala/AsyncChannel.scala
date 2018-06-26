//   Copyright (c) Olivier Matz. All rights reserved.
//   The use and distribution terms for this software are covered by the
//   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
//   which can be found in the file epl-v10.html at the root of this distribution.
//   By using this software in any fashion, you are agreeing to be bound by
//   the terms of this license.
//   You must not remove this notice, or any other, from this software.

package io.github.matzoliv.asyncchannel.scala

import scala.concurrent.{ExecutionContext, Future}
import io.github.matzoliv.asyncchannel._

trait AsyncReadPort extends ReadPort {
  def readAsync(): Future[AnyRef]
  def poll(): Option[AnyRef]
}

trait AsyncWritePort extends WritePort {
  def putAsync(x: AnyRef): Future[Unit]
  def offer(x: AnyRef): Boolean
}

trait AsyncChannel extends AsyncReadPort with AsyncWritePort
