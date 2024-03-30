
/*
 * $Id: Form1.cs 105 2015-07-11 08:14:12Z cattaneo $
 */


using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Security.Cryptography;
using System.Security.Cryptography.X509Certificates;

using TestAIRClient.DatiPaziente;
using TestAIRClient.RilevazioneDati;

namespace TestAIRClient
{
    public partial class Form1 : Form
    {

        private string revision = "$Revision: 105 $";

        private DatiPaziente.PatientDataWebServiceClient AnagrafeWS = null;

        private RilevazioneDati.RelevationWebServiceClient RilevazioneWS = null;

        public static readonly DateTime DefaultDate = new DateTime(2010,10,12);

        private DatiPaziente.authorizationToken st = null;
        private RilevazioneDati.authorizationToken st2 = null;

        private string refMMG1 = "MRCLGU60A01F839U";
        private string refMMG2 = "MRCLGU60A01F839U";

        private string refPaziente = "wFGLWv1pTdPEEC0qR1ToEWacsVJZcqDByMt+2LHqM2s=";

        public Form1()
        {
            InitializeComponent();

            String rev = revision.Substring(9);
            rev = rev.Substring(0, rev.Length-2);
            this.Text = "Client di test Servizi AIR. Rev" + rev;

            // gestione certificato selfsigned
            System.Net.ServicePointManager.ServerCertificateValidationCallback = 
                delegate(object s,
                    X509Certificate certificate, X509Chain chain,
                    System.Net.Security.SslPolicyErrors sslPolicyErrors) { return true; };

            try
            {

                AnagrafeWS = new DatiPaziente.PatientDataWebServiceClient();

                RilevazioneWS = new RelevationWebServiceClient();
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
            }
        }


        private void btAuthorizationToken_Click(object sender, EventArgs e)
        {
            DatiPaziente.authenticationToken auth = new DatiPaziente.authenticationToken();

            auth.username = refMMG1;
            auth.password = Utils.CalculateMD5Hash(refMMG1);
            auth.clientSecret = "TEST";

            try
            {
                st = AnagrafeWS.getAuthorizationToken(refMMG1, auth);

                st2 = new RilevazioneDati.authorizationToken();
                st2.token = st.token;
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
            }

            txtLog.AppendText("Servizio getAuthorizationToken invocato con successo: " + st.token + Environment.NewLine);
        }


        private void btVersion_Click(object sender, EventArgs e)
        {
            string ver = RilevazioneWS.getVersion();
            txtLog.AppendText("Versione del servizio: " + ver + Environment.NewLine);
        }


        #region DatiAnagrafici

        private void btAggiungiPaziente_Click(object sender, EventArgs e)
        {
            patientAnagraficDataResult result = null;

            //<xs:complexType name="doWriteAnagraficData">
            //<xs:sequence>
            //<xs:element name="securityToken" type="tns:securityToken"/>
            //<xs:element name="MMGCodFisc" type="xs:string"/>
            //<xs:element name="patientCodFisc" type="xs:string"/>
            //<xs:element name="patientAnagraficData" type="tns:patientAnagraficData"/>
            //</xs:sequence>
            //</xs:complexType>
            //<xs:complexType name="patientAnagraficData">
            //<xs:sequence>
            //<xs:element name="asl" type="xs:int"/>
            //<xs:element minOccurs="0" name="birthdate" type="xs:dateTime"/>
            //<xs:element minOccurs="0" name="city" type="xs:string"/>
            //<xs:element minOccurs="0" name="citizenId" type="xs:string"/>
            //<xs:element name="dsb" type="xs:int"/>
            //<xs:element minOccurs="0" name="houseNumber" type="xs:string"/>
            //<xs:element minOccurs="0" name="name" type="xs:string"/>
            //<xs:element minOccurs="0" name="nationality" type="xs:string"/>
            //<xs:element minOccurs="0" name="road" type="xs:string"/>
            //<xs:element minOccurs="0" name="patientCodFisc" type="xs:string"/>
            //<xs:element minOccurs="0" name="surname" type="xs:string"/>
            //</xs:sequence>
            //</xs:complexType>

            patientAnagraficData ndg = new patientAnagraficData();

            ndg.asl = 202;
            ndg.birthdate = DefaultDate;
            ndg.city = "Salerno";
            ndg.citizenId = ""; // non settare per creare
            ndg.dsb = 8;
            ndg.houseNumber = "123a";
            ndg.name = "Giuseppe";
            ndg.nationality = "Italiana";
            ndg.road ="via roma";
            ndg.patientCodFisc = "CTTGPP60A11A662B";
            ndg.surname = "Cattaneo";

            try
            {
                result = AnagrafeWS.doWriteAnagraficData(st, refMMG1, ndg);
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
            }

            txtLog.AppendText("Servizio doWriteAnagraficData invocato con successo id cittadino: " + result.citizenId + " " + (result.result ? "OK" : "NO") + Environment.NewLine);

        }

