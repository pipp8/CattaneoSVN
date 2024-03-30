using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace dblp_xml2csv
{
    public interface IDblpEntity
    {
        int Id { get; set; }
    }

    public class dblp
    {
       
    }

    public class Person : IDblpEntity
    {
        public int Id { get; set; }
        public string Name { get; set; }
    }

    public class Www : IDblpEntity
    {
        public int Id { get; set; }
        public int Year { get; set; }
        public string Title { get; set; }
        public string Url { get; set; }
    }

    public class Proceeding : IDblpEntity
    {
        public int Id { get; set; }
        public string Volume { get; set; }
        public int Year { get; set; }
        public string Title { get; set; }
        public string Conference { get; set; }
        public string Publisher { get; set; }
        public string ISBN { get; set; }
        public string Series { get; set; }
    }

    public class Book : IDblpEntity
    {
        public int Id { get; set; }
        public string Volume { get; set; }
        public int Year { get; set; }
        public string Title { get; set; }
        public string Publisher { get; set; }
        public string ISBN { get; set; }
        public string Series { get; set; }
    }

    public class Journal : IDblpEntity
    {
        public int Id { get; set; }
        public string Title { get; set; }
        public string Publisher { get; set; }
    }

    public class Paper : IDblpEntity
    {
        public int Id { get; set; }
        public string Name { get; set; }
    }

    public class PhDThesis : IDblpEntity
    {
        public int Id { get; set; }
        public string Volume { get; set; }
        public int Year { get; set; }
        public string Title { get; set; }
        public string Publisher { get; set; }
        public string ISBN { get; set; }
        public string Series { get; set; }
        public string School { get; set; }
    }

    public class MThesis : IDblpEntity
    {
        public int Id { get; set; }
        public int Year { get; set; }
        public string Title { get; set; }
        public string School { get; set; }
    }

    public class InProceeding
    {
        public int PaperId { get; set; }
        public int ProceedingId { get; set; }
        public string Pages { get; set; }
    }

    public class AuthorOf
    {
        public int PersonId { get; set; }
        public int OtherEntityId { get; set; }
    }

    public class InJournal
    {
        public int PaperId { get; set; }
        public int JournalId { get; set; }
        public DateTime Month { get; set; }
        public int InVolume { get; set; }
        public int InYear { get; set; }
        public string Pages { get; set; }
        public int Number { get; set; }
    }

    public class Cite
    {
        public int EntityId { get; set; }
        public int CitedEntityId { get; set; }
    }

    public class EditorOf
    {
        public int PersonId { get; set; }
        public int OtherEntityId { get; set; }
    }

    public class InCollection
    {
        public int PaperId { get; set; }
        public int BookId { get; set; }
        public string Pages { get; set; }
    }
}
