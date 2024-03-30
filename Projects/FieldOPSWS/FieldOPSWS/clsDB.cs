using System;
using System.Collections.Generic;
using System.Text;

using System.Data.OleDb;
using System.Configuration;
using System.Data;

using System.Reflection;


namespace FieldOPSWS
{
    public class clsDB
    {

        public static string DBConnectionString
        {
            get
            {
                return ConfigurationManager.ConnectionStrings["dbProcessoConnectionString"].ToString() + ";Password=stella";
//                return ConfigurationManager.ConnectionStrings["dbProcessoConnectionString"].ToString();
            }
        }

    }
}
