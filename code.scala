package org.hablapps.talk

/**
References: Generic Java. http://homepages.inf.ed.ac.uk/wadler/gj/
*/
object AllRoadsLeadToLambdaWorld{
  
  object OnceUponATime{

    /** 
     Let's say that we want to program a list of integers; we could make it so
     */
    abstract class IntList{
      def head: Int 
    }
    
    object IntList{
      case class Empty() extends IntList{
        def head = throw new Exception("No such element")
      }
      case class Node(i: Int, l: IntList) extends IntList{
        def head = i
      }
    }
    
    /**
     And use it in the following way:
     */
    val intList: IntList = IntList.Node(1,IntList.Node(2,IntList.Empty()))
    val int: Int = intList.head

    /**
     Now, suppose that you also want a list of strings, besides the above list of integers. 
     There was a time in which we had to copy & paste the above code, and replace `Int`s
     with `String`s throughout the code.
     */
    
    abstract class StringList{
      def head: String 
    }

    object StringList{
      case class Empty() extends StringList{
        def head = throw new Exception("No such element")
      }
      case class Node(i: String, l: StringList) extends StringList{
        def head = i
      }
    }
    
    val strList: StringList = StringList.Node("1",StringList.Node("2",StringList.Empty()))
    val str: String = strList.head

  }

  object JavaToTheRescue{

    /** 
    With the advent of Java, this was no longer necessary. With the use of subtypes
    and the `Any` type, we could write the code once and for all.
    */

    abstract class List{
      def head: Any
    }

    object List{
      case class Empty() extends List{
        def head = throw new Exception("No such element")
      }
      case class Node(i: Any, l: List) extends List{
        def head = i
      }
    }

    /**
    We can create and use a list of integers as follows.
    */
    val intList: List = List.Node(1,List.Node(2,List.Empty()))
    val int: Any = intList.head
    val intAsTrueInt = intList.head.asInstanceOf[Int]
    
    /**
    Same with a list of strings.
    */
    val strList: List = List.Node("1",List.Node("2",List.Empty()))
    val str: String = strList.head.asInstanceOf[String]
    
  }

  object Really_To_The_Rescue_? {

    /**
    As you may have observed, while it's true that now we did only have to 
    write our code once, we have to be extra careful when retrieving 
    information from the list. If you get the type information wrong, bad
    things will happen ... at runtime!
    */
    import JavaToTheRescue._

    // This code compiles!
    val str: String = intList.head.asInstanceOf[String]

    /**
    Note that the first version did not suffer from this problem, i.e.
    the following code doesn't compile.
    */
    // val str2: String = OnceUponATime.intList.head

    /**
    And we could even mix elements of different types in a single list,
    which we may not want it at all. In that case, we wouldn't want this code to
    compile, whereas it really does.
    */
    val strangerList: List = List.Node(1, List.Node("2", List.Empty()))

  }

  object GenericsToTheRescue{

    /**
    Generic Java, i.e. parametric polymorphism, did actually solve the problem.
    */

    abstract class List[A]{
      def head: A
    }

    object List{
      case class Empty[A]() extends List[A]{
        def head = throw new Exception("No such element")
      }
      case class Node[A](i: A, l: List[A]) extends List[A]{
        def head = i
      }
    }

    /* Create and use a list of integers */
    val intList: List[Int] = List.Node[Int](1,List.Node[Int](2,List.Empty[Int]()))
    val int: Int = intList.head
    
    /* Create and use a list of strings */
    val strList: List[String] = List.Node("1",List.Node("2",List.Empty()))
    val str: String = strList.head

    /**
    Besides, our code is wholly type safe, i.e. the following code doesn't compile.
    */
    // val str2: String = intList.head

    /**
    We can still create heterogeneous lists, though.
    */
    val strangerList: List[Any] = List.Node(1, List.Node("1", List.Empty()))

    /**
    We can now define our integer and string lists in a modular way. 
    */
    type IntList = List[Int]
    type StringList = List[String]
  }

  object OnAPIDesign{

    /** 
    Let's say that we want to implement an echo program. We may go like this.
    */
    object Monolithic{
      