        private void btAggiungiDatiAnamnestici_Click(object sender, EventArgs e)
        {
            bool result = false;

            // <xs:complexType name="doWriteAnamnesticData">
            //<xs:sequence>
            //<xs:element name="securityToken" type="tns:authorizationToken"/>
            //<xs:element name="MMGCodFisc" type="xs:string"/>
            //<xs:element name="patientAnamnesticData" type="tns:patientAnamnesticData"/>
            //</xs:sequence>
            //</xs:complexType>
            //<xs:complexType name="patientAnamnesticData">
            //<xs:sequence>
            //<xs:element minOccurs="0" name="alcoholCheckDate" type="xs:dateTime"/>
            //<xs:element name="alcoholKind" type="xs:int"/>
            //<xs:element name="alcoholQuantity" type="xs:int"/>
            //<xs:element name="citizenId" type="xs:string"/>
            //<xs:element name="currentTherapy" type="xs:int"/>
            //<xs:element name="educationLevel" type="xs:int"/>
            //<xs:element minOccurs="0" name="enrollDate" type="xs:dateTime"/>
            //<xs:element name="firstDiabetesDiagnosis" type="xs:int"/>
            //<xs:element name="marital_status" type="xs:int"/>
            //<xs:element name="physicalActivity" type="xs:boolean"/>
            //<xs:element minOccurs="0" name="physicalActivityCheckDate" type="xs:dateTime"/>
            //<xs:element name="physicalActivityLevel" type="xs:int"/>
            //<xs:element minOccurs="0" name="smokingCheckDate" type="xs:dateTime"/>
            //<xs:element name="smokingCurrentAttitude" type="xs:boolean"/>
            //<xs:element name="smokingDailyQuantity" type="xs:int"/>
            //<xs:element name="smokingLastYear" type="xs:int"/>
            //<xs:element name="smokingPreviousAttitude" type="xs:boolean"/>
            //<xs:element name="smokingType" type="xs:int"/>
            //<xs:element name="alcoholUser" type="xs:boolean"/>
            //<xs:element name="workingCategory" type="xs:int"/>
            //<xs:element name="workingPosition" type="xs:int"/>
            //</xs:sequence>
    
 
            patientAnamnesticData anam = new patientAnamnesticData();

            anam.alcoholCheckDate = DefaultDate;
            anam.alcoholKind = 3;
            anam.alcoholQuantity = 2;
            anam.currentTherapy = 1;
            anam.educationLevel = 3;
            anam.enrollDate = DefaultDate;
            anam.firstDiabetesDiagnosis = 2000;
            anam.marital_status = 1;
            anam.physicalActivity = true;
            anam.physicalActivityCheckDate = DefaultDate;
            anam.physicalActivityLevel = 2;
            anam.smokingCheckDate = DefaultDate;
            anam.smokingCurrentAttitude = true;
            anam.smokingDailyQuantity = 20;
            anam.smokingLastYear = 10;
            anam.smokingPreviousAttitude = true;
            anam.smokingType = 3;
            anam.alcoholUser = true;
            anam.workingCategory = 5;
            anam.workingPosition = 3;


            try
            {
                result = AnagrafeWS.doWriteAnamnesticData(st, refMMG1, anam);
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
            }

            txtLog.AppendText("Servizio doWriteAnamnesticData invocato con successo" + (result ? "OK" : "NO") + Environment.NewLine);
        }

