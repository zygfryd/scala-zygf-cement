package zygf.cement

import scala.quoted._

// TODO: opaque type in 3.0.2

sealed abstract case class Cemented[T](value: T)

object Cemented
{
  /** Wrap a `T` into a [[Cemented!]]`[T]` instance, without invoking any caching magic
    * @tparam T
    */
  def wrap[T](value: T): Cemented[T] = new Cemented[T](value) {}
  
  /** Unwrap instances of [[Cemented!]]`[T]` into `T`s */
  inline def unwrap[T](implicit inline cemented: Cemented[T]): T = cemented.value 
  
  /** Summon an implicit [[Cemented!]]`[T]` */
  inline def get[T](implicit value: Cemented[T]): Cemented[T] = value
  
  /** Generate an implicit instance of [[Cemented!]]`[T]` from an implicit instance of `T`, with caching magic */
  implicit inline def gen[T](implicit inline t: T): Cemented[T] = apply(t)
  
  /** Wrap a `T` into a [[Cemented!]]`[T]` instance, with caching magic */
  inline def apply[T](inline value: T): Cemented[T] = cement(wrap(value))
}
