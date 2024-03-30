//
//  RemoteCommunication.m
//  iOTP
//
//  Created by Giuseppe Cattaneo on 21/04/15.
//  Copyright (c) 2015 Giuseppe Cattaneo. All rights reserved.
//

#import "RemoteCommunication.h"

#import "TuitusOTP.h"

#import "iOTPMisc.h"



@interface RemoteCommunication ()

@property (nonatomic, strong) NSString *remoteSeed;
@property (nonatomic, strong) NSString *serverDate;

@property (nonatomic, strong) NSMutableData *receivedData;
@property (nonatomic, strong) NSURLConnection *connection;
@property (nonatomic, strong) NSString *cookieL1Str;
@property (nonatomic, strong) NSDictionary * cookiesL1Dict;

@property (nonatomic, strong) NSMutableDictionary * errorMessages;

@property id<RestartViewControllerDelegate> target;

@property UIAlertView *errorAlert;
@property UIAlertView *infoAlert;

@end

/*
 * Protocollo:
 
 btStep1 clicked ->
 openSession
 requestSMS
 
 ricevi l'SMS, lo digita
 btStep2 clicked
 sendSMS
 getSeed
 closeSession
 */

/*
 POST /isac-rest/session (autenticazione di primo livello)
 Parametri:
 "login" (form-param) - il login dell'utente (userid, nickname, ...)
 "password" (form-param) - la password dell'utente
 Cookie: isac-app - da valorizzare con una stringa concordata e sempre uguale che identifica l'applicazione chiamante
 Risposte:
 201 - Sessione creata con successo, cookies: ["access-token" con il valore del token di sessione] ["csrf-token" con il valore del token anti-csrf]
 401 - Autenticazione non valida (es. password errata, userid non trovato, ecc)
 402 - Password bloccata
 410 - Password scaduta
 */

@implementation RemoteCommunication

- (id) init {
    
    self = [super init];
    
    self.provisioningSrv = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"ProvisioningServer"];
    
    NSString *storyboard  = [[NSBundle mainBundle].infoDictionary objectForKey:@"UIMainStoryboardFile"];
    NSLog(@"%@", storyboard);
    
    [self initErrorMessages];
    
    [self resetCommunication];
    
    return self;
}


- (void) test1: (id) callBackObj {
    
    self.target = callBackObj;
    
    self.progressStatus = OTPSMSRequested;
    
    [self fineStep1];
}

- (void) test2: (id) callBackObj {
    
    self.target = callBackObj;
    self.remoteSeed = @"1234567890";
    
    [self fineStep2];
}


//
// ----------------------------------------------------------------
//

- (void) fineStep1 {
    
    self.progressStatus = OTPSMSRequested;
    
    // riattiva la GUI
    //[[UIApplication sharedApplication] endIgnoringInteractionEvents];
    
    // resetta il cursore dell'App
    [[UIApplication sharedApplication] setNetworkActivityIndicatorVisible:NO];
    
    self.infoAlert = [[UIAlertView alloc]
                              initWithTitle: NSLocalizedString(@"NOTIFY-TITLE", nil)
                              message: NSLocalizedString(@"SMSREQUESTED-MSG", nil)
                              delegate:self
                              cancelButtonTitle:NSLocalizedString(@"OK-BT", nil)
                              otherButtonTitles:nil];
    [self.infoAlert  show];
    
    // instanzia il nuovo view controller
    //[self performSegueWithIdentifier:@"storyboard id" sender:self]
    // alertview handler ritornerà al controller delegate
}


- (void) fineStep2 {
    
    [self resetCommunication];
    self.connection = nil;
    
    self.progressStatus = Terminated;
    
    // riattiva la GUI
    // [[UIApplication sharedApplication] endIgnoringInteractionEvents];
    
    // resetta il cursore dell'App
    [[UIApplication sharedApplication] setNetworkActivityIndicatorVisible:NO];
    
    // ritorna alla secopnda view
    [self.target restartView: self.remoteSeed result: YES];

    // la seconda view provvederà a salvare il seed
}


