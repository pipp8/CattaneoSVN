/*
 $Author: cattaneo $
 $Date: 2011-05-14 19:35:42 +0200(Sab, 14 Mag 2011) $
 $Revision: 14 $
 */

#include <iostream>
#include <fstream>
#include <time.h>

#include "soapWebApiSoapBindingProxy.h"
#include "WebApiSoapBinding.nsmap"

using namespace std;

typedef struct apachesoap__DataHandler {
	std::string href;
} apachesoap__DataHandler;


int main (int argc, char * const argv[]) {

	WebApiSoapBinding service;
	
	_ns1__login request1;
	request1.usrApp = "unisa";
	request1.pwdApp = "unisa";

	_ns1__loginResponse response1;

	if (service.__ns1__login( &request1, &response1) == SOAP_OK)
		std::cout << "Login got: " << response1.loginReturn << std::endl;
	else
	{
		std::cerr << "Call to Login failed" << std::endl;
		return 0;
	}
	
	ns1__MetaDato utente;
	utente.nome = "utente_nome";
	utente.valore = "valore_NomeUtente";

	ns1__MetaDato username1;
	username1.nome = "utente_user";
	username1.valore = "yyi9199";

	ns1__MetaDato oggetto;
	oggetto.nome = "oggetto";
	oggetto.valore = "valore_Oggetto";

	ns1__MetaDato dataRichiesta;
	dataRichiesta.nome = "data_richiesta";
	dataRichiesta.valore = "26/04/2011";

	ns1__MetaDato ** aom = new ns1__MetaDato *[4];
	aom[0] = &username1;
	aom[1] = &utente;
	aom[2] = &oggetto;
	aom[3] = &dataRichiesta;

	_ns1__attivaProcedimento request2;
	
	request2.token = response1.loginReturn;
	request2.username = "yyi9199";
	request2.idAmministrazione = (long) 2;
	request2.idAoo = (long) 2;
	request2.codFlusso = "PEF01";
	request2.nomeDocumento = "Nome Documento (descrizione)";
	request2.nomeFile = "ttt.txt";
	request2.__sizemetaDatiDocumento = 4;
	request2.metaDatiDocumento = aom;
	request2.__sizemetaDatiProcedimento = 0;
	request2.metaDatiProcedimento = NULL;
	
	_ns1__attivaProcedimentoResponse response2;
	
	ifstream::pos_type attachLen;
	char * attachData;
	
    // NSString *path = [[NSBundle mainBundle] pathForResource:@"rainy" ofType:@"png"];
    
	ifstream file ("ttt.txt", ios::in|ios::binary);
	if (file.is_open())
	{
		file.seekg(0, ios::end);
		attachLen = file.tellg();
		attachData = new char [attachLen];
		file.seekg (0, ios::beg);
		file.read (attachData, attachLen);
		file.close();
		
		cout << "the complete file content is in memory" << endl;
	}
	else 
	{
		cout << "Unable to open file";
		exit(-1);
	}
	
	std::string strContentID = "183648746758934743"; // some unique string
	
	soap_set_mime(service.soap, NULL, NULL);
	soap_set_mime_attachment(service.soap, attachData, attachLen, SOAP_MIME_BINARY,
							 "plain/txt", std::string("<" + strContentID + ">").c_str(), NULL, NULL);
	
	apachesoap__DataHandler fileContent;
	fileContent.href = std::string("cid:" + strContentID);
	
	int result = service.__ns1__attivaProcedimento( &request2, &response2);
	// soap_call_ns1__uploadFile(&soap, NULL, NULL, "/some/path/on/the/server/", fileContent, respUpload));
	
	soap_clr_mime(service.soap);
	delete[] attachData;
	//
	// if (service.error)
	//	service.soap_stream_fault(std::cerr);
	
	if (result == SOAP_OK) {
		std::cout << "AttivaProcedimento ok: got: " << std::endl;
		std::cout << "Data Inizio: " << ctime( response2.attivaProcedimentoReturn->dataOraInizio) << std::endl;
		if (response2.attivaProcedimentoReturn->dataOraFine > 0)
            std::cout << "Data Fine:   " << ctime( response2.attivaProcedimentoReturn->dataOraFine) << std::endl;
		std::cout << "Descrizione: " << response2.attivaProcedimentoReturn->descrizione << std::endl;
		std::cout << "Owner:       " << response2.attivaProcedimentoReturn->descrizioneOwner << std::endl;
		std::cout << "ID:          " << response2.attivaProcedimentoReturn->id << std::endl;
		std::cout << "Repertorio:  " << response2.attivaProcedimentoReturn->numeroRepertorio << std::endl;
		std::cout << "Richiedente: " << response2.attivaProcedimentoReturn->richiedente << std::endl;
        std::cout << "UsernameOwner:" << response2.attivaProcedimentoReturn->usernameOwner << std::endl;
	}
    else {
		std::cerr << "Call to AttivaProcedimento failed" << std::endl;
		return 0;
	}
	
    _ns1__aggiungiDocumentoAlProcedimento request3;
    
    request3.token = response1.loginReturn;
	request3.idAmministrazione = (long) 2;
	request3.idAoo = (long) 2;
	request3.idProcedimento = response2.attivaProcedimentoReturn->id;
	request3.nomeDocumento = "Documento2 aggiunto";
	request3.nomeFile = "DipartimentoInformatica.png";
	request3.__sizemetaDatiDocumento = 4;
	request3.metaDatiDocumento = aom;
	
    _ns1__aggiungiDocumentoAlProcedimentoResponse response3;
    
    ifstream file2 ("DipartimentoInformatica.png", ios::in|ios::binary);
	if (file2.is_open())
	{
		file2.seekg(0, ios::end);
		attachLen = file2.tellg();
		attachData = new char [attachLen];
		file2.seekg (0, ios::beg);
		file2.read (attachData, attachLen);
		file2.close();
		
		cout << "the complete file2 content is in memory" << std::endl;
    }
	else 
	{
		cout << "Unable to open file";
		exit(-1);
	}
	
	strContentID = "183648746758934745"; // some unique string
	
	soap_set_mime(service.soap, NULL, NULL);
	soap_set_mime_attachment(service.soap, attachData, attachLen, SOAP_MIME_BINARY,
							 "image/jpeg", std::string("<" + strContentID + ">").c_str(), NULL, NULL);
	
	fileContent.href = std::string("cid:" + strContentID);
	
	result = service.__ns1__aggiungiDocumentoAlProcedimento(&request3, &response3); 
  	
	soap_clr_mime(service.soap);
	delete[] attachData;
	
	if (result == SOAP_OK) {
        std::cout << "Aggiungi Documento a Procedimento ok: got: (void)" << std::endl;
    }
  }

