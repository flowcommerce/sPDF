package io.flow.spdf

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.io.File
import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future, TimeoutException, blocking}
import scala.sys.process._

class PdfSpec extends AnyWordSpec with Matchers {

  "A Pdf" should {

    "require the executionPath config" in {
      val file = new File("notexecutable")
      val filePath = file.getAbsolutePath

      assertThrows[NoExecutableException] {
        new Pdf(filePath, PdfConfig.default)
      }

      assertThrows[NoExecutableException] {
        Pdf(filePath, PdfConfig.default)
      }

    }

    val page =
      """
        |<html><body><h1>Hello</h1></body></html>
            """.stripMargin

    PdfConfig.findExecutable match {
      case Some(_) =>
        "generate a PDF file from an HTML string" in {
          val file = File.createTempFile("scala.spdf", "pdf")

          val pdf = Pdf(PdfConfig.default)

          pdf.run(page, file)

          Seq("file", file.getAbsolutePath).!! should include("PDF document")
        }

        "generate a PDF file from an HTML string with timeout" in {
          val file = File.createTempFile("scala.spdf", "pdf")

          val pdf = Pdf(PdfConfig.default)

          val p = pdf.prepare(page, file).run() // start asynchronously
          val f = Future(blocking(p.exitValue())) // wrap in Future
          try Await.result(f, Duration(5, TimeUnit.SECONDS)) catch {
            case _: TimeoutException => p.destroy()
          }

          Seq("file", file.getAbsolutePath).!! should include("PDF document")
        }

        "destroy process on timeout" in {
          val file = File.createTempFile("scala.spdf", "pdf")

          val pdf = Pdf(PdfConfig.default)

          val p = pdf.prepare(page, file).run() // start asynchronously
          val f = Future(blocking(p.exitValue())) // wrap in Future
          try Await.result(f, Duration(50, TimeUnit.MILLISECONDS)) catch {
            case _: TimeoutException => p.destroy()
          }

          Seq("file", file.getAbsolutePath).!! should not include "PDF document"
        }

      case None =>
        "Skipping test, missing wkhtmltopdf binary" in { true should equal(true) }
    }


  }

}