      def echo(): String = {
        val msg: String = scala.io.StdIn.readLine()
        println(msg)
        msg
      }

    }

    /**
    But this program mixes two concerns: the logic of our program, and the actual
    IO instructions. In order to modularise this program we use APIs, i.e. interfaces.
    For instance, a simple interface for reading and writing strings might go like this.
    */
    
    trait IO{
      def read(): String
      def write(msg: String): Unit
    }

    /**
    The purpose of this interface is encapsulating the actual way in 
    which strings will be read and written in the API implementation, in such a way that
    client code will be kept clean and decoupled from any possible implementation 
    or infrastructural concern.
    */

    def echo(io: IO): String = {
      val msg: String = io.read()
      io.write(msg)
      msg
    }

    /**
    We can reuse our program as is, regardless of the actual way in which 
    strings are read and written. Thus, you may think of your API as the
    wall that protects us from the white walkers ... For instance, if we want
    to implement our IO API using the console, no trace of it will be found
    at the `echo` program.
    */
    
    object ConsoleIO extends IO{
      def read(): String = scala.io.StdIn.readLine()
      def write(msg: String): Unit = Console.println(msg)
    }

    /**
    In order to reuse our program with a particular instance of the IO API,
    we simply pass it as an argument.
    */
    
    object Modular{
      def echoConsole() = echo(ConsoleIO)
    }

  }

  // Will be needed later
  import scala.concurrent.{Await, Future, ExecutionContext, duration}
  import ExecutionContext.Implicits.global, duration._

  object EvilIsBack{
  
    /**
    But, is that true? Is it a true wall? Well, it happens that this wall
    has many leaks ... For instance, let's say that we need an implementation
    which uses asynchronous features. This may happen, for instance, if we
    are programming a storage API using slick 2.0, and then we want to upgrade
    to slick 3.0. We'll simulate that change here.
    */
    
    object WrongAsyncIO extends OnAPIDesign.IO{

      def read(): String = {
        val futureMsg: Future[String] = ???
        Await.result(futureMsg, 1 second)
      }
      def write(msg: String): Unit = ???
    }

    /**
    But we can't really block if we are to implement a true asynchronous API.
    So, we are forced to change the signature of the IO API. 
    */
    
    trait IO{
      def read(): Future[String]
      def write(msg: String): Future[Unit]
    }

    /** 
    Which allow us to implement the API in true asynchronous terms.
    */

    object AsyncIO extends IO{
      def read(): Future[String] = Future(scala.io.StdIn.readLine())
      def write(msg: String): Future[Unit] = Future(println(msg))
    }

    /**
    However, this has a very high cost, namely we have to change *all* the programs
    that we implemented over the API, as well as all the API implementations, of course.
    */

    def echo(io: IO): Future[String] =
      io.read().flatMap{ msg => 
        io.write(msg).flatMap{ _ => 
          Future.successful(msg)
        }
      }
  }

  object RawLambdaWorld{

    /**
    We abstract away the differences between OnAPIDesign.IO and EvilIsBack.IO using
    type constructor classes.
    */
    trait IO[P[_]]{
      def read(): P[String]
      def write(msg: String): P[Unit]

      // Combinators
      def flatMap[A,B](p1: P[A])(f: A => P[B]): P[B]
      def returns[A](a: A): P[A]
    }

    /**
    This API is flexible (i.e. generic) enough to accommodate all types of specific
    APIs: synchronous, asynchronous, with explicit error reporting, state-based, etc.
    */

    type Id[T]=T

    object ConsoleIO extends IO[Id]{
      def read(): String = scala.io.StdIn.readLine()
      def write(msg: String): Unit = Console.println(msg)
      
      // combinators
      def flatMap[A,B](p1: A)(f: A => B): B = f(p1)
      def returns[A](a: A): A = a
    }

    object AsynchIO extends IO[Future]{
      def read(): Future[String] = Future(scala.io.StdIn.readLine())
      def write(msg: String): Future[Unit] = Future(Console.println(msg))
      
      // combinators 
      def flatMap[A,B](p1: Future[A])(f: A => Future[B]): Future[B] = 
        p1.flatMap(f)
      def returns[A](a: A): Future[A] = 
        Future.successful(a)
    }

