using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.ServiceModel;
using System.Runtime.Serialization;

namespace Store
{
    [DataContract]
    public class Book
    {
        [DataMember]
        public string ID;
        [DataMember]
        public string Title;
        [DataMember]
        public string Author;
        [DataMember]
        public string Description;
        [DataMember]
        public string Genre;
        [DataMember]
        public decimal Price;
        [DataMember]
        public DateTime PublishDate;
    }
}
