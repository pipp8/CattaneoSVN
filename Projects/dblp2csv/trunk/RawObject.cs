using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace dblp_xml2csv
{
    public class RawObject : IComparable<RawObject>, IComparer<RawObject>, IComparable
    {
        public Int32 Id { get; set; }
        public Dictionary<string, string> Attributes { get; set; }
        public RawObject()
        {
            Attributes = new Dictionary<string, string>();
        }

        public int CompareTo(RawObject other)
        {
            return Id.CompareTo(other.Id);
        }

        public int Compare(RawObject x, RawObject y)
        {
            return x.Id.CompareTo(y.Id);
        }

        int IComparable.CompareTo(object obj)
        {
            return CompareTo((RawObject)obj);
        }
    }

    public class RawLink : IComparable<RawLink>, IComparer<RawLink>, IComparable
    {
        public Int32 Id { get; set; }
        public Int32 From { get; set; }
        public Int32 To { get; set; }
        public Dictionary<string, string> Attributes { get; set; }
        public RawLink()
        {
            Attributes = new Dictionary<string, string>();
        }

        public int CompareTo(RawLink other)
        {
            return Id.CompareTo(other.Id);
        }

        public int Compare(RawLink x, RawLink y)
        {
            return x.Id.CompareTo(y.Id);
        }

        int IComparable.CompareTo(object obj)
        {
            return CompareTo((RawLink)obj);
        }
    }

    public class RawAttribute
    {
        public Int32 ObjectId { get; set; }
        public String Key { get; set; }
        public String Value { get; set; }
    }
}