    /**
    Our programs, however, will be less concise and clear than before.
    */

    def echo[P[_]](io: IO[P]): P[String] = 
      io.flatMap(io.read()){ msg => 
        io.flatMap(io.write(msg)){ _ => 
          io.returns(msg)
        }
      }

    /**
    But our `echo` program is now truly generic!
    */

    def echoConsole: String = echo(ConsoleIO)

    def echoAsynch: Future[String] = echo(AsynchIO)

  }

  object MonadicLambdaWorld{
    
    /**
    We have two problems left: 1) the imperative combinators have nothing to do with the
    IO API; if we have to implement an API for file system manipulation, logging, etc., we
    will write the very same combinators, once and again; 2) we really need to 
    be able to write our programs in a more concise and understandable way. In this 
    module we tackle the first problem.
    */

    /** 
    The first problem can be solved easily by isolating the imperative combinators
    in their own API. This API is actually what is commonly known as a Monad.
    */

    trait IO[P[_]]{
      def read(): P[String]
      def write(msg: String): P[Unit]
    }

    trait Monad[P[_]]{
      def flatMap[A,B](p1: P[A])(f: A => P[B]): P[B]
      def returns[A](a: A): P[A]
    }

    /**
    Instances of the IO API will deal now just with the implementation of the 
    read and write instructions. Instances of the monad API deal with the 
    implementation of the monadic (i.e. imperative) comabinators 
    */

    type Id[T]=T

    object ConsoleIO extends IO[Id]{
      def read(): String = scala.io.StdIn.readLine()
      def write(msg: String): Unit = Console.println(msg)
    }

    object IdMonad extends Monad[Id]{
      def flatMap[A,B](p1: A)(f: A => B): B = f(p1)
      def returns[A](a: A): A = a
    }

    object AsynchIO extends IO[Future]{
      def read(): Future[String] = Future(scala.io.StdIn.readLine())
      def write(msg: String): Future[Unit] = Future(Console.println(msg))
    }

    object FutureMonad extends Monad[Future]{
      def flatMap[A,B](p1: Future[A])(f: A => Future[B]): Future[B] = 
        p1.flatMap(f)
      def returns[A](a: A): Future[A] = 
        Future.successful(a)
    }

    /**
    In order to implement our programs we have to pass now two instances: the
    IO API and the monadic API. But they are as verbose as before.
    */

    def echo[P[_]](io: IO[P], monad: Monad[P]): P[String] = 
      monad.flatMap(io.read()){ msg => 
        monad.flatMap(io.write(msg)){ _ => 
          monad.returns(msg)
        }
      }
  }


  object SweetLambdaWorld{
    
    /**
    In order to improve the conciseness and readability of our programs we will
    exploit two major features of Scala: context bounds and for-comprehensions.
    */
    import MonadicLambdaWorld.{Monad, IO}

    object IO{
      object Syntax{
        def read[P[_]]()(implicit IO: IO[P]) = IO.read()
        def write[P[_]](msg: String)(implicit IO: IO[P]) = IO.write(msg)
      }
    }

    object Monad{
      object Syntax{
        implicit class FlatMapOps[P[_],A](p: P[A])(implicit M: Monad[P]){
          def flatMap[B](f: A => P[B]): P[B] = M.flatMap(p)(f)
          def map[B](f: A => B): P[B] = M.flatMap(p)(f andThen M.returns)
        }
        def returns[P[_],A](a: A)(implicit M: Monad[P]) = M.returns(a)
      }
    }
    
    /**
    Using context bounds alone, we managed to simple the signature of our 
    programs.
    */
    import IO.Syntax._, Monad.Syntax._

    def echo[P[_]: IO: Monad]: P[String] = 
      read() flatMap { msg => 
        write(msg) flatMap { _ => 
          returns(msg)
        }
      }

    /** 
    Using for-comprehensions, we are now able to write our program with the 
    look-and-feel of a conventional imperative program.
    */
    def echo2[P[_]: IO: Monad]: P[String] = for{
      msg <- read()
      _ <- write(msg)
    } yield msg

  }
}