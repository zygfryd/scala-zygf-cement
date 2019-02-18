package zygf.cement.impl

import java.lang.reflect.Method
import java.util.concurrent.locks.ReentrantLock

// TODO: multithreaded tests?

abstract class Placeholder
{
  private val lock: ReentrantLock = new ReentrantLock(false)
  
  def getValue: Object
  
  def startReplacement(): Boolean = {
    lock.lock()
  
    if (lock.getHoldCount > 1) {
      lock.unlock()
      throw new IllegalStateException("Circular initialization detected")
    }
    
    if (getValue ne this) {
      lock.unlock()
      false
    }
    else {
      true
    }
  }
  
  def finishReplacement(): Unit = {
    lock.unlock()
  }
}

class FallbackPlaceholder(val getter: Method) extends Placeholder
{
  override def getValue = getter.invoke(null)
}
