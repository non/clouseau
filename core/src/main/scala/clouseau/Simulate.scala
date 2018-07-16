package clouseau

/**
 * 
 */
object Simulate {

  /**
   * Use simulation to estimate the impact of code on heap memory.
   *
   * This method takes a snapshot of the heap size before running the
   * `body` thunk. Then it runs the thunk, and takes another snapshot,
   * and returns the size difference.
   *
   * The resulting value can be positive (the thunk allocated more
   * memory than it freed) or negative (it freed more memory than it
   * allocated). This method is possibly less accurate than Calculate,
   * but allows the caller to measure the impact of side-effects and
   * other global objects.
   *
   * The approach here is based on an article by Vlad Roubtsov:
   *
   * https://www.javaworld.com/article/2077496/testing-debugging/java-tip-130--do-you-know-your-data-size-.html
   */
  def sizeOf[A](body: => A): Long = {
    // warm up, initialize the stuff we're using.
    Math.round(3.4)
    runGc()

    // get the initial size of the heap
    val heapBefore = runGc()

    // evaluate the thunk, and then get the size of the heap
    var subject: A = body
    val heapAfter = runGc()

    // free the subject to suppress a warning, and calculate the size
    // difference we observed.
    subject = null.asInstanceOf[A]
    heapAfter - heapBefore
  }

  final val MaxTries = 1000
  final val NumGcs = 4

  final private val runtime: Runtime =
    Runtime.getRuntime()

  def usedMemory(): Long =
    runtime.totalMemory() - runtime.freeMemory()

  def runGc(): Long = {
    var i = 0
    while (i < NumGcs) {
      tryGc()
      i += 1
    }
    runtime.totalMemory() - runtime.freeMemory()
  }

  def tryGc(): Unit = {
    var usedMem1 = usedMemory()
    var usedMem2 = Long.MaxValue

    var i = 0
    while ((usedMem1 < usedMem2) && (i < MaxTries)) {
      runtime.runFinalization()
      runtime.gc()
      Thread.`yield`()
      usedMem2 = usedMem1
      usedMem1 = usedMemory()
      i += 1
    }
  }

}
