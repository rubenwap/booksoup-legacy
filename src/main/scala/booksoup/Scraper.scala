package booksoup

import java.io.File

import com.github.tototoshi.csv.CSVWriter
import org.jsoup.{Connection, Jsoup}

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.util.Random

case class Author(
                   name: String,
                   url: String,
                 )

case class Book(
               AuthorUrl: String,
                 BookUrl: String,
               )

object Scrape extends Helpers with App {

  val authorsDb = new File("authors.csv")
  val writerAuthors = CSVWriter.open(authorsDb)

  val booksDb = new File("books.csv")
  val writerBooks = CSVWriter.open(booksDb)

  val authors = getAuthors
  writerAuthors.writeRow(List("name", "url"))
  writerAuthors.writeAll(authors.map(a => List(a.name, a.url)))
  writerAuthors.close()
  
  val books = getBooks(authors)
  writerBooks.writeRow(List("url"))
  writerBooks.writeAll(books.map(b => List(b.AuthorUrl, b.BookUrl)))
  writerBooks.close()

}

trait Helpers {

  val baseUrl = "https://www.holaebook.com"

  val userAgents: List[String] = List[String](
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36",
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:53.0) Gecko/20100101 Firefox/53.0",
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393",
    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)",
  )

  def getAuthors: List[Author] = {
    val index = 'a' to 'z'
    index.map(letter => {
      val conn = getConn(s"""https://www.holaebook.com/autores/${letter}.html""")
      val authNodes = conn.get().select("""a[href^="/autor/"]""")
      for (auth <- authNodes.asScala) yield Author(auth
        .text()
        .replace("Sobre holaebook.com |", "")
        .replace("Libros Gratis de ", ""), s"""${baseUrl}${auth.attr("href")}""")
    }).toList.flatten.distinct.filter(a => a.name != "")
  }

  def getBooks(authors: List[Author]): List[Book] = {
    authors.flatMap(author => {
      val conn = getConn(author.url)
      val bookNodes = conn.get().select("""a[href^="book/"]""")
      for (book <- bookNodes.asScala) yield Book(author.url, book.attr("href")
      )
    })
  }

  def getConn(url: String): Connection = {
    println(url)
    val random = new Random
    Thread.sleep(random.nextInt(1000))
    try {
      Jsoup
        .connect(url)
        .userAgent(userAgents(random.nextInt(userAgents.length)))
    } catch {
      case _: Throwable => {
        Thread.sleep(random.nextInt(10000))
        getConn(url)
      }
    } 
  }

}
