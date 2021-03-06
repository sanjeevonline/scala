import scala.tools.partest.ReplTest

object Test extends ReplTest {
  override def extraSettings = "-Xmacros"
  def code = """
    |import scala.reflect.makro.{Context => Ctx}
    |
    |object Impls {
    |  def foo(c: Ctx)(x: c.Expr[Int]) = {
    |    import c.mirror._
    |    val body = Apply(Select(x.tree, newTermName("$plus")), List(Literal(Constant(1))))
    |    Expr[Int](body)
    |  }
    |
    |  def bar(c: Ctx)(x: c.Expr[Int]) = {
    |    import c.mirror._
    |    val body = Apply(Select(x.tree, newTermName("$plus")), List(Literal(Constant(2))))
    |    Expr[Int](body)
    |  }
    |
    |  def quux(c: Ctx)(x: c.Expr[Int]) = {
    |    import c.mirror._
    |    val body = Apply(Select(x.tree, newTermName("$plus")), List(Literal(Constant(3))))
    |    Expr[Int](body)
    |  }
    |}
    |object Macros {
    |  object Shmacros {
    |    def foo(x: Int): Int = macro Impls.foo
    |  }
    |  def bar(x: Int): Int = macro Impls.bar
    |}; class Macros {
    |  def quux(x: Int): Int = macro Impls.quux
    |}
    |
    |import Macros.Shmacros._
    |println(foo(2) + Macros.bar(2) * new Macros().quux(4))
    |""".stripMargin
}