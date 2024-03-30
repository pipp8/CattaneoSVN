using System;
using System.Collections.Generic;
using System.Text;
using NLog;

namespace NLogSimpleDemo
{
    class AbstractClass
    {
        protected Logger logger = null;

        public AbstractClass()
        {
            logger = LogManager.GetLogger(this.GetType().Name);
        }
    }
}