- (void) alertView: (UIAlertView *) alertView clickedButtonAtIndex:(NSInteger)buttonIndex{
    
    if (alertView == self.errorAlert) {
        // gestione dell'errore
        [self.target restartView: @"" result: NO];

    }
    else {
        switch (self.progressStatus) {
          
            case OTPSMSRequested:
                // ritorna alla prima view
                [self.target restartView: @"" result: YES];
                break;
        
            case SendConfirm:
                // chiude la sessione dopo aver stampato il clock skew
                [self closeSession];
                break;
        
            case Terminated:
                // ritorna alla secopnda view
                [self.target restartView: self.remoteSeed result: YES];
                break;
                
            default:
                [self.target restartView: @"" result: YES];
                break;
        }
    }
}



- (void) openSession: (NSString *) user withPassword: (NSString *) password
              target: (id) callBackObj {

    self.target = callBackObj;
    
    self.progressStatus = LoginReq;
    
    NSString * srvPath = @"isac-rest/session";
    
    NSString * url = [NSString stringWithFormat: @"%@/%@", self.provisioningSrv, srvPath];
    
    // Create the request.
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[NSURL URLWithString: url]];
    
    // Set the request's content type to application/x-www-form-urlencoded
    
    [request setValue:@"application/x-www-form-urlencoded" forHTTPHeaderField:@"Content-Type"];
    
    // Convert your data and set your request's HTTPBody property
    // Designate the request a POST request and specify its body data
    
    [request setHTTPMethod:@"POST"];
    
    // In body data for the 'application/x-www-form-urlencoded' content type,
    // form fields are separated by an ampersand. Note the absence of a leading ampersand.
    NSString *bodyData = [NSString stringWithFormat: @"login=%@&password=%@", user, password];
    // NSString *bodyData = @"login=pompeo003@gmail.com&password=Infocert1"; // OK
    
    NSData *requestBodyData = [bodyData dataUsingEncoding:NSUTF8StringEncoding];
    
    // NSData * requestBodyData = [NSData dataWithBytes:[bodyData UTF8String] length:strlen([bodyData UTF8String])];
    [request setHTTPBody: requestBodyData];
    
    // Create url connection and fire request
    // Create the NSMutableData to hold the received data.
    // receivedData is an instance variable declared elsewhere.
    self.receivedData = [NSMutableData dataWithCapacity: 0];
    
    // create the connection with the request
    // and start loading the data
    
    self.connection = [[NSURLConnection alloc] initWithRequest:request delegate:self];
    
    NSLog(@"Request sent (status:%d)", (int) self.progressStatus);
    
    if (!self.connection) {
        // Release the receivedData object.
        self.receivedData = nil;
        // Inform the user that the connection failed.
        return;
    }
}




/*
 POST /isac-rest/session/sms (invio OTP SMS)
 Parametri:
 "X-CSRFToken" (header-param) - da valorizzare con il token anti-csrf
 Cookie: csrf-token - da valorizzare con il token anti-csrf
 Cookie: access-token - da valorizzare con l'access token
 Cookie: isac-app - nome dell'app
 Risposte:
 204 - OTP inviato con successo, se valorizzato l'header "X-PRE-OTP" contiene un prefisso al OTP da mostrare all'utente (es. OTP: ABC-12345678, l'header contiene "ABC")
 400 - token anti-csrf non valido o non coincidente tra header e cookie
 401 - tentativi di invio OTP terminati per la sessione
 404 - sessione non trovata o account senza cellulare associato
 406 - sessione già di livello 2
 */