/*
 ##
 # UNISA - Parametri statici per cliente/ambiente
 # =================================================
 # Cliente : UNISA
 # Ambiente: Collaudo
 # 
 # URL Collaudo:
 #
 #     * LCycle workflow services:
 #           http://www.legalcyclecl.infocert.it/wfca/services/WebApi?wsdl
 #        
 ##
 
 [Ente]
 idAmm=2
 idAoo=2
 
 [Utente webservices]
 user=unisa
 password=unisa
 
 [Codice flusso]
 codiceFlusso=PEF01
 
 [Utente owner dei flussi (se non viene valorizzato l'owner sarà yyi0687)]
 username=yyi9199
 
 [URL WEB SERVICES]
 url=http://www.legalcyclecl.infocert.it/wfca/services/WebApi?wsdl
 /** 
 * Indicazioni di integrazione con i ws Workflow
 * - webApi: è un oggetto proxy dei servizi
*/ 

/*
LOGIN Al SISTEMA:
token = webApi.login(user,password);

ATTIVA PROCEDIMENTO:
//Definizione dei metadati
MetaDato utente= new MetaDato();
utente.setNome("utente_nome");
utente.setValore(valore_nomeutente);

MetaDato username= new MetaDato();
username.setNome("utente_user");
username.setValore(valore_username);

MetaDato oggetto= new MetaDato();
oggetto.setNome("oggetto");
oggetto.setValore(valore_oggetto);

MetaDato dataRichiesta= new MetaDato();
dataRichiesta.setNome("data_richuiesta");
dataRichiesta.setValore(valore_datarichiesta);

MetaDato[] metaDatiDocumento=new MetaDato[4];
metaDatiDocumento[1]=utente;
metaDatiDocumento[0]=username;
metaDatiDocumento[2]=oggetto;
metaDatiDocumento[3]=nome;

//Attivazione del procedimento
DataHandler dataHandler=new DataHandler(new FileDataSource(new File("c:/prova.pdf"));
Procedimento procedimento=webApi.attivaProcedimento(token, username,//se passo null l'owner è yyi0687 
                                                    idAmm, idAoo, codiceFlusso,nome_documento,
                                                    nome_file, metaDatiDocumento, null, dataHandler);
                                        
    // Classe utilizzata da webapi
    //
package it.infococert.wfca.client;
                                        
import java.net.URL;                                        
import javax.activation.DataHandler;
                                        
import org.apache.axis.attachments.AttachmentPart;
import org.apache.log4j.Logger;
                                        
import it.infocert.wfca.webapi.Flusso;
import it.infocert.wfca.webapi.MetaDato;
import it.infocert.wfca.webapi.Procedimento;
import it.infocert.wfca.webapi.WebApiServiceLocator;
import it.infocert.wfca.webapi.WebApiSoapBindingStub;
                                        
public class WfcaWebApi{
    private Logger logger = Logger.getLogger(WfcaWebApi.class.getName());
    private WebApiSoapBindingStub services;
                                            
    public WfcaWebApi(String url)throws Exception{
        try {
            WebApiServiceLocator locatorServices = new WebApiServiceLocator();
            URL urlNet  = new URL(url);
            services =  (WebApiSoapBindingStub) locatorServices.getWebApi(urlNet);
        }
        catch (Exception e) {
            logger.error(e,e);
            throw e;
        }
    }

     public Procedimento attivaProcedimento(String token, String username,
                                            long idAmministrazione, long idAoo, String codFlusso,
                                            String nomeDocumento, String nomeFile,
                                            MetaDato[] metaDatiDocumento, MetaDato[] metaDatiProcedimento,DataHandler dataHandler) throws Exception {
     
        AttachmentPart attachment = new AttachmentPart();
        attachment.setDataHandler(dataHandler);
        services.addAttachment(attachment);
        return services.attivaProcedimento(token, username, idAmministrazione, idAoo, codFlusso, nomeDocumento, nomeFile, metaDatiDocumento, metaDatiProcedimento);
     }
     
     public String login(String usrApp, String pwdApp) throws Exception {
         return services.login(usrApp, pwdApp);
     }
 }
                                        
*/