        private void btComplicazioni_Click(object sender, EventArgs e)
        {
            //<xs:complexType name="doWriteComplications">
            //<xs:sequence>
            //<xs:element name="securityToken" type="tns:authorizationToken"/>
            //<xs:element name="MMGCodFisc" type="xs:string"/>
            //<xs:element name="patientComplications" type="tns:patientComplications"/>
            //</xs:sequence>
            //</xs:complexType>
            //<xs:complexType name="authorizationToken">
            //<xs:sequence>
            //<xs:element minOccurs="0" name="token" type="xs:string"/>
            //</xs:sequence>
            //</xs:complexType>
            //<xs:complexType name="patientComplications">
            //<xs:sequence>
            //<xs:element minOccurs="0" name="amputation" type="xs:dateTime"/>
            //<xs:element minOccurs="0" name="angina" type="xs:dateTime"/>
            //<xs:element minOccurs="0" name="blindness" type="xs:dateTime"/>
            //<xs:element name="citizenId" type="xs:string"/>
            //<xs:element minOccurs="0" name="claudicatio" type="xs:dateTime"/>
            //<xs:element minOccurs="0" name="dialysis" type="xs:dateTime"/>
            //<xs:element minOccurs="0" name="fundus" type="xs:dateTime"/>
            //<xs:element minOccurs="0" name="ictus" type="xs:dateTime"/>
            //<xs:element minOccurs="0" name="ima" type="xs:dateTime"/>
            //<xs:element minOccurs="0" name="ischemicHeartCondition" type="xs:dateTime"/>
            //<xs:element minOccurs="0" name="nephropathy" type="xs:dateTime"/>
            //<xs:element minOccurs="0" name="retinopathy" type="xs:dateTime"/>
            //<xs:element minOccurs="0" name="revascularization" type="xs:dateTime"/>
            //<xs:element minOccurs="0" name="tia" type="xs:dateTime"/>
            //<xs:element minOccurs="0" name="ulcerations" type="xs:dateTime"/>
            //</xs:sequence>
        }

        private void btEsenzioni_Click(object sender, EventArgs e)
        {
            // <xs:complexType name="doWriteExemptionCode">
            //<xs:sequence>
            //<xs:element name="securityToken" type="tns:authorizationToken"/>
            //<xs:element name="MMGCodFisc" type="xs:string"/>
            //<xs:element name="patientExemption" type="tns:patientExemption"/>
            //</xs:sequence>
            //</xs:complexType>
            //<xs:complexType name="patientExemption">
            //<xs:sequence>
            //<xs:element minOccurs="0" name="citizenId" type="xs:string"/>
            //<xs:element name="exemptionCode" type="xs:string"/>
            //<xs:element name="exemptionDate" nillable="true" type="xs:dateTime"/>
            //</xs:sequence>
            //</xs:complexType>
        }

        #endregion DatiAnagrafici


        #region DatiRilevazioni