- (void) requestSMS {
    
    self.progressStatus = OTPSMSRequest;
    
    NSString * srvPath = @"isac-rest/session/sms";
    
    NSString * url = [NSString stringWithFormat: @"%@/%@", self.provisioningSrv, srvPath];
    
    // Create the request.
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[NSURL URLWithString: url]];
    
    // Set the request's content type to application/x-www-form-urlencoded
    
    [request setValue:@"application/x-www-form-urlencoded" forHTTPHeaderField:@"Content-Type"];
    
    // Convert your data and set your request's HTTPBody property
    // Designate the request a POST request and specify its body data
    
    [request setHTTPMethod:@"POST"];
    
    // setta l'Header param X-CSRFToken con il valore del token ricevuto
    [request addValue:[self.cookiesL1Dict valueForKey: @"csrf-token"]
   forHTTPHeaderField:@"X-CSRFToken"];
    
    // setta i cookies della richieste
    [request addValue:self.cookieL1Str forHTTPHeaderField:@"Set-Cookie"];
    
    // In body data for the 'application/x-www-form-urlencoded' content type,
    // form fields are separated by an ampersand. Note the absence of a leading ampersand.
    NSString *bodyData = @"";
    
    NSData *requestBodyData = [bodyData dataUsingEncoding:NSUTF8StringEncoding];
    
    [request setHTTPBody: requestBodyData];
    
    // Create url connection and fire request
    // Create the NSMutableData to hold the received data.
    // receivedData is an instance variable declared elsewhere.
    self.receivedData = [NSMutableData dataWithCapacity: 0];
    
    // create the connection with the request
    // and start loading the data
    
    self.connection = [[NSURLConnection alloc] initWithRequest:request delegate:self];
    
    NSLog(@"Request sent (status:%d)", (int) self.progressStatus);
    
    if (!self.connection) {
        // Release the receivedData object.
        self.receivedData = nil;
        // Inform the user that the connection failed.
        return;
    }
}



/*
 PUT /isac-rest/session/sms (verifica OTP SMS)
 Parametri:
 Nel body della richiesta: il valore del OTP inserito dall'utente
 "X-CSRFToken" (header-param) - da valorizzare con il token anti-csrf
 Cookie: csrf-token - da valorizzare con il token anti-csrf
 Cookie: access-token - da valorizzare con l'access token
 Cookie: isac-app - nome dell'app
 Risposte:
 204 - OTP verificato con successo
 400 - token anti-csrf non valido o non coincidente tra header e cookie, OTP non valido (esempio contiene lettere o è troppo corto)
 401 - tentativi di verifica OTP terminati per la sessione
 403 - OTP valido ma non corretto
 404 - sessione non trovata
 */
- (void) sendSMS: (NSString *) smsOTP target: (id) callBackObj {
    
    self.target = (id<RestartViewControllerDelegate>) callBackObj;
    
    self.progressStatus = OTPVerifyRequest;
    
    NSString * srvPath = @"isac-rest/session/sms";
    
    NSString * url = [NSString stringWithFormat: @"%@/%@", self.provisioningSrv, srvPath];
    
    // Create the request.
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[NSURL URLWithString: url]];
    
    // Set the request's content type to application/x-www-form-urlencoded
    
    [request setValue:@"application/x-www-form-urlencoded" forHTTPHeaderField:@"Content-Type"];
    
    // Convert your data and set your request's HTTPBody property
    // Designate the request a POST request and specify its body data
    
    [request setHTTPMethod:@"PUT"];
    
    // setta l'Header param X-CSRFToken con il valore del token ricevuto
    [request addValue:[self.cookiesL1Dict valueForKey: @"csrf-token"]
   forHTTPHeaderField:@"X-CSRFToken"];
    
    // setta i cookies della richieste
    [request addValue:self.cookieL1Str forHTTPHeaderField:@"Set-Cookie"];
    
    // In body data for the 'application/x-www-form-urlencoded' content type
    // invia l'SMS senza nome campo
    NSData *requestBodyData = [smsOTP dataUsingEncoding:NSUTF8StringEncoding];
    
    [request setHTTPBody: requestBodyData];
    
    // Create url connection and fire request
    // Create the NSMutableData to hold the received data.
    // receivedData is an instance variable declared elsewhere.
    self.receivedData = [NSMutableData dataWithCapacity: 0];
    
    // create the connection with the request
    // and start loading the data
    
    self.connection = [[NSURLConnection alloc] initWithRequest:request delegate:self];
    
    NSLog(@"Request sent (status:%d)", (int) self.progressStatus);
    
    if (!self.connection) {
        // Release the receivedData object.
        self.receivedData = nil;
        // Inform the user that the connection failed.
        return;
    }
}


