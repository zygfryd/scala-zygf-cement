package zygf.cement

import scala.reflect.macros.blackbox

sealed abstract case class Cemented[T](value: T)

object Cemented
{
  /** Wrap a `T` into a [[Cemented!]]`[T]` instance, without invoking any caching magic
    * @tparam T
    */
  def wrap[T](value: T): Cemented[T] = new Cemented[T](value) {}
  
  /** Unwrap instances of [[Cemented!]]`[T]` into `T`s */
  def unwrap[T](implicit cemented: Cemented[T]): T = cemented.value
  
  /** Summon an implicit [[Cemented!]]`[T]` */
  def get[T](implicit value: Cemented[T]): Cemented[T] = value
  
  /** Generate an implicit instance of [[Cemented!]]`[T]` from an implicit instance of `T`, with caching magic */
  implicit def gen[T]: Cemented[T] = macro genImpl[T]
  
  def genImpl[T: c.WeakTypeTag](c: blackbox.Context): c.Tree = {
    import c.universe._
    
    val t = weakTypeTag[T].tpe
  
    c.inferImplicitValue(t) match {
      case EmptyTree =>
        // not found - so expand to implicitly[], so it fails and reports the right error 
        applyImpl[T](c)(q"implicitly[$t]")
      case inferred =>
        // let's fill in the implicit ourselves to save some bytecode by not emitting a call to implicitly[]
        applyImpl[T](c)(inferred)
    }
  }
  
  /** Wrap a `T` into a [[Cemented!]]`[T]` instance, with caching magic */
  def apply[T](value: T): Cemented[T] = macro applyImpl[T]
  
  def applyImpl[T: c.WeakTypeTag](c: blackbox.Context)(value: c.Tree): c.Tree = {
    import c.universe._
    val static = symbolOf[Cemented[_]].companion
    cement.applyImpl[Cemented[T]](c)(q"$static.wrap($value)")
  }
}
