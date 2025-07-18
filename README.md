# sPDF #

sPDF ( pronounced _speedy-f_ ) is a Scala library that makes it super easy to create complex PDFs from plain old HTML, CSS and Javascript.

On the backend it uses [wkhtmltopdf](http://wkhtmltopdf.org) which renders HTML using Webkit.

__sPDF__ is heavily inspired by Ruby's [PdfKit](https://github.com/pdfkit/pdfkit) gem.

The main features of __sPDF__ are:

* full support of `wkhtmltopdf` extended parameters (see the source of the `PdfConfig` trait)
* can read HTML from several sources: `java.io.File`, `java.io.InputStream`, `java.net.URL`, `scala.xml.Elem`, and `String`
* can write PDFs to `File` and `OutputStream`

The source HTML can reference to images and stylesheet files as long as the URLs point to the absolute path of the source file.
It's also possible to embed javascript code in the pages, `wkhtmltopdf` will wait for the document ready event before generating the PDF.

## Installation ##

Add the following to your sbt build for Scala 2.10, 2.11, 2.12 and 2.13:

```scala
libraryDependencies += "io.flow" %% "spdf" % "1.4.3"
```

## Usage ##

```scala
import io.flow.spdf._

// Create a new Pdf converter with a custom configuration
// run `wkhtmltopdf --extended-help` for a full list of options
val pdf = Pdf(new PdfConfig {
  orientation := Landscape
  pageSize := "Letter"
  marginTop := "1in"
  marginBottom := "1in"
  marginLeft := "1in"
  marginRight := "1in"
})

val page = <html><body><h1>Hello World</h1></body></html>

// Save the PDF generated from the above HTML into a Byte Array
val outputStream = new ByteArrayOutputStream
pdf.run(page, outputStream)

// Save the PDF of Google's homepage into a file
pdf.run(new URL("http://www.google.com"), new File("google.pdf"))
```

If you want to use sPDF in headless mode on debian you'll need to call to wkhtmltopdf through a virtualizer like xvfb-run.
This is because wkhtmltopdf does not support running in headless mode on debian through the apt package. To use sPDF
in this kind of environment you need to use WrappedPdf instead of Pdf. For Example:

```scala
// Create a new Pdf converter with a custom configuration
// run `wkhtmltopdf --extended-help` for a full list of options
val pdf = WrappedPdf(Seq("xvfb-run", "wkhtmltopdf"), new PdfConfig {
  orientation := Landscape
  pageSize := "Letter"
  marginTop := "1in"
  marginBottom := "1in"
  marginLeft := "1in"
  marginRight := "1in"
})

val page = <html><body><h1>Hello World</h1></body></html>

// Save the PDF generated from the above HTML into a Byte Array
val outputStream = new ByteArrayOutputStream
pdf.run(page, outputStream)

// Save the PDF of Google's homepage into a file
pdf.run(new URL("http://www.google.com"), new File("google.pdf"))
```

## Installing wkhtmltopdf ##

Visit the [wkhtmltopdf downloads page](http://wkhtmltopdf.org/downloads.html) and install the appropriate package for your platform.

## Troubleshooting ##

### NoExecutableException ###

Make sure `wkhtmltopdf` is installed and your JVM is running with the correct `PATH` environment variable.

If that doesn't work you can manually set the path to `wkhtmltopdf` when you create a new `Pdf` instance:

```scala
val pdf = Pdf("/opt/bin/wkhtmltopdf", PdfConfig.default)
```

### Resources aren't included in the PDF ###

Images, CSS, or JavaScript does not seem to be downloading correctly in the PDF. This is due to the fact that `wkhtmltopdf` does not know where to find those files. Make sure you are using absolute paths (start with forward slash) to your resources. If you are using PDFKit to generate PDFs from a raw HTML source make sure you use complete paths (either file paths or urls including the domain).

## Notes ##

### Asynchronous conversion ###

__sPDF__ relies on Scala's `scala.sys.process.Process` class to execute `wkhtmltopdf` and pipe input/output data.

The execution of `wkhtmltopdf` and thus the conversion to PDF is blocking. If you need the processing to be asynchronous, you can use `Pdf.prepare` and wrap the call inside a `Future`.

```scala
val pdf = Pdf(PdfConfig.default)

val p = pdf.prepare(new URL("http://www.google.com"), new File("google.pdf")).run() // start asynchronously
val f = Future(blocking(p.exitValue())) // wrap in Future
try Await.result(f, Duration(5, TimeUnit.SECONDS)) catch {
  case _: TimeoutException => p.destroy() // destroy the process in case of timeout
}
```

## Contributing ##

* Fork the project.
* Make your feature addition or bug fix.
* Add tests for it. This is important, so I don't break it in a future version unintentionally.
* Commit, do not mess with build settings, version, or history.
* Send me a pull request. Bonus points for topic branches.

### Release / Publish ###

* `release cross with-defaults`
* check out released version
* `publishSigned`
* `sonatypeRelease`

## Roadmap ##

- [X] Full support for extended options
- [X] Full support for input types
- [ ] Streaming API (with `scalaz-stream`)
- [ ] Simplified API with implicits
- [ ] Integration with Play for streaming PDFs in HTTP responses

## Copyright ##

Copyright (c) 2013, 2014 Federico Feroldi. See `LICENSE` for details.
