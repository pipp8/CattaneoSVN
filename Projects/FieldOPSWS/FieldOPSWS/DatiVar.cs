namespace FieldOPSWS
{
    [System.Serializable()]
    public class DatiVar
    {
        /* PRIVATE FIELDS */
        private string _IdVariabile;
	    private string _Tipo;
        private string _Unita;
        private int _Campionamento;
        private string _ValCorrente;

        /* PUBLIC PROPERTIES */

        public string IdVariabile
        {
            get { return _IdVariabile; }
            set { _IdVariabile = value; }
        }
 
        public string Tipo
        {
            get { return _Tipo; }
            set { _Tipo = value; }
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

        public string ValCorrente
        {
            get { return _ValCorrente; }
            set { _ValCorrente = value; }
        }
 
    }
}
