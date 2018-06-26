//   Copyright (c) Olivier Matz, Rich Hickey and contributors. All rights reserved.
//   The use and distribution terms for this software are covered by the
//   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
//   which can be found in the file epl-v10.html at the root of this distribution.
//   By using this software in any fashion, you are agreeing to be bound by
//   the terms of this license.
//   You must not remove this notice, or any other, from this software.

package io.github.matzoliv.asyncchannel.implementation;

import io.github.matzoliv.asyncchannel.*;
import io.github.matzoliv.asyncchannel.implementation.results.*;

public class AltReadPort implements ReadPort {

    private WritePort writePort;
    private Object value;

    public AltReadPort(WritePort writePort, Object value)  {
        this.writePort = writePort;
        this.value = value;
    }

    @Override
    public TakeResult take(Handler handler) {
        PutResult result = writePort.put(value, handler);

        if (result instanceof PutSucceeded) {
            return new TakeSucceeded(null);
        } else if (result instanceof PutParked) {
            return new TakeParked();
        } else {
            return new TakeCancelled();
        }
    }
}