/*
 GET: /identita/app/seed
 
 Parametri:
 Cookie: access-token - da valorizzare con l'access token
 Risposte:
 201 - Con SEED nel body di risposta
 401 - Il token fa riferimento ad una sessione di livello < 2
 404 - Sessione non trovata
 412 - Errore generazione SEED
 */
- (void) getSeed {
    
    self.progressStatus = SeedRequest;
    
    NSString * srvPath = @"API/lcsi-selfcare-api/identita/app/seed";
    
    NSString * url = [NSString stringWithFormat: @"%@/%@", self.provisioningSrv, srvPath];
    
    // Create the request.
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[NSURL URLWithString: url]];
    
    // Set the request's content type to application/x-www-form-urlencoded
    
    [request setValue:@"application/x-www-form-urlencoded" forHTTPHeaderField:@"Content-Type"];
    
    [request setHTTPMethod:@"GET"];
    
    // setta l'Header param X-CSRFToken con il valore del token ricevuto
    [request addValue:[self.cookiesL1Dict valueForKey: @"csrf-token"]
   forHTTPHeaderField:@"X-CSRFToken"];
    
    // setta i cookies della richieste
    [request addValue:self.cookieL1Str forHTTPHeaderField:@"Set-Cookie"];
    
    // Create url connection and fire request
    // Create the NSMutableData to hold the received data.
    // receivedData is an instance variable declared elsewhere.
    self.receivedData = [NSMutableData dataWithCapacity: 0];
    
    // create the connection with the request
    // and start loading the data
    
    self.connection = [[NSURLConnection alloc] initWithRequest:request delegate:self];
    
    NSLog(@"Request sent (status:%d)", (int) self.progressStatus);
    
    if (!self.connection) {
        // Release the receivedData object.
        self.receivedData = nil;
        // Inform the user that the connection failed.
        return;
    }
}



/*
 POST: API/lcsi-rest/identita/app/seed
 
 Parametri:
 Cookie: access-token - da valorizzare con l'access token
 Risposte:
 201 - Con OTP generato localmente nel body di risposta
 400 - Se l'OTP e' sbagliato
 */
- (void) sendConfirm {
    
    self.progressStatus = SendConfirm;
    
    NSString * srvPath = @"API/lcsi-selfcare-api/identita/app/seed";
    
    NSString * url = [NSString stringWithFormat: @"%@/%@", self.provisioningSrv, srvPath];
    
    // Create the request.
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[NSURL URLWithString: url]];
    
    // Set the request's content type to application/x-www-form-urlencoded
    
    [request setValue:@"application/x-www-form-urlencoded" forHTTPHeaderField:@"Content-Type"];
    
    [request setValue:@"POST@identita:app:seed" forHTTPHeaderField:@"X-Requested-By"];
    
    [request setHTTPMethod:@"POST"];
    
    // setta l'Header param X-CSRFToken con il valore del token ricevuto
    [request addValue:[self.cookiesL1Dict valueForKey: @"csrf-token"]
   forHTTPHeaderField:@"X-CSRFToken"];
    
    // setta i cookies della richieste
    [request addValue:self.cookieL1Str forHTTPHeaderField:@"Set-Cookie"];
    
    // calcolo il primo OTP
    time_t minutes = [[NSDate date] timeIntervalSince1970] / 60;
    
    //Proviamo sia il minuto calcolato che quelli prima e dopo
    NSString *otp = [NSString stringWithFormat: @"%@",
                     [TuitusOTP generateTimeOTP:self.remoteSeed timeData:minutes
                                         length: 8 hashType: kCCHmacAlgSHA512]];
    
    NSLog(@"Send OTP: Time %ld min., seed: %@, otp: %@",
          minutes, self.remoteSeed, otp);
    
    // In body data for the 'application/x-www-form-urlencoded' content type,
    NSData *requestBodyData = [otp dataUsingEncoding:NSUTF8StringEncoding];
    
    [request setHTTPBody: requestBodyData];
    
    // Create url connection and fire request
    // Create the NSMutableData to hold the received data.
    // receivedData is an instance variable declared elsewhere.
    self.receivedData = [NSMutableData dataWithCapacity: 0];
    
    // create the connection with the request
    // and start loading the data
    
    self.connection = [[NSURLConnection alloc] initWithRequest:request delegate:self];
    
    NSLog(@"Request sent (status:%d)", (int) self.progressStatus);
    
    if (!self.connection) {
        // Release the receivedData object.
        self.receivedData = nil;
        // Inform the user that the connection failed.
        return;
    }
}


