Java implementation of Clojure's core.async channels. The important logic was transposed with little modifications.
It has no dependencies and uses built in standard Future APIs in both Java and Scala.

Ping pong example in Java :

```java
import io.github.matzoliv.asyncchannel.AsyncChannel;
import io.github.matzoliv.asyncchannel.Channels;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class Test {
    public static CompletableFuture<Void> pingPongLoop(String id, int loopLeft, AsyncChannel in, AsyncChannel out) {
        return in.readAsync()
                .thenComposeAsync(msg -> {
                    System.out.println(String.format("Task %s: received %s, left %d", id, msg, loopLeft));
                    if (msg.equals("ping")) {
                        return out.putAsync("pong");
                    } else {
                        return out.putAsync("ping");
                    }
                })
                .thenComposeAsync((Void x) -> {
                    if (loopLeft > 1) {
                        return pingPongLoop(id, loopLeft - 1, in, out);
                    } else {
                        return CompletableFuture.completedFuture(null);
                    }
                });
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
        AsyncChannel c1 = Channels.create();
        AsyncChannel c2 = Channels.create();

        c1.putAsync("ping");

        CompletableFuture.anyOf(
            pingPongLoop("A", 100, c1, c2),
            pingPongLoop("B", 100, c2, c1)
        ).get();
    }
}
```

Ping pong example in Scala :

```scala
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
```