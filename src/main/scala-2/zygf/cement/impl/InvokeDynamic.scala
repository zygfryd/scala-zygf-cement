package zygf.cement.impl

import scala.reflect.internal.SymbolTable
import scala.reflect.macros.blackbox 

// Taken from https://github.com/scala/scala/blob/2.13.x/test/files/run/indy-via-macro-with-dynamic-args/macro_1.scala
// and modified to generate a call to a parameterless function that returns Object

object InvokeDynamic
{
  def apply(c: blackbox.Context)(bootstrapMethod: c.Symbol, bootstrapArgs: List[c.universe.Literal], dynArgs: List[c.Tree]): c.Tree = {
    val symtab = c.universe.asInstanceOf[SymbolTable]
    import symtab._
    val dummySymbol = NoSymbol.newTermSymbol(TermName("result")).setInfo(internal.methodType(Nil, typeOf[java.lang.Object]))
    val bootstrapArgTrees: List[Tree] = Literal(Constant(bootstrapMethod)).setType(NoType) :: bootstrapArgs.asInstanceOf[List[Tree]]
    val result = ApplyDynamic(Ident(dummySymbol).setType(dummySymbol.info), bootstrapArgTrees ::: dynArgs.asInstanceOf[List[Tree]])
    result.setType(dummySymbol.info.resultType)
    result.asInstanceOf[c.Tree]
  }
}
