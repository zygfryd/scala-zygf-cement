package zygf.cement

import scala.language.reflectiveCalls
import scala.reflect.macros.blackbox

import zygf.cement.impl.{CementedCallSite, FallbackPlaceholder, InvokeDynamic}

object cement // It's a lowercase object, but it works like a def
{
  def apply[T](constructor: T): T = macro applyImpl[T]
  
  private var is211 = None: Option[Boolean]
  
  def applyImpl[T: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree): c.Tree = {
    import scala.util.Try
    
    is211 match {
      case None =>
        is211 = Some {
          val really_? = c.universe.asInstanceOf[{ def settings: { def isScala211: Boolean
                                                                   def isScala212: Boolean } }]
          val settings = really_?.settings
          
          !Try(settings.isScala212).getOrElse(false) && Try(settings.isScala211).getOrElse(false)
        }
      case _ =>
    }
    
    if (is211.get)
      staticFallbackImpl[T](c)(constructor)
    else
      invokeDynamicImpl[T](c)(constructor)
  }
  
  def invokeDynamicImpl[T: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree): c.Tree = {
    import c.universe._
  
    val T = weakTypeTag[T].tpe
    
    // the static bootstrap method for initializing the callsite via invokedynamic
    val bootstrap = typeOf[CementedCallSite].companion.member(TermName("bootstrap"))
  
    // the Placeholder class, that we need to cast to, to update the callsite's value
    val Placeholder = symbolOf[CementedCallSite#Placeholder]
  
    // locals
    val api = TermName(c.freshName("api"))
    val v = TermName(c.freshName("v"))
  
    q"""{
      val $v = ${InvokeDynamic(c)(bootstrap, Nil, Nil)}
      if (! $v.isInstanceOf[$Placeholder]) {
        $v
      }
      else {
        val $api = $v.asInstanceOf[$Placeholder]
        if ($api.startReplacement()) {
          try { $api.setValue($constructor) }
          finally { $api.finishReplacement() }
        }
        else
          $api.getValue()
      }
    }.asInstanceOf[$T]
    """
  }
  
  def staticFallbackImpl[T: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree): c.Tree = {
    import c.universe._
    
    import scala.reflect.internal.Flags._
    
    val T = weakTypeTag[T].tpe
    
    // the Placeholder class, that we need to cast to, to update the callsite's value
    val Placeholder = symbolOf[FallbackPlaceholder]
    
    // locals
    val api = TermName(c.freshName("api"))
    val v = TermName(c.freshName("v"))
    val temp = TermName(c.freshName("temp"))
    
    val className = TypeName(c.freshName("Cement"))
    val classIdent = Ident(className)
    
    val field = ValDef(Modifiers((STATIC | SYNTHETIC | MUTABLE: Long).asInstanceOf[FlagSet],
                                 typeNames.EMPTY,
                                 Apply(Select(New(Ident(TypeName("volatile"))), termNames.CONSTRUCTOR), Nil) :: Nil),
                       TermName("storage"),
                       Ident(symbolOf[Object]),
                       q"""new ${Placeholder}(classOf[${classIdent}].getMethod("storage"))""")
    
    q"""{
      class $className {
        $field
      }
      
      val $v = ${classIdent}.storage
      if (! $v.isInstanceOf[$Placeholder]) {
        $v
      }
      else {
        val $api = $v.asInstanceOf[$Placeholder]
        if ($api.startReplacement()) {
          try {
            val $temp = $constructor.asInstanceOf[Object]
            ${classIdent}.storage = $temp
            $temp
          }
          finally { $api.finishReplacement() }
        }
        else
          ${classIdent}.storage
      }
    }.asInstanceOf[$T]
    """
  }
}
