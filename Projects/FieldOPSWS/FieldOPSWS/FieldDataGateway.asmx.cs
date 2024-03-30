using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Services;
using System.Reflection;

namespace FieldOPSWS
{
    /// <summary>
    /// Summary description for Service1
    /// </summary>
    [WebService(Namespace = "http://tempuri.org/")]
    [WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
    [System.ComponentModel.ToolboxItem(false)]
    // To allow this Web Service to be called from script, using ASP.NET AJAX, uncomment the following line. 
    // [System.Web.Script.Services.ScriptService]
    public class FieldDataGateway : System.Web.Services.WebService
    {
        static public string revision = "$Revision: 40 $";

        [WebMethod]
        public string GetVersion()
        {
            Assembly asm = Assembly.GetExecutingAssembly();
            AssemblyName asmName = asm.GetName();
            string fmtStd = "{0}\r\nVersione: {1}.{2}.{3}.{4} del: {5}";

            return string.Format(fmtStd, asmName.Name, asmName.Version.Major, asmName.Version.MajorRevision,
                                478, revision, "26/7/2013");
        }

        [WebMethod]
        [System.Xml.Serialization.XmlInclude(typeof(DatiCiclo))]
        public DatiCiclo[] GetAllCicli()
        {
            try
            {
                DataSet1TableAdapters.AllCicliTableAdapter ta = new DataSet1TableAdapters.AllCicliTableAdapter();
                ta.Connection.ConnectionString = clsDB.DBConnectionString;

                DataSet1.AllCicliDataTable dt = ta.GetData();
 
                int cnt = 0;
                DatiCiclo[] retValue = new DatiCiclo[dt.Count];
                foreach (DataSet1.AllCicliRow  r in dt.Rows)
                {
                    DatiCiclo rv = new DatiCiclo();
                    rv.IdCiclo = r.IdCiclo;
                    rv.StatoAttivo = r.StatoAttivo;
                    rv.Descrizione = r.Descrizione;
                    rv.Indirizzo = r.Indirizzo;
                    rv.Campionamento = r.Campionamento;
                    rv.DescrizioneStato = r.descStato;
                    rv.IdStato = r.IdStato;

                    retValue[cnt++] = rv;
                }
                return retValue;
            }
            catch (Exception ex)
            {
                throw new Exception("DB Failed: " + ex.Message);
                return null;
            }
        }

        [WebMethod]
        [System.Xml.Serialization.XmlInclude(typeof(DatiVariabileEx))]
        public DatiVariabileEx[] GetAllVars()
        {
            try
            {
                DataSet1TableAdapters.VariabileTableAdapter ta = new DataSet1TableAdapters.VariabileTableAdapter();
                ta.Connection.ConnectionString = clsDB.DBConnectionString;

                DataSet1.VariabileDataTable dtVars;
                try
                {
                    dtVars = ta.GetData();
                }
                catch (Exception ex)
                {
                    throw (new Exception("E' fallita la query per leggere tutte le variabili"));
                }

                if (dtVars.Count <= 0)
                {
                    throw (new Exception("Non è stato possibile trovare alcuna variabile"));
                }
                else
                {
                    int cnt = 0;
                    DatiVariabileEx[] retVal = new DatiVariabileEx[dtVars.Count];
                    foreach (DataSet1.VariabileRow r in dtVars.Rows)
                    {
                        DatiVariabileEx rv = new DatiVariabileEx();

                        rv.IdVariabile = r.IdVariabile;
                        rv.Descrizione = r.Descrizione;
                        rv.Tipo = r.Tipo;
                        rv.Indirizzo = r.Indirizzo;
                        rv.Unita = r.Unita;
                        rv.Campionamento = r.Campionamento;
                        rv.ValMinimo = r.ValMinimo;
                        rv.ValMassimo = r.ValMassimo;
                        rv.ClasseAllarme = r.ClasseAllarme;
                        rv.StatoAllarme = r.StatoAllarme;
                        rv.ValCorrente = r.ValCorrente;

                        retVal[cnt++] = rv;
                    }
                    return retVal;
                }
            }
            catch (Exception ex)
            {
                throw new Exception("DB Failed: " + ex.Message);
                return null;
            }
        }

        [WebMethod]
        [System.Xml.Serialization.XmlInclude(typeof(DatiVar))]
        public DatiVar GetVarById( string IdVar)
        {
            try
            {
                DataSet1TableAdapters.VariabileTableAdapter ta = new DataSet1TableAdapters.VariabileTableAdapter();
                ta.Connection.ConnectionString = clsDB.DBConnectionString;

                DataSet1.VariabileDataTable dtVar;
                try
                {
                    dtVar = ta.GetVariableById( IdVar);
                }
                catch (Exception ex)
                {
                    throw (new Exception("E' fallita la query per leggere il valore della variabile: " + IdVar ));
                }

                if (dtVar.Count <= 0)
                {
                    throw (new Exception("Non è stato possibile trovare la variabile con ID: " + IdVar));
                }
                else
                {
                    DatiVar retVal = new DatiVar();

                    retVal.IdVariabile = dtVar[0].IdVariabile;
                    retVal.Tipo = dtVar[0].Tipo;
                    retVal.Unita = dtVar[0].Unita;
                    retVal.Campionamento = Convert.ToInt32(dtVar[0].Campionamento);
                    retVal.ValCorrente = dtVar[0].ValCorrente;

                    return retVal;
                }
            }
            catch (Exception ex)
            {
                throw new Exception("DB Failed: " + ex.Message);
                return null;
            }
        }


        [WebMethod]
        [System.Xml.Serialization.XmlInclude(typeof(DatiVar))]
        public int GetStatoCicloById(int IdCiclo)
        {
            try
            {
                DataSet1TableAdapters.CicloTableAdapter ta = new DataSet1TableAdapters.CicloTableAdapter();
                ta.Connection.ConnectionString = clsDB.DBConnectionString;

                int IdStatus = -1;
                try
                {
                    IdStatus = Convert.ToInt32(ta.GetStatoCicloById(IdCiclo));
                }
                catch (Exception ex)
                {
                    throw (new Exception("E' fallita la query per leggere lo stato del ciclo con Id: " + IdCiclo));
                }

                return IdStatus;
            }
            catch (Exception ex)
            {
                throw new Exception("DB Failed: " + ex.Message);
                return -1;
            }
        }
    }

}