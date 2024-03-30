namespace FieldOPSWS
{
    [System.Serializable()]
    public class DatiVariabileEx
    {
        /* PRIVATE FIELDS */
        private string _IdVariabile;
        private string _Descrizione;
	    private string _Tipo;
        private int _Indirizzo;
        private string _Unita;
        private int _Campionamento;
        private string _ValMinimo;
        private string _ValMassimo;
        private string _ClasseAllarme;
        private bool _StatoAllarme;
        private string _ValCorrente;

 
        /* PUBLIC PROPERTIES */

        public string IdVariabile
        {
            get { return _IdVariabile; }
            set { _IdVariabile = value; }
        }

        public string Descrizione
        {
            get { return _Descrizione; }
            set { _Descrizione = value; }
        }
 
        public string Tipo
        {
            get { return _Tipo; }
            set { _Tipo = value; }
        }

        public int Indirizzo
        {
            get { return _Indirizzo; }
            set { _Indirizzo = value; }
        }

        public string Unita
        {
            get { return _Unita; }
            set { _Unita = value; }
        }
 
  
        public int Campionamento
        {
            get { return _Campionamento; }
            set { _Campionamento = value; }
        }

        public string ValMinimo
        {
            get { return _ValMinimo; }
            set { _ValMinimo = value; }
        }

        public string ValMassimo
        {
            get { return _ValMassimo; }
            set { _ValMassimo = value; }
        }

        public string ClasseAllarme
        {
            get { return _ClasseAllarme; }
            set { _ClasseAllarme = value; }
        }

        public bool StatoAllarme
        {
            get { return _StatoAllarme; }
            set { _StatoAllarme = value; }
        }

        public string ValCorrente
        {
            get { return _ValCorrente; }
            set { _ValCorrente = value; }
        }
    }
}
