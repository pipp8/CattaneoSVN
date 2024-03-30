using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace dblp_xml2csv
{
    public class DblpContext
    {
        public List<Person> Persons { get; set; }
        public List<Www> Wwws { get; set; }
        public List<Proceeding> Proceedings { get; set; }
        public List<Book> Books { get; set; }
        public List<Journal> Journals { get; set; }
        public List<Paper> Papers { get; set; }
        public List<PhDThesis> PhDThesises { get; set; }
        public List<MThesis> MThesises { get; set; }
        public List<InProceeding> InProceedings { get; set; }
        public List<AuthorOf> AuthorOfs { get; set; }
        public List<InJournal> InJournals { get; set; }
        public List<Cite> Cites { get; set; }
        public List<EditorOf> EditorOfs { get; set; }
        public List<InCollection> InCollections{ get; set; }

        public DblpContext()
        {
            Persons = new List<Person>();
            Wwws = new List<Www>();
            Proceedings = new List<Proceeding>();
            Books = new List<Book>();
            Journals = new List<Journal>();
            Papers = new List<Paper>();
            PhDThesises = new List<PhDThesis>();
            MThesises = new List<MThesis>();
            InProceedings = new List<InProceeding>();
            AuthorOfs = new List<AuthorOf>();
        }
    }
}
