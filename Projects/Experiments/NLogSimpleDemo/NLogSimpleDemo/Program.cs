using System;
using System.Collections.Generic;
using System.Text;

using NLog;


namespace NLogSimpleDemo
{
    class Program
    {


        static void Main(string[] args)
        {
            Logger logger = LogManager.GetLogger("Main");

            DerivedClass tt = new DerivedClass();

            tt.Log("test");

            logger.Trace("Sample trace message");
            logger.Debug("Sample debug message");
            logger.Info("Sample informational message");
            logger.Warn("Sample warning message");
            logger.Error("Sample error message");
            logger.Fatal("Sample fatal error message");

            // alternatively you can call the Log() method 
            // and pass log level as the parameter.
            logger.Log(LogLevel.Info, "Sample informational message");

            Console.ReadLine();
        }
    }
}
