using System;
using System.Collections.Generic;


namespace DblpDqSimulator.Common
{
    public static class ObjectExtension
    {
        public static TOut IfNotNull<TIn, TOut>(this TIn theIn, Func<TIn, TOut> f) where TIn : class
        {
            return theIn != null ? f(theIn) : default(TOut);
        }

        public static string FormatWith(this string str, params object[] pars)
        {
            return String.Format(str, pars);
        }

        public static HashSet<T> ToHashSet<T>(this IEnumerable<T> data)
        {
            var hs = new HashSet<T>();
            foreach (var dt in data)
            {
                hs.Add(dt);
            }
            return hs;
        }
    }
}
