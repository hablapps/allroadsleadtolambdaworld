name := "hablapps"

scalaVersion := "2.11.8"

scalacOptions ++= Seq(
  "-language:implicitConversions",
  "-unchecked",
  "-deprecation",
  "-feature",
  "-language:postfixOps",
  "-language:higherKinds")

initialCommands in console := """
  |import org.hablapps.talk.AllRoadsLeadToLambdaWorld._
  |import OnceUponATime.{StringList => SL, IntList => IL, _}
  |import JavaToTheRescue._
  |""".stripMargin
