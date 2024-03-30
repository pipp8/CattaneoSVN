using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Services;

namespace TestWebService
{
    /// <summary>
    /// Summary description for Service1
    /// </summary>
    [WebService(Namespace = "http://tempuri.org/")]
    [WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
    [System.ComponentModel.ToolboxItem(false)]
    // To allow this Web Service to be called from script, using ASP.NET AJAX, uncomment the following line. 
    // [System.Web.Script.Services.ScriptService]
    public class Service1 : System.Web.Services.WebService
    {

        [WebMethod]
        public string HelloWorld()
        {
            return "Hello World";
        }

        [WebMethod]
        public string Conc(string n1, string n2, string n3)
        {
            HttpContext.Current.Trace.Write("n1 " + n1);
            HttpContext.Current.Trace.Write("n2 " + n2);
            HttpContext.Current.Trace.Write("n3 " + n3);

            return (n1 + "-" + n2 + "-" + n3);
        }
    }
}