/* NSC -- new Scala compiler
 * Copyright 2005-2011 LAMP/EPFL
 * @author  Paul Phillips
 */

package scala.reflect

import java.lang.reflect.{ InvocationTargetException, UndeclaredThrowableException }

/** A few java-reflection oriented utility functions useful during reflection bootstrapping.
 */
object ReflectionUtils {
  // Unwraps some chained exceptions which arise during reflective calls.
  def unwrapThrowable(x: Throwable): Throwable = x match {
    case  _: InvocationTargetException |      // thrown by reflectively invoked method or constructor
          _: ExceptionInInitializerError |    // thrown when running a static initializer (e.g. a scala module constructor)
          _: UndeclaredThrowableException |   // invocation on a proxy instance if its invocation handler's `invoke` throws an exception
          _: ClassNotFoundException |         // no definition for a class instantiated by name
          _: NoClassDefFoundError             // the definition existed when the executing class was compiled, but can no longer be found
            if x.getCause != null =>
              unwrapThrowable(x.getCause)
    case _ => x
  }
  // Transforms an exception handler into one which will only receive the unwrapped
  // exceptions (for the values of wrap covered in unwrapThrowable.)
  def unwrapHandler[T](pf: PartialFunction[Throwable, T]): PartialFunction[Throwable, T] = {
    case ex if pf isDefinedAt unwrapThrowable(ex)   => pf(unwrapThrowable(ex))
  }

  def defaultReflectionClassLoader() = {
    // say no to non-determinism of mirror classloaders
    // default classloader will be instantiated using current system classloader
    // if you wish so, you can rebind it by setting ``mirror.classLoader'' to whatever is necessary
//    val cl = Thread.currentThread.getContextClassLoader
//    if (cl == null) getClass.getClassLoader else cl
//    cl
    getClass.getClassLoader
  }

  def singletonInstance(cl: ClassLoader, className: String): AnyRef = {
    val name = if (className endsWith "$") className else className + "$"
    val clazz = java.lang.Class.forName(name, true, cl)
    val singleton = clazz getField "MODULE$" get null
    singleton
  }

  // Retrieves the MODULE$ field for the given class name.
  def singletonInstanceOpt(cl: ClassLoader, className: String): Option[AnyRef] =
    try Some(singletonInstance(cl, className))
    catch { case _: ClassNotFoundException  => None }

  def invokeFactory(cl: ClassLoader, className: String, methodName: String, args: AnyRef*): AnyRef = {
    val singleton = singletonInstance(cl, className)
    val method = singleton.getClass.getMethod(methodName, classOf[ClassLoader])
    method.invoke(singleton, args: _*)
  }

  def invokeFactoryOpt(cl: ClassLoader, className: String, methodName: String, args: AnyRef*): Option[AnyRef] =
    try Some(invokeFactory(cl, className, methodName, args: _*))
    catch { case _: ClassNotFoundException  => None }
}
