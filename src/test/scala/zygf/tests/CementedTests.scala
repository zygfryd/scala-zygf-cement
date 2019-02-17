package zygf.tests

import zygf.cement.Cemented

class CementedTests extends TestSuite
{
  testNumEval("can't manufacture stuff out of thin air", 0) { _ =>
    assertDoesNotCompile("implicitly[Cemented[Symbol]]")
  }
  
  testNumEval("works per implicit binding", 4) { f =>
    implicit def cem: Symbol = f('foo).asInstanceOf[Symbol]
    implicitly[Cemented[Symbol]]
    implicitly[Cemented[Symbol]]
    Cemented.get[Symbol]
    Cemented.get[Symbol]
  }
  
  testNumEval("but caches for 1 binding", 1) { f =>
    implicit def cem: Symbol = f('foo).asInstanceOf[Symbol]
    (1 to 3).foreach { _ =>
      implicitly[Cemented[Symbol]]
    }
  }
  
  testNumEval("without it, there's no caching", 3) { f =>
    implicit def cem: Symbol = f('foo).asInstanceOf[Symbol]
    (1 to 3).foreach { _ =>
      implicitly[Symbol]
    }
  }
  
  testNumEval("apply is magic", 1) { f =>
    implicit def cem: Cemented[Symbol] = Cemented { f('foo).asInstanceOf[Symbol] }
    (1 to 3).foreach { _ =>
      Cemented.get[Symbol]
    }
  }
  
  testNumEval("wrap is not magic", 3) { f =>
    implicit def cem: Cemented[Symbol] = Cemented.wrap { f('foo).asInstanceOf[Symbol] }
    (1 to 3).foreach { _ =>
      Cemented.get[Symbol]
    }
  }
  
  {
    testNumEval("README example", 2) { eval =>
      import org.apache.logging.log4j._
      import zygf.cement.Cemented
      
      object Logging
      {
        implicit def makeAutoLogger(implicit scope: sourcecode.FullName): Logger = {
          eval(null)
          LogManager.getLogger(scope.value)
        }
        
        def autoLogger(implicit logger: Cemented[Logger]) = logger.value
      }
      
      import Logging._
      
      (1 to 3).foreach { _ =>
        autoLogger.info("foo")
        autoLogger.info("bar")
      }
    }
  }
}
