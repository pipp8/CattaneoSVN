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
using System.ServiceModel;
using WcfServiceLibrary1;

namespace Store
{
    [ServiceContract]
    interface IBookService
    {
        [OperationContract]
        List<Book> GetAllBooks();

        [OperationContract]
        List<Book> GetBookByID(string BookID);

        [OperationContract]
        List<Book> Filter(string Author, string Genre, string Title);
    }
}