        private void btDatiStartup_Click(object sender, EventArgs e)
        {
            //<xs:complexType name="startupRelevation">
            //<xs:complexContent>
            //<xs:extension base="tns:yearlyRelevation">
            //<xs:sequence/>
            //</xs:extension>
            //</xs:complexContent>
            bool result = false;

            startupRelevation rilevazione = new startupRelevation();

            //<xs:complexType name="doWriteYearlyPatientData">
            //<xs:sequence>
            //<xs:element minOccurs="0" name="securityToken" type="tns:authorizationToken"/>
            //<xs:element minOccurs="0" name="MMGCodFisc" type="xs:string"/>
            //<xs:element minOccurs="0" name="relevation" type="tns:yearlyRelevation"/>
            //<xs:element name="quarter" type="xs:int"/>
            //<xs:element name="year" type="xs:int"/>
            //</xs:sequence>

            //<xs:complexType name="yearlyRelevation">
            //<xs:complexContent>
            //<xs:extension base="tns:semestralRelevation">
            //<xs:sequence>

            // prima si setta la chiave primaria che ogni MMG avrà salvato al momento dell'arruolamento paziente
            rilevazione.citizenId = refPaziente;

            // dati trimestrali 
            rilevazione.antropometricData = new antropometricData();

            rilevazione.antropometricData.bmi = 12.34;
            rilevazione.antropometricData.bmi_date = DefaultDate;
            rilevazione.antropometricData.height = 172;
            rilevazione.antropometricData.height_date = DefaultDate;
            rilevazione.antropometricData.waistcircumference = 80;
            rilevazione.antropometricData.waistcircumference_date = DefaultDate;
            rilevazione.antropometricData.weight = 67.89;
            rilevazione.antropometricData.weight_date = DefaultDate;

            rilevazione.educativeReinforcement = new educativeReinforcement();

            rilevazione.educativeReinforcement.footPrevention = true;
            rilevazione.educativeReinforcement.footPrevention_date = DefaultDate;
            rilevazione.educativeReinforcement.nutrition = true;
            rilevazione.educativeReinforcement.nutrition_date = DefaultDate;
            rilevazione.educativeReinforcement.selfMonitoring = true;
            rilevazione.educativeReinforcement.selfMonitoring_date = DefaultDate;
            rilevazione.educativeReinforcement.sport = true;
            rilevazione.educativeReinforcement.sport_date = DefaultDate;

            rilevazione.bloodPressureMax = 180;
            rilevazione.bloodPressureMax_date = DefaultDate;

            rilevazione.bloodPressureMin = 80;
            rilevazione.bloodPressureMin_date = DefaultDate;

            rilevazione.glicemicSelfMonitoring = true;
            rilevazione.glicemicSelfMonitoring_date = DefaultDate;

            // dati semestrali
            rilevazione.footInspection = true;
            rilevazione.footInspection_date = DefaultDate;
            rilevazione.hbA1c = 23.45;
            rilevazione.hbA1c_date = DefaultDate;

            // + delta annuali
            rilevazione.ALT = 123;
            rilevazione.ALT_date = DefaultDate;
            rilevazione.AST = 34;
            rilevazione.AST_date = DefaultDate;
            rilevazione.CVRiskCalculation = 45;
            rilevazione.CVRiskCalculation_date = DefaultDate;
            rilevazione.cardiologicVisit = true;
            rilevazione.cardiologicVisit_date = DefaultDate;
            rilevazione.colesteroloLDL = 200;
            rilevazione.colesteroloLDL_date = DefaultDate;
            rilevazione.colesteroloTot = 300;
            rilevazione.colesteroloTot_date = DefaultDate;
            rilevazione.creatinemia = 12.34;
            rilevazione.creatinemia_date = DefaultDate;
            rilevazione.diabetologicVisit = true;
            rilevazione.diabetologicVisit_date = DefaultDate;
            rilevazione.ECG = true;
            rilevazione.ECG_date = DefaultDate;
            rilevazione.emocromopFp = "bianchi, rossi";
            rilevazione.emocromopFp_date = DefaultDate;
            rilevazione.fundus = true;
            rilevazione.fundus_date = DefaultDate;
            rilevazione.GGT = 56;
            rilevazione.GGT_date = DefaultDate;
            rilevazione.microalbuminuria = 54.321;
            rilevazione.microalbuminuria_date = DefaultDate;
            rilevazione.nephrologicVisit = true;
            rilevazione.nephrologicVisit_date = DefaultDate;
            rilevazione.neurologicVisit = true;
            rilevazione.neurologicVisit_date = DefaultDate;
            rilevazione.oculisticVisit = true;
            rilevazione.oculisticVisit_date = DefaultDate;
            rilevazione.trigliceridi = 432;
            rilevazione.trigliceridi_date = DefaultDate;
            rilevazione.uricemia = 65.43;
            rilevazione.uricemia_date = DefaultDate;

            try
            {
                result = RilevazioneWS.doWriteStartUpPatientData(st2, refMMG1, rilevazione, 3, 2015);
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
            }

            txtLog.AppendText("Servizio doWriteStartUpPatientData invocato con successo" + (result ? "OK" : "NO") + Environment.NewLine);

        }


