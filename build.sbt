name := "sPDF"

description := "Create PDFs using plain old HTML+CSS. Uses wkhtmltopdf on the back-end which renders HTML using Webkit."

licenses := Seq(
  ("MIT", url("http://opensource.org/licenses/MIT"))
)

organization := "io.flow"

scalaVersion := "2.13.13"

enablePlugins(GitVersioning)
git.useGitDescribe := true

coverageDataDir := file("target/scala-2.13")
coverageHighlighting := true
coverageFailOnMinimum := true
coverageMinimumStmtTotal := 85
coverageMinimumBranchTotal := 85

lazy val allScalacOptions = Seq(
  "-feature",
  "-Xfatal-warnings",
  "-unchecked",
  "-Xcheckinit",
  "-Xlint:adapted-args",
  "-Ypatmat-exhaust-depth", "100", // Fixes: Exhaustivity analysis reached max recursion depth, not all missing cases are reported.
  "-Wconf:src=generated/.*:silent",
  "-Wconf:src=target/.*:silent", // silence the unused imports errors generated by the Play Routes
)

Test / fork := true

libraryDependencies ++= Seq(
  "org.mockito" %% "mockito-scala-scalatest" % "1.17.31" % Test,
  "org.scalatest" %% "scalatest" % "3.2.18" % Test,
  "org.scala-lang.modules" %% "scala-xml" % "2.3.0",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2",
)

resolvers += "Artifactory" at "https://flow.jfrog.io/flow/libs-release/"

publishTo := {
  val host = "https://flow.jfrog.io/flow"
  if (isSnapshot.value) {
    Some("Artifactory Realm" at s"$host/libs-snapshot-local;build.timestamp=" + new java.util.Date().getTime)
  } else {
    Some("Artifactory Realm" at s"$host/libs-release-local")
  }
}

credentials += Credentials(
  "Artifactory Realm",
  "flow.jfrog.io",
  System.getenv("ARTIFACTORY_USERNAME"),
  System.getenv("ARTIFACTORY_PASSWORD")
)

scalacOptions ++= allScalacOptions
