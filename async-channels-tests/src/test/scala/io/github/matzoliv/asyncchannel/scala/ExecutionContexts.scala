import java.util.concurrent.Executors

import io.github.matzoliv.asyncchannel.scala.Channels
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class ExecutionContexts extends FlatSpec with Matchers {

  // Those tests don't actually exercice properties of this library. Scala's Future implementation
  // enforce that any callbacks be registered with an ExecutionContext associated to it. When the
  // callbacks activates, it's enqueued in the ExecutionContext's executor. Thus continuations are
  // never executed immediately in the same call stack.
  "Async operations" should "execute continuations in the proper context" in {
    val context1 = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1));
    val context2 = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1));

    val context1ThreadId = Await.result(Future { Thread.currentThread().getId() }(context1), atMost = 1 second)
    val context2ThreadId = Await.result(Future { Thread.currentThread().getId() }(context2), atMost = 1 second)

    val c = Channels.create()

    val putFuture = c.putAsync("hello").map { x => Thread.currentThread().getId() }(context1)
    val readFuture = c.readAsync().map { x => Thread.currentThread().getId() }(context2)

    val putContinuationThreadId = Await.result(putFuture, atMost = 1 second)
    val readContinuationThreadId = Await.result(readFuture, atMost = 1 second)

    putContinuationThreadId shouldEqual context1ThreadId
    readContinuationThreadId shouldEqual context2ThreadId
  }

  "Async operations" should "always yield before executing continuations" in {
    implicit val context = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1));

    val c = Channels.create()
    val threadLocal = new ThreadLocal[String]()
    threadLocal.set("testThread")

    val putFuture = c.putAsync("hello").map { x => threadLocal.get() }
    threadLocal.set("beforeReadAsync")
    c.readAsync()
    threadLocal.set("afterReadAsync")

    val putContinuationDynValue = Await.result(putFuture, atMost = 1 second)
    putContinuationDynValue shouldEqual null
  }

}