/*
 DELETE /isac-rest/session (invalida/elimina la sessione)
 Parametri:
 Cookie: access-token - da valorizzare con l'access token
 Cookie: isac-app - nome dell'app
 Risposte:
 204 - Sessione eliminata
 */
- (void) closeSession {
    
    self.progressStatus = CloseRequest;
    
    NSString * srvPath = @"isac-rest/session";
    
    NSString * url = [NSString stringWithFormat: @"%@/%@", self.provisioningSrv, srvPath];
    
    // Create the request.
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[NSURL URLWithString: url]];
    
    // Set the request's content type to application/x-www-form-urlencoded
    
    [request setValue:@"application/x-www-form-urlencoded" forHTTPHeaderField:@"Content-Type"];
    
    // Convert your data and set your request's HTTPBody property
    // Designate the request a POST request and specify its body data
    
    [request setHTTPMethod:@"DELETE"];
    
    // setta l'Header param X-CSRFToken con il valore del token ricevuto
    [request addValue:[self.cookiesL1Dict valueForKey: @"csrf-token"]
   forHTTPHeaderField:@"X-CSRFToken"];
    
    // setta i cookies della richieste
    [request addValue:self.cookieL1Str forHTTPHeaderField:@"Set-Cookie"];
    
    // Create url connection and fire request
    // Create the NSMutableData to hold the received data.
    // receivedData is an instance variable declared elsewhere.
    self.receivedData = [NSMutableData dataWithCapacity: 0];
    
    // create the connection with the request
    // and start loading the data
    
    self.connection = [[NSURLConnection alloc] initWithRequest:request delegate:self];
    
    NSLog(@"Request sent (step:%d)", (int) self.progressStatus);
    
    if (!self.connection) {
        // Release the receivedData object.
        self.receivedData = nil;
        // Inform the user that the connection failed.
        return;
    }
}



//
// --------------------------------------------------------------------------------------
//


/*
 * metodi delegate per il protocollo NSURLConnectionDelegate
 */

