using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace SharedObjectLibrary
{
    // Remote object.
    public class RemoteObject : MarshalByRefObject
    {
        private int callCount = 0;
        public double DoubleProperty
        {
            get;
            set;
        }
        public int GetCount()
        {
            Console.WriteLine("GetCount has been called.");
            callCount++;
            return (callCount);
        }

        public double Add(double n1, double n2)
        {
            callCount++;
            return (DoubleProperty = n1 + n2);
        }
        public double Subtract(double n1, double n2)
        {
            callCount++;
            return (DoubleProperty = n1 - n2);
        }
        public double Multiply(double n1, double n2)
        {
            callCount++;
            return (DoubleProperty = n1 * n2);
        }
        public double Divide(double n1, double n2)
        {
            callCount++;
            return (DoubleProperty = n1 / n2);
        }
    }
}
