package examples

import concurrent._
import java.util.concurrent.{ CountDownLatch, Executors, TimeUnit }

object computeServer extends App {

  class ComputeServer(n: Int) {

    private trait Job {
      type T
      def task: T
      def ret(x: T): Unit
    }

    private val openJobs = new Channel[Job]()

    private def processor(i: Int) {
      printf("processor %d starting\n", i)
      while (!isDone) {
        val job = openJobs.read
        printf("processor %d read a job\n", i)
        job.ret(job.task)
      }
      printf("processor %d terminating\n", i)
    }

    def submit[A](p: => A): Future[A] = future {
      val reply = new SyncVar[A]()
      openJobs.write {
        new Job {
          type T = A
          def task = p
          def ret(x: A) = reply.put(x)
        }
      }
      reply.get
    }

    val done = new SyncVar[Boolean]
    def isDone = done.isSet && done.get(500).get
    def finish() {
      done.put(true)
      val nilJob =
        new Job {
          type T = Null
          def task = null
          def ret(x: Null) { }
        }
      // unblock readers
      for (i <- 1 to n) { openJobs.write(nilJob) }
    }

    for (i <- 1 to n; f = future { processor(i) }) f onComplete { _ => doneLatch.countDown() }
  }

  val Processors = 2
  implicit val ctx = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2 * Processors))
  val doneLatch = new CountDownLatch(Processors+1)
  val server = new ComputeServer(Processors)

  val f = server.submit(42)
  val g = server.submit(38)
  val h = for (x <- f; y <- g) yield { x + y }
  h onComplete {
    case Right(v) => println(v); windDown()
    case _ => windDown()
  }
  def windDown() {
    server.finish()
    doneLatch.countDown()
  }
  def shutdown() {
    ctx.shutdown()
    ctx.awaitTermination(1, TimeUnit.SECONDS)
  }
  doneLatch.await()
  shutdown()
}