- (void)connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)response
{
    
    NSString * errorDescription = nil;
    
    // This method is called when the server has determined that it
    // has enough information to create the NSURLResponse object.
    
    // It can be called multiple times, for example in the case of a
    // redirect, so each time we reset the data.
    
    [self.receivedData setLength:0];
    
    NSHTTPURLResponse *HTTPResponse = (NSHTTPURLResponse *)response;
    
    NSDictionary *fields = [HTTPResponse allHeaderFields];
    
    long code = [HTTPResponse statusCode];
    
    NSLog(@"Response Received: InternalStatus: %d, returnCode: %ld, headerExpire: %@",
          (int) self.progressStatus, code, [fields objectForKey:@"Expires"]);
    
    // gestione del codice di errore
    NSString * strCode = [NSString stringWithFormat:@"%d%ld", (int) self.progressStatus, code];
    
    if (code >= 400) {
        
        errorDescription = [_errorMessages valueForKey: strCode];
        if (errorDescription == nil) {
            errorDescription = [NSString stringWithFormat:
                                @"Errore sconosciuto code: %ld / step: %d", code, (int) self.progressStatus];
        }
        
        NSString * msg = [NSString stringWithFormat:NSLocalizedString(@"COMMERROR-MSG", nil), errorDescription];
        
        NSLog(@"Server Error:%@", msg);
        
        if (self.progressStatus == OTPVerifyRequest)
            self.progressStatus = WrongOTP;
        
        self.errorAlert = [[UIAlertView alloc]
                                   initWithTitle:NSLocalizedString(@"COMMERROR-TITLE", nil)
                                   message: msg delegate:self
                                   cancelButtonTitle:NSLocalizedString(@"OK-BT", nil)
                                   otherButtonTitles:nil];
        [self.errorAlert  show];
        // [self resetCommunication];
        // riparte dall'handler dell'AlertView e ritorna al chiamante
        
        if (self.progressStatus == SendConfirm) {
            
            // recupera dall'header l'UTC secondo le specifiche RFC 2616
            self.serverDate = [fields valueForKey:@"System-Date"]; // time sul server
            self.remoteSeed = @""; // annulla il seed ricevuto
            
        }
    }
    else {
        
        if (self.progressStatus == LoginReq) {
            
            self.cookieL1Str = [fields valueForKey:@"Set-Cookie"]; // It is your cookie
            
            self.cookiesL1Dict = [self parseCocokies: self.cookieL1Str];
            
            NSLog(@"Got L1 Cookies: %@,\ncsrf:<%@>", self.cookieL1Str,
                  [self.cookiesL1Dict valueForKey: @"csrf-token"]);
            
            // estrai la lista di cookies
            //        NSArray *allCookies =[[NSArray alloc]init];
            //        allCookies = [NSHTTPCookie
            //                   cookiesWithResponseHeaderFields:[HTTPResponse allHeaderFields]
            //                   forURL:[NSURL URLWithString:@""]]; // send to URL, return NSArray
        }
        else if (self.progressStatus == OTPVerifyRequest) {
            // non ci sono cookies e' solo alzato il livello del cooky originale
        }
        else if (self.progressStatus == SendConfirm) {
            
            // recupera dall'header l'UTC secondo le specifiche RFC 2616
            self.serverDate = [fields valueForKey:@"System-Date"]; // time sul server
            // _serverDate	@"Wed, 11 Mar 2015 15:26:19 GMT"
            NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
            
            NSLocale * enUSPOSIXLocale = [NSLocale localeWithLocaleIdentifier:@"en_US_POSIX"];
            
            [dateFormatter setLocale:enUSPOSIXLocale];
            
            [dateFormatter setDateFormat:@"EEE, dd MMM yyyy HH:mm:ss Z"];
            
            NSDate *dateOnServer = [dateFormatter dateFromString:_serverDate];
            
            NSDate *currentTime = [NSDate date];
            
            NSTimeInterval timeInterval = [currentTime timeIntervalSinceDate:dateOnServer];
            
            NSLog(@"Remote System time: %@\nLocal System Time: %@\nclock difference:%.0f (sec.)",
                  [dateFormatter stringFromDate: dateOnServer],
                  [dateFormatter stringFromDate: currentTime],
                  timeInterval);
            
            NSString * msg = [NSString stringWithFormat:NSLocalizedString(@"SEEDCONFIRMED-MSG", nil),
                              self.serverDate, timeInterval];
            
            self.infoAlert = [[UIAlertView alloc]
                                       initWithTitle:NSLocalizedString(@"SEEDRECEIVED-TITLE", nil)
                                       message: msg delegate:self
                                       cancelButtonTitle: NSLocalizedString(@"OK-BT", nil) otherButtonTitles:nil];
            [self.infoAlert  show];
        }
        
    }
}