        private void btRilevazioneTrimestrale_Click(object sender, EventArgs e)
        {
            bool result = false;

            quarterRelevation rilevazione = new quarterRelevation();

            rilevazione.antropometricData = new antropometricData();

            //-<xs:complexType name="quarterRelevation">
            //-<xs:sequence>
            //<xs:element name="antropometricData" type="tns:antropometricData" minOccurs="0"/>
            //<xs:element name="bloodPressureMax" type="xs:int"/>
            //<xs:element name="bloodPressureMax_date" type="xs:dateTime" minOccurs="0"/>
            //<xs:element name="bloodPressureMin" type="xs:int"/>
            //<xs:element name="bloodPressureMin_date" type="xs:dateTime" minOccurs="0"/>
            //<xs:element name="citizenId" type="xs:string"/>
            //<xs:element name="educativeReinforcement" type="tns:educativeReinforcement" minOccurs="0"/>
            //<xs:element name="glicemicSelfMonitoring" type="xs:boolean"/>
            //<xs:element name="glicemicSelfMonitoring_date" type="xs:dateTime" minOccurs="0"/>
            //</xs:sequence>
            //</xs:complexType>
            //-<xs:complexType name="antropometricData">
            //-<xs:sequence>
            //<xs:element name="bmi" type="xs:double"/>
            //<xs:element name="bmi_date" type="xs:dateTime" minOccurs="0"/>
            //<xs:element name="height" type="xs:int"/>
            //<xs:element name="height_date" type="xs:dateTime"/>
            //<xs:element name="waistcircumference" type="xs:int"/>
            //<xs:element name="waistcircumference_date" type="xs:dateTime" minOccurs="0"/>
            //<xs:element name="weight" type="xs:double"/>
            //<xs:element name="weight_date" type="xs:dateTime"/>
            //</xs:sequence>
            //</xs:complexType>
            //-<xs:complexType name="educativeReinforcement">
            //-<xs:sequence>
            //<xs:element name="footPrevention" type="xs:boolean"/>
            //<xs:element name="footPrevention_date" type="xs:dateTime" minOccurs="0"/>
            //<xs:element name="nutrition" type="xs:boolean"/>
            //<xs:element name="nutrition_date" type="xs:dateTime" minOccurs="0"/>
            //<xs:element name="selfMonitoring" type="xs:boolean"/>
            //<xs:element name="selfMonitoring_date" type="xs:dateTime" minOccurs="0"/>
            //<xs:element name="sport" type="xs:boolean"/>
            //<xs:element name="sport_date" type="xs:dateTime" minOccurs="0"/>
            //</xs:sequence>
            //</xs:complexType>

            // prima si setta la chiave primaria che ogni MMG avrà salvato al momento dell'arruolamento paziente
            // rilevazione.citizenId = "wFGLWv1pTdPEEC0qR1ToEWacsVJZcqDByMt+2LHqM2s=";
            rilevazione.citizenId = refPaziente;         

            rilevazione.antropometricData.bmi = 12.34;
            rilevazione.antropometricData.bmi_date = DefaultDate;
            rilevazione.antropometricData.height = 172;
            rilevazione.antropometricData.height_date = DefaultDate;
            rilevazione.antropometricData.waistcircumference = 80;
            rilevazione.antropometricData.waistcircumference_date = DefaultDate;
            rilevazione.antropometricData.weight = 67.89;
            rilevazione.antropometricData.weight_date = DefaultDate;

            rilevazione.educativeReinforcement = new educativeReinforcement();

            rilevazione.educativeReinforcement.footPrevention = true;
            rilevazione.educativeReinforcement.footPrevention_date = DefaultDate;
            rilevazione.educativeReinforcement.nutrition = true;
            rilevazione.educativeReinforcement.nutrition_date = DefaultDate;
            rilevazione.educativeReinforcement.selfMonitoring = true;
            rilevazione.educativeReinforcement.selfMonitoring_date = DefaultDate;
            rilevazione.educativeReinforcement.sport = true;
            rilevazione.educativeReinforcement.sport_date = DefaultDate;

            rilevazione.bloodPressureMax = 180;
            rilevazione.bloodPressureMax_date = DefaultDate;

            rilevazione.bloodPressureMin = 80;
            rilevazione.bloodPressureMin_date = DefaultDate;

            rilevazione.glicemicSelfMonitoring = true;
            rilevazione.glicemicSelfMonitoring_date = DefaultDate;

            try
            {
                result = RilevazioneWS.doWriteQuarterPatientData(st2, refMMG1, rilevazione, 3, 2015);
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
            }

            txtLog.AppendText("Servizio doWriteQuarterPatientData invocato con successo" + (result ? "OK" : "NO") + Environment.NewLine);

        }


