import scala.reflect.makro.{Context => Ctx}

object Impls {
  def foo(c: Ctx)(x: c.Expr[Int]): c.Expr[Int] = {
    import c.mirror._
    Apply(Select(x.tree, newTermName("$plus")), List(Literal(Constant(1))))
  }

  def bar(c: Ctx)(x: c.Expr[Int]): c.Expr[Int] = {
    import c.mirror._
    Apply(Select(x.tree, newTermName("$plus")), List(Literal(Constant(2))))
  }

  def quux(c: Ctx)(x: c.Expr[Int]): c.Expr[Int] = {
    import c.mirror._
    val body = Apply(Select(x.tree, newTermName("$plus")), List(Literal(Constant(3))))
    Expr[Int](body)
  }
}
