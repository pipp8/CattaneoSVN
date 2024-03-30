/* Step By Step Simple Sample Series
 * 
 * Example: WCF: Writing/consuming a service using VS2010
 * 
 * Scenario: 
 * Book store service(WCF) that would fetch the book information.
 * 
 * Technology:
 * WCF, Windows Forms, LINQ
 * 
 * 
 * Narrator: Kamran Khan
 * Blog: IZLOOITE.wordpress.com (aka izz-loo-aight)
 * 
 * Creative Commons -ShareAlike http://creativecommons.org/licenses/by-nc-sa/2.0/
 * 
 */

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Store;
using System.Xml.Linq;
using System.Linq.Expressions;
using System.ServiceModel;

namespace Store
{
    class BookService : IBookService
    {
        #region IBookService Members

        public List<Book> GetAllBooks()
        {
            XDocument db = GetDb();
            List<Book> lstBooks
                 =
                (from book in db.Descendants("book")
                 select new Book()
                 {
                     ID = book.Attribute("id").Value
                     ,
                     Author = book.Element("author").Value
                     ,
                     Genre = book.Element("genre").Value
                     ,
                     Price = Convert.ToDecimal(book.Element("price").Value)
                     ,
                     Description = book.Element("description").Value
                     ,
                     PublishDate = Convert.ToDateTime(book.Element("publish_date").Value)
                     ,
                     Title = book.Element("title").Value
                 }).ToList<Book>();

            return lstBooks;
        }

        public List<Book> Filter(string Author, string Genre, string Title)
        {
            XDocument db = GetDb();

            //A simple way to get all books and apply the Where predicate.

            //Howto: Convert XElements to Custom Object
            List<Book> lstBooks = GetAllBooks();

            if (!string.IsNullOrEmpty(Author))
            {
                lstBooks = lstBooks.Where(book => book.Author == Author).ToList<Book>();
            }

            if (!string.IsNullOrEmpty(Genre))
            {
                lstBooks = lstBooks.Where(book => book.Genre == Genre).ToList<Book>();
            }
            if (!string.IsNullOrEmpty(Title))
            {
                lstBooks = lstBooks.Where(book => book.Title == Title).ToList<Book>();
            }
            return lstBooks;
        }

        public List<Book> GetBookByID(string BookID)
        {
            XDocument db = GetDb();

            //Howto: Convert XElements to Custom Object
            List<Book> lstBooks =
                (from book in db.Descendants("book")
                 where book.Attribute("id").Value.Equals(BookID)
                 select new Book()
                 {
                     ID = book.Attribute("id").Value
                    ,
                     Author = book.Element("author").Value
                    ,
                     Genre = book.Element("genre").Value
                    ,
                     Price = Convert.ToDecimal(book.Element("price").Value)
                    ,
                     Description = book.Element("description").Value
                    ,
                     PublishDate = Convert.ToDateTime(book.Element("publish_date").Value)
                    ,
                     Title = book.Element("title").Value
                 }).ToList();


            return lstBooks;
        }

        private XDocument GetDb()
        {
            return XDocument.Load("SampleDB.xml");
        }

        #endregion
    }
}
