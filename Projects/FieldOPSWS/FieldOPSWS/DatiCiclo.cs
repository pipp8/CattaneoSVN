
using System.Xml.Serialization;


namespace FieldOPSWS
{
    [System.Serializable()]
    public class DatiCiclo
    {
        /* PRIVATE FIELDS */
        private int _IdCiclo;
        private int _StatoAttivo;
        private string _Descrizione;
        private int _Indirizzo;
        private int _Campionamento;
        private string _DescrizioneStato;
        private int _IdStato;
  
        /* PUBLIC PROPERTIES */
        public int IdCiclo
        {
            get { return _IdCiclo; }
            set { _IdCiclo = value; }
        }
        public int StatoAttivo
        {
            get { return _StatoAttivo; }
            set { _StatoAttivo = value; }
        }
        public string Descrizione
        {
            get { return _Descrizione; }
            set { _Descrizione = value; }
        }
        public int Indirizzo
        {
            get { return _Indirizzo; }
            set { _Indirizzo = value; }
        }
        public int Campionamento
        {
            get { return _Campionamento; }
            set { _Campionamento = value; }
        }
        public string DescrizioneStato
        {
            get { return _DescrizioneStato; }
            set { _DescrizioneStato = value; }
        }
        public int IdStato
        {
            get { return _IdStato; }
            set { _IdStato = value; }
        }
    }
}