- (void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data
{
    
    // Append the new data to receivedData.
    // receivedData is an instance variable declared elsewhere.
    
    [self.receivedData appendData:data];
    
}



- (void)connection:(NSURLConnection *)connection didFailWithError:(NSError *)error
{
    
    // Release the connection and the data object
    // by setting the properties (declared elsewhere)
    // to nil.  Note that a real-world app usually
    // requires the delegate to manage more than one
    // connection at a time, so these lines would
    // typically be replaced by code to iterate through
    // whatever data structures you are using.
    
    connection = nil;
    
    self.receivedData = nil;
    
    // inform the user
    
    NSString * msg = [NSString stringWithFormat:@"Connection failed! Error: %@ - %@",
                      [error localizedDescription],
                      [[error userInfo] objectForKey:NSURLErrorFailingURLStringErrorKey]];

    NSLog(@"%@", msg);

    self.errorAlert = [[UIAlertView alloc]
                       initWithTitle:NSLocalizedString(@"COMMERROR-TITLE", nil)
                       message: msg delegate:self
                       cancelButtonTitle:NSLocalizedString(@"OK-BT", nil)
                       otherButtonTitles:nil];
    [self.errorAlert  show];
    [self resetCommunication];

}


- (void)connectionDidFinishLoading:(NSURLConnection *)connection
{
    
    // do something with the data stored in receivedData
    NSString * response = [[NSString alloc] initWithData:self.receivedData encoding: NSUTF8StringEncoding];
    
    NSLog(@"Received message. Content:<%@> (step:%d)", response, (int) self.progressStatus);
    
    switch ((int) self.progressStatus) {
            
        case LoginReq:
            if ([response isEqualToString: @"session created"]) {
                [self requestSMS];
            }
            break;
            
        case OTPSMSRequest:     // [self sendSMS] continua con il bottone;
            // OK sms richiesto con successo disabilita il tasto per la richiesta
            [self fineStep1];
            break;
            
        case OTPVerifyRequest:
            [self getSeed];
            break;
            
        case SeedRequest:
            // ok got seed now send confirmation
            self.remoteSeed = response;
            NSLog(@"Seed lenght %lu", (unsigned long)[_remoteSeed length]);
            
            [self sendConfirm];
            break;
            
        case SendConfirm:
            //[self closeSession]; non fa nulla aspetta l'OK dall'utente per chiudere la connessione
            break;
            
        case CloseRequest:
            [self fineStep2];
            break;
            
        case WrongOTP:
            // do nothing
            break;
    }
    
    // Release the connection and the data object
    // by setting the properties (declared elsewhere)
    // to nil.  Note that a real-world app usually
    // requires the delegate to manage more than one
    // connection at a time, so these lines would
    // typically be replaced by code to iterate through
    // whatever data structures you are using.
    
    // connection = nil;
    
    // self.receivedData = nil;
}




- (void) resetCommunication {
    // azzera il protocollo di richiesta
        
    self.cookieL1Str = nil;
    self.cookiesL1Dict = nil;
    
    self.receivedData = nil;
    self.connection = nil;
    
}


-(NSDictionary *) parseCocokies: (NSString *) line {
    
    if (line == nil)
        return nil;
    
    NSString *pattern = @"(access-token|csrf-token)\\s*=\\s*([\\p{Hex_Digit}-]+)";
    NSError *error = NULL;
    NSRegularExpression *expression = [NSRegularExpression regularExpressionWithPattern:pattern
                                                                                options:NSRegularExpressionCaseInsensitive
                                                                                  error: &error];
    NSMutableDictionary *result = [NSMutableDictionary dictionary];
    //    NSTextCheckingResult *textCheckingResult = [expression firstMatchInString:line
    //                                                                          options:0
    //                                                                            range:NSMakeRange(0, line.length)];
    //    NSString* key = [line substringWithRange:[textCheckingResult rangeAtIndex:1]];
    //    NSString* value = [line substringWithRange:[textCheckingResult rangeAtIndex:2]];
    //    result[key] = value;
    
    [expression enumerateMatchesInString:line options:0 range:NSMakeRange(0, [line length])
                              usingBlock:^(NSTextCheckingResult *match, NSMatchingFlags flags, BOOL *stop){
                                  // your code to handle matches here
                                  NSString* key = [line substringWithRange:[match rangeAtIndex:1]];
                                  NSString* value = [line substringWithRange:[match rangeAtIndex:2]];
                                  result[key] = value;
                                  
                              }];
    
    return result;
}


- (void) initErrorMessages
{
    _errorMessages = [[NSMutableDictionary alloc] init];
    
    // step 1 (status = LoginReq = 1) - POST /isac-rest/session (autenticazione di primo livello)
    NSString * strCode = [NSString stringWithFormat:@"%d%d", (int)LoginReq, 401];
    [_errorMessages setValue: NSLocalizedString(@"ATUTHFAIL-ERR", nil) forKey: strCode];

    strCode = [NSString stringWithFormat:@"%d%d", (int)LoginReq, 402];
    [_errorMessages setValue: NSLocalizedString(@"PSWDLOCKED-ERR", nil)  forKey:strCode];
    
    strCode = [NSString stringWithFormat:@"%d%d", (int)LoginReq, 410];
    [_errorMessages setValue: NSLocalizedString(@"PSWDEXPIRED-ERR", nil)  forKey:strCode];
    
    // step 2 (status = OTPSMSRequest = 2) - POST /isac-rest/session/sms (richiesta invio OTP SMS)
    
    strCode = [NSString stringWithFormat:@"%d%d", (int)OTPSMSRequest, 400];
    [_errorMessages setValue: NSLocalizedString(@"INVALIDTOKEN-ERR", nil) forKey:strCode];
    
    strCode = [NSString stringWithFormat:@"%d%d", (int)OTPSMSRequest, 401];
    [_errorMessages setValue: NSLocalizedString(@"TOOMANYFAILS-ERR", nil) forKey:strCode];
    
    strCode = [NSString stringWithFormat:@"%d%d", (int)OTPSMSRequest, 404];
    [_errorMessages setValue: NSLocalizedString(@"SESSIONNOTFOUND-ERR", nil) forKey:strCode];
    
    strCode = [NSString stringWithFormat:@"%d%d", (int)OTPSMSRequest, 406];
    [_errorMessages setValue: NSLocalizedString(@"LVL2SESSION-ERR", nil) forKey:strCode];
    
    // step 3 (status = OTPVerifyRequest = 4) - PUT /isac-rest/session/sms (verifica OTP SMS)
    
    strCode = [NSString stringWithFormat:@"%d%d", (int)OTPVerifyRequest, 400];
    [_errorMessages setValue: NSLocalizedString(@"OTPMISMATCH-ERR", nil) forKey:strCode];
    
    strCode = [NSString stringWithFormat:@"%d%d",  (int)OTPVerifyRequest, 401];
    [_errorMessages setValue: NSLocalizedString(@"TOOMANYOTPFAIL-ERR", nil) forKey:strCode];
    
    strCode = [NSString stringWithFormat:@"%d%d", (int)OTPVerifyRequest, 403];
    [_errorMessages setValue: NSLocalizedString(@"INVALIDOTP-ERR", nil) forKey:strCode];
    
    strCode = [NSString stringWithFormat:@"%d%d", (int)OTPVerifyRequest, 404];
    [_errorMessages setValue: NSLocalizedString(@"SESSIONNOTFOUND-ERR", nil) forKey:strCode];
    
    // step 4 (status = SeedRequest = 5) - GET: /identita/app/seed (richiesta seed)
    
    strCode = [NSString stringWithFormat:@"%d%d", (int)SeedRequest, 401];
    [_errorMessages setValue: NSLocalizedString(@"TOOWEAKTOKEN-ERR", nil) forKey:strCode];
    
    strCode = [NSString stringWithFormat:@"%d%d", (int)SeedRequest, 404];
    [_errorMessages setValue: NSLocalizedString(@"SESSIONNOTFOUND-ERR", nil) forKey:strCode];
    
    strCode = [NSString stringWithFormat:@"%d%d", (int)SeedRequest, 412];
    [_errorMessages setValue: NSLocalizedString(@"SEEDGENERATORFAILED-ERR", nil) forKey:strCode];
    
    // step 5 (status = SendConfirm = 6) - POST: /identita/app/seed (verifica OTP)
    
    strCode = [NSString stringWithFormat:@"%d%d", (int)SendConfirm, 400];
    [_errorMessages setValue: NSLocalizedString(@"OTPNOTFAOUND-ERR", nil) forKey:strCode];
    
    strCode = [NSString stringWithFormat:@"%d%d", (int)SendConfirm, 401];
    [_errorMessages setValue: NSLocalizedString(@"OTPNOTFAOUND-ERR", nil) forKey:strCode];
    
    strCode = [NSString stringWithFormat:@"%d%d", (int)SendConfirm, 404];
    [_errorMessages setValue:  NSLocalizedString(@"SESSIONNOTFOUND-ERR", nil) forKey:strCode];
}

@end

