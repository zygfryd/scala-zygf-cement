package zygf.cement

import scala.reflect.macros.blackbox

import zygf.cement.impl.{CementedCallSite, InvokeDynamic}

object cement // It's a lowercase object, but it works like a def
{
  def apply[T](constructor: T): T = macro applyImpl[T]
  
  def applyImpl[T: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree): c.Tree = {
    import c.universe._
    
    val T = weakTypeTag[T].tpe
  
    // the static bootstrap method for initializing the callsite via invokedynamic
    val bootstrap = typeOf[CementedCallSite].companion.member(TermName("bootstrap"))
    
    // the Placeholder class, that we need to cast to, to update the callsite's value
    val Placeholder = symbolOf[CementedCallSite#Placeholder]
    
    // locals
    val lock = TermName(c.freshName("lock"))
    val api = TermName(c.freshName("api"))
    val v = TermName(c.freshName("v"))
    
    q"""{
      val $v = ${InvokeDynamic(c)(bootstrap, Nil, Nil)}
      if (! $v.isInstanceOf[$Placeholder]) {
        $v
      }
      else {
        val $api = $v.asInstanceOf[$Placeholder]
        val $lock = $api.startUpdate()
        if ($lock ne null) {
          try { $api.setValue($constructor) }
          finally { $api.finishUpdate($lock) }
        }
        else
          $api.getValue()
      }
    }.asInstanceOf[$T]
    """
  }
}
