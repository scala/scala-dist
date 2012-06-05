package examples

import language.implicitConversions

import concurrent._
import java.util.concurrent.{ CountDownLatch, Executors, TimeUnit }

object computeServer {

  class ComputeServer(n: Int, completer: Either[Throwable,Unit] => Unit)(implicit ctx: ExecutionContext) {

    private trait Job {
      type T
      def task: T
      def ret(x: T): Unit
      def err(x: Throwable): Unit
    }

    private val openJobs = new Channel[Job]()

    private def processor(i: Int) {
      printf("processor %d starting\n", i)
      // simulate failure in faulty #3
      if (i == 3) throw new IllegalStateException("processor %d: Drat!" format i)
      while (!isDone) {
        val job = openJobs.read
        printf("processor %d read a job\n", i)
        try job ret job.task
        catch {
          case x => job err x
        }
      }
      printf("processor %d terminating\n", i)
    }

    def submit[A](p: => A): Future[A] = future {
      val reply = new SyncVar[Either[Throwable, A]]()
      openJobs.write {
        new Job {
          type T = A
          def task = p
          def ret(x: A) = reply.put(Right(x))
          def err(x: Throwable) = reply.put(Left(x))
        }
      }
      reply.get match {
        case Right(x) => x
        case Left(x) => throw x
      }
    }

    val done = new SyncVar[Boolean]
    def isDone = done.isSet && done.get
    def finish() {
      done.put(true)
      val nilJob =
        new Job {
          type T = Null
          def task = null
          def ret(x: Null) { }
          def err(x: Throwable) { }
        }
      // unblock readers
      for (i <- 1 to n) { openJobs.write(nilJob) }
    }

    // You can, too! http://www.manning.com/suereth/
    def futured[A,B](f: A => B): A => Future[B] = { in => future(f(in)) }
    def futureHasArrived(f: Future[Unit]) = f onComplete completer

    1 to n map futured(processor) foreach futureHasArrived

    // Until your book arrives in the mail, or until your Kindle arrives in the mail
    //for (i <- 1 to n; f = future(processor(i))) f onComplete completer
  }

  def main(args: Array[String]) {
    def usage(msg: String = "scala examples.computeServer <n>"): Nothing = {
      println(msg)
      sys.exit(1)
    }
    val avail = Runtime.getRuntime.availableProcessors
    val default = 4 min avail
    def num(s: String): Int = try { s.toInt.min(avail) } catch { case _ => usage("Bad number "+ s) }
    if (args.length > 1) usage()
    val numProcessors = args.headOption.map(num).getOrElse(default)
    if (numProcessors < 1) usage(""+ numProcessors +" processors doesn't sound very useful. Try between one and "+ avail)

    // Factor of two because this example demonstrates gross consumption of threads. Don't try this at home.
    implicit val ctx = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2 * numProcessors))
    val numResults = 2
    val doneLatch = new CountDownLatch(numProcessors + numResults)
    def counting[A](op: =>A): A = {
      val a = op
      doneLatch.countDown()
      a
    }
    def completer(e: Either[Throwable, Unit]) {
      counting(e.left foreach (x => println("Processor terminated in error: "+ x.getMessage)))
    }
    val server = new ComputeServer(numProcessors, completer _)

    trait CountingCompletion[A] {
      def onCountingComplete[B](f: (Either[Throwable, A]) => B)(implicit x: ExecutionContext)
    }
    implicit def futureToCounting[A](future: Future[A]): CountingCompletion[A] = new CountingCompletion[A] {
      def onCountingComplete[B](f: (Either[Throwable, A]) => B)(implicit x: ExecutionContext) =
        counting(future onComplete f)
    }

    def dbz = 1/0
    val k = server.submit(dbz)
    k onCountingComplete {
      case Right(v) => println("k returned? "+ v)
      case Left(v) => println("k failed! "+ v)
    }

    val f = server.submit(42)
    val g = server.submit(38)
    val h = for (x <- f; y <- g) yield { x + y }
    h onCountingComplete {
      case Right(v) => println(v); windDown()
      case _ => windDown()
    }
    def windDown() {
      server.finish()
    }
    def shutdown() {
      ctx.shutdown()
      ctx.awaitTermination(1, TimeUnit.SECONDS)
    }
    doneLatch.await()
    shutdown()
  }
}
