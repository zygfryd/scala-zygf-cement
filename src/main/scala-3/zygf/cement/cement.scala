package zygf.cement

import scala.quoted._

import zygf.cement.impl.DottyPlaceholder

object cement
{
  inline def apply[T](inline expression: T): T = ${ applyImpl('expression) }
  
  // The Quotes API doesn't provide all we need, so we load some of dotc's classes
  // this is ugly and probably very fragile, but what else is there to do
  private object hack {
    val Symbols = Class.forName("dotty.tools.dotc.core.Symbols")
    val Symbol = Class.forName("dotty.tools.dotc.core.Symbols$Symbol")
    val ClassSymbol = Class.forName("dotty.tools.dotc.core.Symbols$ClassSymbol")
    val SymDenotation = Class.forName("dotty.tools.dotc.core.SymDenotations$SymDenotation")
    val Context = Class.forName("dotty.tools.dotc.core.Contexts$Context")
    val Names = Class.forName("dotty.tools.dotc.core.Names")
    val Name = Class.forName("dotty.tools.dotc.core.Names$Name")
    val tpd = Class.forName("dotty.tools.dotc.ast.tpd")
    val Tree = Class.forName("dotty.tools.dotc.ast.Trees$Tree")
    
    val enteredSymbol = Symbol.getMethod("entered", Context)
    val typeName = Names.getMethod("typeName", classOf[String])
    val termName = Names.getMethod("termName", classOf[String])
    
    private val Symbols_methods = Symbols.getMethods.toList
    val newNormalizedClassSymbol = Symbols_methods.iterator.filter(_.getName == "newNormalizedClassSymbol").next()
    val newNormalizedClassSymbol_default5 = Symbols.getMethod("newNormalizedClassSymbol$default$5")
    val newNormalizedClassSymbol_default6 = Symbols.getMethod("newNormalizedClassSymbol$default$6")
    val newNormalizedClassSymbol_default7 = Symbols.getMethod("newNormalizedClassSymbol$default$7")
    val newNormalizedClassSymbol_default8 = Symbols.getMethod("newNormalizedClassSymbol$default$8")
    val newNormalizedClassSymbol_default9 = Symbols.getMethod("newNormalizedClassSymbol$default$9")
    val newDefaultConstructor = Symbols_methods.iterator.filter(_.getName == "newDefaultConstructor").next()
    
    val tpd_DefDef = tpd.getMethod("DefDef", Symbol, Tree, Context)
    val tpd_DefDef_default2 = tpd.getMethod("DefDef$default$2")
    val tpd_ClassDef = tpd.getMethods.iterator.filter(_.getName == "ClassDef").next()
    val tpd_Select = tpd.getMethod("Select", Tree, Name, Context)
  
    val denotOfSymbol = Symbol.getMethod("denot", Context)
    val typeRefOfDenot = SymDenotation.getMethod("typeRef", Context)
    val addAnnotation = SymDenotation.getMethod("addAnnotation", ClassSymbol, Context)
  }
  
  def applyImpl[T](expression: Expr[T])(using Quotes, Type[T]): Expr[T] = {
    import quotes.reflect._
  
    val context = quotes.getClass.getMethod("ctx").invoke(quotes)
    
    val className = hack.typeName.invoke(null, "CementSite")
    val classSym = hack.newNormalizedClassSymbol
      .invoke(null,
              Symbol.spliceOwner, // parent
              className, // name
              Flags.Final | Flags.Synthetic, // flags
              List(TypeRepr.of[Object]), // supertypes
              hack.newNormalizedClassSymbol_default5.invoke(null),
              hack.newNormalizedClassSymbol_default6.invoke(null),
              hack.newNormalizedClassSymbol_default7.invoke(null),
              hack.newNormalizedClassSymbol_default8.invoke(null),
              hack.newNormalizedClassSymbol_default9.invoke(null),
              context).asInstanceOf[Symbol]
    hack.enteredSymbol.invoke(classSym, context)
    
    // symbol for our null field
    val valSym = Symbol.newVal(classSym, // parent
                               "storage", // name
                               TypeRepr.of[Object], // type
                               Flags.Mutable | Flags.JavaStatic | Flags.Static | Flags.Synthetic, // flags
                               Symbol.noSymbol /* privateWithin */)
    hack.addAnnotation.invoke(hack.denotOfSymbol.invoke(valSym, context), Symbol.requiredClass("scala.volatile"), context)
    hack.enteredSymbol.invoke(valSym, context)
    
    val valDef = ValDef(valSym, None)
    
    // argument-less empty constructor
    val conSym = hack.newDefaultConstructor.invoke(null, classSym, context)
    hack.enteredSymbol.invoke(conSym, context)

    val conDef = hack.tpd_DefDef.invoke(null, conSym, hack.tpd_DefDef_default2.invoke(null), context)
  
    val fieldSym = classSym.memberField("storage")
    
    // static constructor
    val staticSym = Symbol.newMethod(classSym,
                                     "<clinit>",
                                     MethodType(Nil)(_ => Nil, _ => TypeRepr.of[Unit]),
                                     Flags.Synthetic | Flags.Method | Flags.Private | Flags.Static,
                                     Symbol.noSymbol)
    hack.enteredSymbol.invoke(staticSym, context)
  
    // CementSite.storage = new zygf.cement.impl.DottyPlaceholder(classOf[CementSite])
    val thisType  = hack.typeRefOfDenot.invoke(hack.denotOfSymbol.invoke(classSym, context), context).asInstanceOf[TypeRepr]
    val thisClass = Literal(ClassOfConstant(thisType))
    val fieldInit = Apply(Select.unique(New(TypeIdent(Symbol.classSymbol("zygf.cement.impl.DottyPlaceholder"))), "<init>"),
                          thisClass :: Nil)
    val staticDef = hack.tpd_DefDef.invoke(null, staticSym, Assign(Ref(fieldSym), fieldInit), context)
    
    val classDef = hack.tpd_ClassDef.invoke(null,
                                            classSym, // symbol
                                            conDef, // constructor
                                            List(valDef, staticDef), // body
                                            Nil, // super args
                                            context).asInstanceOf[ClassDef]
    
    val fieldRef = Ref(fieldSym).asExprOf[Object]
    
    val block = '{
      val v = ${fieldRef}
      if (!v.isInstanceOf[DottyPlaceholder]) {
        v.asInstanceOf[T]
      }
      else {
        val api = v.asInstanceOf[DottyPlaceholder]
        if (api.startReplacement()) {
          try {
            val temp: T = ${expression}
            ${Assign(Ref(fieldSym), '{temp.asInstanceOf[Object]}.asTerm).asExpr}
            temp
          }
          finally {
            api.finishReplacement()
          }
        }
        else {
          ${fieldRef}.asInstanceOf[T]
        }
      }
    }
    
    Block(List(classDef), block.asTerm).asExprOf[T]
  }
}
