using System;
using System.Collections.Generic;
using System.Text;
using NLog;

namespace NLogSimpleDemo
{
    class DerivedClass : AbstractClass
    {
        public void Log(string msg)
        {
            logger.Info(msg);
        }
    }
}
