import sbt._, Keys._

object build extends Build {

  val buildSettings = Seq(
    scalaVersion := "2.11.0",
    scalacOptions ++= Seq("-deprecation", "-language:_"),
    licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php"))
  )

  lazy val root = Project(
    "root", file(".")
  ).settings(
    buildSettings: _*
  ).aggregate(example, argonautMacro)

  lazy val example = Project(
    "example", file("example")
  ).settings(
    buildSettings: _*
  ).dependsOn(argonautMacro)

  lazy val argonautMacro = Project(
    "argonaut-macro",
    file("macros")
  ).settings(
    buildSettings: _*
  ).settings(
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    libraryDependencies ++= (
      ("io.argonaut" % "argonaut_2.11.0-RC1" % "6.0.3") :: // TODO Scala 2.11.0 final
      Nil
    )
  )
}

