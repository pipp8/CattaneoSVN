using System;
using System.Collections.Generic;
using System.Collections;
using System.Linq;
using System.Text;
using System.Xml.Linq;
using System.Data.Linq;

namespace dblp_xml2csv
{
    class Program
    {
        static void Main(string[] args)
        {
            var dblpXml = args[0];
            LoadDblpXml(dblpXml);
        }

        private static DblpContext LoadDblpXml(string dblpXml)
        {
            var context = new DblpContext();
            var objs = new Dictionary<Int32, RawObject>();
            var links = new Dictionary<Int32, RawLink>();
            var objAtts = new HashSet<String>();
            var lnkAtts = new HashSet<String>();
            var progress = 0;
            //Open file
            using (var f = System.IO.File.OpenText(dblpXml))
            {
                //Read Raw Objects
                //Read Raw Links
                //Read Raw Attributes and apply them to raw links and objects

                var elements = GetXmlElements(f);
                foreach (var e in elements)
                {
                    progress++;
                    var obj = ParseObject(e);
                    var lnk = ParseLink(e);
                    var att = ParseAttribute(e);
                    if (obj != null)
                        objs.Add(obj.Id, obj);
                    if (lnk != null)
                        links.Add(lnk.Id, lnk);
                    if (att != null)
                    {
                        switch (att.Key)



























                        {
                            case "in-year":
                            case "in-number":
                            case "in-volume":
                            case "month":
                            case "pages":
                            case "in-proceedings":
                            case "link-type":
                                var thelnk = default(RawLink);
                                links.TryGetValue(att.ObjectId, out thelnk);
                                thelnk.Attributes.Add(att.Key, att.Value);
                                lnkAtts.Add(att.Key);
                                break;
                            default:
                                var theObj = default(RawObject);
                                objs.TryGetValue(att.ObjectId, out theObj);
                                theObj.Attributes.Add(att.Key, att.Value);
                                objAtts.Add(att.Key);
                                break;
                        }
                        if (progress % 1000 == 0)
                            Console.Write("/");
                    }
                    if (progress % 10000 == 0)
                        Console.Write(".");
                }

                f.Close();
            }

            Console.WriteLine();
            Console.WriteLine("Writing the Link File");
            progress = 0;
            //Write the result in two files: Links, and Objects
            var lnkAttsList = lnkAtts.ToList();

            using(var lnkFile = System.IO.File.CreateText(dblpXml+".lnk.csv"))
            {
                progress ++;
                if (progress % 10000 == 0)
                    Console.Write(".");

                lnkFile.Write("LinkId, From, To");
                lnkAttsList.ForEach(l => lnkFile.Write( "," + l ));
                lnkFile.WriteLine();

                foreach (var lnk in links.Values)
                {
                    lnkFile.Write("{0}, {1}, {2}", lnk.Id, lnk.From, lnk.To);
                    lnkAttsList.ForEach(l => lnkFile.Write( lnk.Attributes.ContainsKey(l) ? "," + lnk.Attributes[l] : "," ));
                    lnkFile.WriteLine();
                }
                lnkFile.Close();
            }

            Console.WriteLine();
            Console.WriteLine("Writing Object Files");

            var objAttsList = objAtts.ToList();
            progress = 0;
            using (var objFile = System.IO.File.CreateText(dblpXml + ".obj.csv"))
            {
                progress++;
                if (progress % 10000 == 0)
                    Console.Write("~");

                objFile.Write("Id");
                objAttsList.ForEach(l => objFile.Write("," + l.Replace(",", "~")));
                objFile.WriteLine();

                foreach (var obj in objs.Values)
                {
                    objFile.Write("{0}", obj.Id);
                    objAttsList.ForEach(l => objFile.Write(obj.Attributes.ContainsKey(l) ? "," + obj.Attributes[l].Replace(",", "~") : ","));
                    objFile.WriteLine();
                }
                objFile.Close();
            }

            return context;
        }

        private static RawObject ParseObject(XElement e)
        {
            if ((e == null) || e.Name != "OBJECT")
                return null;
            return new RawObject() { Id = Int32.Parse(e.Attribute(XName.Get("ID")).Value) };
        }

        private static RawLink ParseLink(XElement e)
        {
            if ((e == null) || e.Name != "LINK")
                return null;
            return new RawLink()
            {
                Id = Int32.Parse(e.Attribute(XName.Get("ID")).Value),
                From = Int32.Parse(e.Attribute(XName.Get("O1-ID")).Value),
                To = Int32.Parse(e.Attribute(XName.Get("O2-ID")).Value)
            };
        }

        private static RawAttribute ParseAttribute(XElement e)
        {
            if ((e == null) || e.Name != "ATTR-VALUE")
                return null;
            return new RawAttribute()
            {
                ObjectId = Int32.Parse(e.Attribute(XName.Get("ITEM-ID")).Value),
                Key = e.Attribute(XName.Get("NAME")).Value,
                Value = e.Element(XName.Get("COL-VALUE")).Value
            };
        }

        private static IEnumerable<XElement> GetXmlElements(System.IO.TextReader f)
        {
            //read until nex interesting element;
            var line = "";
            var interestings = new[] {"OBJECT", "LINK", "ATTR-VALUE", "ATTRIBUTE" };
            var attributeName = "";
            while ((line = f.ReadLine())!=null)
            {
                line = line.Trim();
                var nextOOI = interestings.FirstOrDefault( i => line.StartsWith("<"+i+" ") );
                if (nextOOI == null)
                    continue;
                //If it is the attribute tag, get the current atttribute name
                if (nextOOI == "ATTRIBUTE")
                {
                    attributeName = line.Split(new[] { "NAME=\"" }, 2, StringSplitOptions.RemoveEmptyEntries)[1].Split("\"".ToCharArray(), 2, StringSplitOptions.RemoveEmptyEntries)[0];
                    continue;
                }
                //Read to the end of the tag
                var xml = line;
                if (!xml.Contains("/>"))
                    while (!xml.Contains("</" + nextOOI))
                        xml += f.ReadLine();
                var rv = XElement.Parse(xml);

                if (nextOOI == "ATTR-VALUE")
                {
                    rv.Add(new XAttribute("NAME", attributeName));
                }
                yield return rv;
            }
        }
    }
}