        private void btRilevazioneSemestrale_Click(object sender, EventArgs e)
        {
            //<xs:complexType name="doWriteSemestralPatientData">
            //<xs:sequence>
            //<xs:element name="securityToken" type="tns:authorizationToken"/>
            //<xs:element name="MMGCodFisc" type="xs:string"/>
            //<xs:element name="relevation" type="tns:semestralRelevation"/>
            //<xs:element name="quarter" type="xs:int"/>
            //<xs:element name="year" type="xs:int"/>
            //</xs:sequence>
            //</xs:complexType>
            // quarter + i seguenti
            //<xs:complexType name="semestralRelevation">
            //<xs:complexContent>
            //<xs:extension base="tns:quarterRelevation">
            //<xs:sequence>
            //<xs:element name="footInspection" type="xs:boolean"/>
            //<xs:element minOccurs="0" name="footInspection_date" type="xs:dateTime"/>
            //<xs:element name="hbA1c" type="xs:double"/>
            //<xs:element minOccurs="0" name="hbA1c_date" type="xs:dateTime"/>
            //</xs:sequence>
            //</xs:extension>
            //</xs:complexContent>
            //</xs:complexType>

            bool result = false;

            semestralRelevation rilevazione = new semestralRelevation();

            // prima si setta la chiave primaria che ogni MMG avrà salvato al momento dell'arruolamento paziente
            rilevazione.citizenId = refPaziente;

            rilevazione.antropometricData = new antropometricData();

            rilevazione.antropometricData.bmi = 12.34;
            rilevazione.antropometricData.bmi_date = DefaultDate;
            rilevazione.antropometricData.height = 172;
            rilevazione.antropometricData.height_date = DefaultDate;
            rilevazione.antropometricData.waistcircumference = 80;
            rilevazione.antropometricData.waistcircumference_date = DefaultDate;
            rilevazione.antropometricData.weight = 67.89;
            rilevazione.antropometricData.weight_date = DefaultDate;

            rilevazione.educativeReinforcement = new educativeReinforcement();

            rilevazione.educativeReinforcement.footPrevention = true;
            rilevazione.educativeReinforcement.footPrevention_date = DefaultDate;
            rilevazione.educativeReinforcement.nutrition = true;
            rilevazione.educativeReinforcement.nutrition_date = DefaultDate;
            rilevazione.educativeReinforcement.selfMonitoring = true;
            rilevazione.educativeReinforcement.selfMonitoring_date = DefaultDate;
            rilevazione.educativeReinforcement.sport = true;
            rilevazione.educativeReinforcement.sport_date = DefaultDate;

            rilevazione.bloodPressureMax = 180;
            rilevazione.bloodPressureMax_date = DefaultDate;

            rilevazione.bloodPressureMin = 80;
            rilevazione.bloodPressureMin_date = DefaultDate;

            rilevazione.glicemicSelfMonitoring = true;
            rilevazione.glicemicSelfMonitoring_date = DefaultDate;

            // + delta semestrali
            //<xs:element name="footInspection" type="xs:boolean"/>
            //<xs:element minOccurs="0" name="footInspection_date" type="xs:dateTime"/>
            //<xs:element name="hbA1c" type="xs:double"/>
            //<xs:element minOccurs="0" name="hbA1c_date" type="xs:dateTime"/>

            rilevazione.footInspection = true;
            rilevazione.footInspection_date = DefaultDate;
            rilevazione.hbA1c = 23.45;
            rilevazione.hbA1c_date = DefaultDate;

            try
            {
                result = RilevazioneWS.doWriteSemestralPatientData(st2, refMMG1, rilevazione, 3, 2015);
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
            }

            txtLog.AppendText("Servizio doWriteSemestralPatientData invocato con successo" + (result ? "OK" : "NO") + Environment.NewLine);
        }


        
        private void btDatiAnnuali_Click(object sender, EventArgs e)
        {
            //<xs:complexType name="doWriteYearlyPatientData">
            //<xs:sequence>
            //<xs:element minOccurs="0" name="securityToken" type="tns:authorizationToken"/>
            //<xs:element minOccurs="0" name="MMGCodFisc" type="xs:string"/>
            //<xs:element minOccurs="0" name="relevation" type="tns:yearlyRelevation"/>
            //<xs:element name="quarter" type="xs:int"/>
            //<xs:element name="year" type="xs:int"/>
            //</xs:sequence>

            //<xs:complexType name="yearlyRelevation">
            //<xs:complexContent>
            //<xs:extension base="tns:semestralRelevation">
            //<xs:sequence>
            bool result = false;

            yearlyRelevation rilevazione = new yearlyRelevation();

            // prima si setta la chiave primaria che ogni MMG avrà salvato al momento dell'arruolamento paziente
            rilevazione.citizenId = refPaziente;

            // dati trimestrali 
            rilevazione.antropometricData = new antropometricData();

            rilevazione.antropometricData.bmi = 12.34;
            rilevazione.antropometricData.bmi_date = DefaultDate;
            rilevazione.antropometricData.height = 172;
            rilevazione.antropometricData.height_date = DefaultDate;
            rilevazione.antropometricData.waistcircumference = 80;
            rilevazione.antropometricData.waistcircumference_date = DefaultDate;
            rilevazione.antropometricData.weight = 67.89;
            rilevazione.antropometricData.weight_date = DefaultDate;

            rilevazione.educativeReinforcement = new educativeReinforcement();

            rilevazione.educativeReinforcement.footPrevention = true;
            rilevazione.educativeReinforcement.footPrevention_date = DefaultDate;
            rilevazione.educativeReinforcement.nutrition = true;
            rilevazione.educativeReinforcement.nutrition_date = DefaultDate;
            rilevazione.educativeReinforcement.selfMonitoring = true;
            rilevazione.educativeReinforcement.selfMonitoring_date = DefaultDate;
            rilevazione.educativeReinforcement.sport = true;
            rilevazione.educativeReinforcement.sport_date = DefaultDate;

            rilevazione.bloodPressureMax = 180;
            rilevazione.bloodPressureMax_date = DefaultDate;

            rilevazione.bloodPressureMin = 80;
            rilevazione.bloodPressureMin_date = DefaultDate;

            rilevazione.glicemicSelfMonitoring = true;
            rilevazione.glicemicSelfMonitoring_date = DefaultDate;

            // dati semestrali
            rilevazione.footInspection = true;
            rilevazione.footInspection_date = DefaultDate;
            rilevazione.hbA1c = 23.45;
            rilevazione.hbA1c_date = DefaultDate;

            // + delta annuali
            //<xs:element name="ALT" type="xs:int"/>
            //<xs:element minOccurs="0" name="ALT_date" type="xs:dateTime"/>
            //<xs:element name="AST" type="xs:int"/>
            //<xs:element minOccurs="0" name="AST_date" type="xs:dateTime"/>
            //<xs:element name="CVRiskCalculation" type="xs:int"/>
            //<xs:element minOccurs="0" name="CVRiskCalculation_date" type="xs:dateTime"/>
            //<xs:element name="cardiologicVisit" type="xs:boolean"/>
            //<xs:element minOccurs="0" name="cardiologicVisit_date" type="xs:dateTime"/>
            //<xs:element name="colesteroloLDL" type="xs:double"/>
            //<xs:element minOccurs="0" name="colesteroloLDL_date" type="xs:dateTime"/>
            //<xs:element name="colesteroloTot" type="xs:double"/>
            //<xs:element minOccurs="0" name="colesteroloTot_date" type="xs:dateTime"/>
            //<xs:element name="creatinemia" type="xs:double"/>
            //<xs:element minOccurs="0" name="creatinemia_date" type="xs:dateTime"/>
            //<xs:element name="diabetologicVisit" type="xs:boolean"/>
            //<xs:element minOccurs="0" name="diabetologicVisit_date" type="xs:dateTime"/>
            //<xs:element name="ECG" type="xs:boolean"/>
            //<xs:element minOccurs="0" name="ECG_date" type="xs:dateTime"/>
            //<xs:element minOccurs="0" name="emocromopFp" type="xs:string"/>
            //<xs:element minOccurs="0" name="emocromopFp_date" type="xs:dateTime"/>
            //<xs:element name="fundus" type="xs:boolean"/>
            //<xs:element minOccurs="0" name="fundus_date" type="xs:dateTime"/>
            //<xs:element name="GGT" type="xs:int"/>
            //<xs:element minOccurs="0" name="GGT_date" type="xs:dateTime"/>
            //<xs:element name="microalbuminuria" type="xs:double"/>
            //<xs:element minOccurs="0" name="microalbuminuria_date" type="xs:dateTime"/>
            //<xs:element name="nephrologicVisit" type="xs:boolean"/>
            //<xs:element minOccurs="0" name="nephrologicVisit_date" type="xs:dateTime"/>
            //<xs:element name="neurologicVisit" type="xs:boolean"/>
            //<xs:element minOccurs="0" name="neurologicVisit_date" type="xs:dateTime"/>
            //<xs:element name="oculisticVisit" type="xs:boolean"/>
            //<xs:element minOccurs="0" name="oculisticVisit_date" type="xs:dateTime"/>
            //<xs:element name="trigliceridi" type="xs:double"/>
            //<xs:element minOccurs="0" name="trigliceridi_date" type="xs:dateTime"/>
            //<xs:element name="uricemia" type="xs:double"/>
            //<xs:element minOccurs="0" name="uricemia_date" type="xs:dateTime"/>

            rilevazione.ALT = 123;
            rilevazione.ALT_date = DefaultDate;
            rilevazione.AST = 34;
            rilevazione.AST_date = DefaultDate;
            rilevazione.CVRiskCalculation = 45;
            rilevazione.CVRiskCalculation_date = DefaultDate;
            rilevazione.cardiologicVisit = true;
            rilevazione.cardiologicVisit_date = DefaultDate;
            rilevazione.colesteroloLDL = 200;
            rilevazione.colesteroloLDL_date = DefaultDate;
            rilevazione.colesteroloTot = 300;
            rilevazione.colesteroloTot_date = DefaultDate;
            rilevazione.creatinemia = 12.34;
            rilevazione.creatinemia_date = DefaultDate;
            rilevazione.diabetologicVisit = true;
            rilevazione.diabetologicVisit_date = DefaultDate;
            rilevazione.ECG = true;
            rilevazione.ECG_date = DefaultDate;
            rilevazione.emocromopFp = "bianchi, rossi";
            rilevazione.emocromopFp_date = DefaultDate;
            rilevazione.fundus = true;
            rilevazione.fundus_date = DefaultDate;
            rilevazione.GGT = 56;
            rilevazione.GGT_date = DefaultDate;
            rilevazione.microalbuminuria = 54.321;
            rilevazione.microalbuminuria_date = DefaultDate;
            rilevazione.nephrologicVisit = true;
            rilevazione.nephrologicVisit_date = DefaultDate;
            rilevazione.neurologicVisit = true;
            rilevazione.neurologicVisit_date = DefaultDate;
            rilevazione.oculisticVisit = true;
            rilevazione.oculisticVisit_date = DefaultDate;
            rilevazione.trigliceridi = 432;
            rilevazione.trigliceridi_date = DefaultDate;
            rilevazione.uricemia = 65.43;
            rilevazione.uricemia_date = DefaultDate;


            try
            {
                result = RilevazioneWS.doWriteYearlyPatientData(st2, refMMG1, rilevazione, 3, 2015);
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
            }

            txtLog.AppendText("Servizio doWriteYearlyPatientData invocato con successo" + (result ? "OK" : "NO") + Environment.NewLine);
        }


        private void btFineRilevazione_Click(object sender, EventArgs e)
        {
            //<xs:complexType name="doEndRelevation">
            //<xs:sequence>
            //<xs:element minOccurs="0" name="securityToken" type="tns:authorizationToken"/>
            //<xs:element minOccurs="0" name="patientCodFisc" type="xs:string"/>
            //<xs:element name="quarter" type="xs:int"/>
            //<xs:element name="year" type="xs:int"/>
            //</xs:sequence>
            int result = 0;

            try
            {
                result = RilevazioneWS.doEndRelevation(st2, refMMG1);
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
            }

            txtLog.AppendText("Servizio doEndRelevation invocato con successo: " + result + " pazienti caricati" + Environment.NewLine);
        }
        #endregion datirilevazione


    }
}
