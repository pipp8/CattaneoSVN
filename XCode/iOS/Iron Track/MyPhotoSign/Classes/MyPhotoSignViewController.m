//
//  MyPhotoSignViewController.m
//  MyPhotoSign
//
//  Created by Antonio De Marco on 23/07/10.
//  Copyright __MyCompanyName__ 2010. All rights reserved.
//

#import "MyPhotoSignViewController.h"
#import "OpenSSLRSA.h"
#import "OpenSSLHash.h"
#import "CommonSec.h"
#import "OpenSSLX509.h"
#import "OpenSSLPKCS7.h"
#import "OpenSSLTSP.h"
#import "MyPhotoSignAppDelegate.h"
#import "HelpSetupViewController.h"

@implementation MyPhotoSignViewController

@synthesize overlayView;
@synthesize btnCam;
@synthesize imagePickerCtrl;
@synthesize serverSwitch;
@synthesize passText;
@synthesize activityIndicator;
@synthesize lblProcess;
@synthesize progressView;

- (IBAction)onBtnCamTouch: (id)sender {
	MyPhotoSignAppDelegate *appDelegate = (MyPhotoSignAppDelegate *) [[UIApplication sharedApplication] delegate];
	if ([appDelegate isAppActivable]) {	
		if (![self checkLoadKey])
			[self showAlertPassword];
		else
			[self showCam];
	} else {
		HelpSetupViewController *helperSetupViewController = [[HelpSetupViewController alloc] initWithNibName:@"HelpSetupViewController" bundle:nil];
		[self presentModalViewController:helperSetupViewController animated:YES];
		[helperSetupViewController release];
	}

}


-(void)showAlertPassword {
	
	UIAlertView *myAlertView = [[UIAlertView alloc] initWithTitle:@"Password" message:@"\n\n" delegate:self cancelButtonTitle:@"Cancel" otherButtonTitles:@"OK", nil];
	
	[passText setBackgroundColor:[UIColor whiteColor]];
	[myAlertView addSubview:passText];
    [passText becomeFirstResponder];
	[myAlertView setTag:1];
	[myAlertView show];
	[myAlertView release];
}

-(Boolean) checkLoadKey {

	MyPhotoSignAppDelegate *appDelegate = (MyPhotoSignAppDelegate *) [[UIApplication sharedApplication] delegate];
    Boolean test = NO;
	OpenSSLRSA *rsa = [[OpenSSLRSA alloc] init]; 
	CommonSec *commonSec = [[CommonSec alloc] init];
	[commonSec add_all_algorithms];
	NSString *privKeyPath;
	
	//il path attuale della chiave privata
	// NSBundle *thisBundle = [NSBundle bundleForClass:[self class]];
	// privKeyPath = [thisBundle pathForResource:@"mykey" ofType:@"key"];
	privKeyPath = [NSString stringWithFormat:@"%@/%@", [appDelegate certPath], @"mykey.key"];
	
	//carico la chiave privata nella struttura RSA 
	RSA *privKey = [rsa loadPrivateKey:privKeyPath Key:[passText text]];
	// in caso di password non esistente
	if (privKey == nil)
		privKey = [rsa loadPrivateKey:privKeyPath Key:nil];
	
	
	if (privKey == nil)
		test = NO;
	else {
		test = YES;
	    [rsa freeRSAStruct:privKey];
	}
			
	[rsa release];
	[commonSec cleanup];
	[commonSec release];
	return test;
}

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex {
    MyPhotoSignAppDelegate *appDelegate = (MyPhotoSignAppDelegate *) [[UIApplication sharedApplication] delegate];
	 if (alertView.tag == 0) {
		
		 [serverSwitch setOn:FALSE];
		 if ([appDelegate isAppActivable]) 
			 [btnCam setEnabled:YES];
		 else
	         [btnCam setEnabled:NO];
		 
	 }   
	
	
	if (alertView.tag == 1) {
		if (buttonIndex == 1) {
		
		
			if ([self checkLoadKey]) {
				[self showCam];
				
			} else {
				UIAlertView* alertView = [[UIAlertView alloc] initWithTitle:@"Errore" message:@"Password non valida" delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
				[alertView show];
				[alertView release];
			}
		
		}
		
	}

}

-(void) switchDropBoxService:(id)sender {
		
	if (![[DBSession sharedSession] isLinked]) {
		
		DBLoginController* controller = [[DBLoginController new] autorelease];
        controller.delegate = self;
        [controller presentFromController:self];
    } else {
		
		[[DBSession sharedSession] unlink];
        [[[[UIAlertView alloc] 
           initWithTitle:@"Account Scollegato!" message:@"Il tuo account dropbox è stato scollegato" 
           delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil]
          autorelease]
         show];
	}
	
	
}


#pragma mark DBLoginControllerDelegate methods

- (void)loginControllerDidLogin:(DBLoginController*)controller {
	
	
}

- (void)loginControllerDidCancel:(DBLoginController*)controller {
	[serverSwitch setOn:[[DBSession sharedSession] isLinked]];
}

-(void) showCam {
	
	imagePickerCtrl.sourceType = UIImagePickerControllerSourceTypeCamera;
	[overlayView.locationManager startUpdatingLocation];	
	[overlayView.view setHidden:FALSE];	
	imagePickerCtrl.cameraOverlayView = overlayView.view;
	[self presentModalViewController:imagePickerCtrl animated:YES];
}

- (IBAction)onServerSwitchChange: (id)sender {
	
	
	MyPhotoSignAppDelegate *appDelegate = (MyPhotoSignAppDelegate *) [[UIApplication sharedApplication] delegate];
	

	if ([appDelegate isAppActivable]) {
		[btnCam setEnabled:YES];
					
		[self switchDropBoxService:nil];
	
		
	} else {
		[btnCam setEnabled:NO];
		HelpSetupViewController *helperSetupViewController = [[HelpSetupViewController alloc] initWithNibName:@"HelpSetupViewController" bundle:nil];
		[self presentModalViewController:helperSetupViewController animated:YES];
		[helperSetupViewController release];
				
	}

}

- (NSInteger) b64signSizeTesterWithCert:(X509 *)cert Key:(RSA *)key {
 	//test della firma nell'immagine salvata
	//TODO: Valuare altre strade più performanti
	
	OpenSSLRSA *rsa = [[OpenSSLRSA alloc] init ]; 
	OpenSSLHash *hashSec = [[OpenSSLHash alloc] init ];
	OpenSSLPKCS7 *pkcs7Sec = [[OpenSSLPKCS7 alloc] init ];
	CommonSec *commonSec = [[CommonSec alloc] init ];
	NSData *data = [@"TESTER" dataUsingEncoding: NSASCIIStringEncoding ];
	
/*	NSData *dataHash = [hashSec SHA1:data];

	NSData *signature = [rsa signWithPrivateKey:key dataToSign:dataHash];
 */
    PKCS7 *pkcs7 = [pkcs7Sec sign:data Cert:cert PrivKey:key];
	
	NSData *pkcs7Data = [pkcs7Sec getDataWithPKCS7:pkcs7];
	
	NSString *b64Sign = [commonSec base64EncodedStringWithData:pkcs7Data];
	[pkcs7Sec FreePKCS7Struct:pkcs7];
	
	[rsa release];
	[hashSec release];
	[pkcs7Sec release];
	[commonSec release];
	
	return [b64Sign length];
	
}


- (void) startProcessing: (id)sender {
	[btnCam setEnabled:NO];
    [activityIndicator startAnimating];
	[progressView setHidden:NO];
	lblProcess.text = @"";
	[lblProcess setHidden:NO];
}

- (void) stopProcessing: (id)sender {
	lblProcess.text = @"";
	[btnCam setEnabled:YES];
	[activityIndicator stopAnimating];
	[lblProcess setHidden:YES];
	[progressView setHidden:YES];
}

-(void) changeLabelprogressText: (NSString *)text {

	lblProcess.text = text;

}

-(void) changeProgressBarValue: (NSNumber *) number {

	[progressView setProgress:[number floatValue]];

}

-(void) changeLabelprogressTextOnMainThread: (NSString *)text {
	NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
	[self performSelectorOnMainThread:@selector(changeLabelprogressText:) withObject:text waitUntilDone:NO];
	[pool release];
}

-(void) changeProgressBarValueOnMainThread: (float)value {
	NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
	[self performSelectorOnMainThread:@selector(changeProgressBarValue:) withObject:[NSNumber numberWithFloat:value] waitUntilDone:NO];
	[pool release];
}

-(void)imageProcessing:(NSData *)imageData {
    MyPhotoSignAppDelegate *appDelegate = (MyPhotoSignAppDelegate *) [[UIApplication sharedApplication] delegate];
  
	//I cambiamenti grafici vanno eseguiti sul thread principale, come indicato da Apple...
	//Altrimenti si rischia di non far apparire a video l'effetto voluto.   
	[self performSelectorOnMainThread:@selector(startProcessing:) withObject:nil waitUntilDone:YES];
 

	
	[self changeLabelprogressTextOnMainThread:@"Processo immagine in corso...."];
	EXFJpeg *jpegScanner = [[EXFJpeg alloc] init];

	[jpegScanner scanImageData:imageData];
	EXFMetaData *exifData = [jpegScanner exifMetaData];	
	
    //scrivo latitudine utilizzando le funzioni helper
	
	NSMutableArray *locArrayLat; 
	EXFGPSLoc *gpsLocLat;		
	locArrayLat = [self createLocArray:overlayView.locationPoint.coordinate.latitude];
	
	gpsLocLat = [[EXFGPSLoc alloc] init];
	[self populateGPS: gpsLocLat :locArrayLat];
	[exifData addTagValue:gpsLocLat forKey:[NSNumber numberWithInt:EXIF_GPSLatitude] ];
	
	[gpsLocLat release];
	[locArrayLat release];		
	
	//scrivo ref latitude
	NSString *refLat;
	if (overlayView.locationPoint.coordinate.latitude <0.0){
		refLat = @"S";
	}else{
		refLat =@"N";
	}
	[exifData addTagValue: refLat forKey:[NSNumber numberWithInt:EXIF_GPSLatitudeRef] ];	
	
	
	//scrivo longitudine
	NSMutableArray *locArrayLong; 
	EXFGPSLoc *gpsLocLong;	
	locArrayLong = [self createLocArray:overlayView.locationPoint.coordinate.longitude];
	gpsLocLong = [[EXFGPSLoc alloc] init];
	[self populateGPS: gpsLocLong :locArrayLong];
	[exifData addTagValue:gpsLocLong forKey:[NSNumber numberWithInt:EXIF_GPSLongitude] ];
	[gpsLocLong release];
	[locArrayLong release];		
	
	//scrivo ref longitudine
	NSString *refLong;
	if (overlayView.locationPoint.coordinate.longitude <0.0){
		refLong = @"W";
	}else{
		refLong =@"E";
	}
	[exifData addTagValue: refLong forKey:[NSNumber numberWithInt:EXIF_GPSLongitudeRef] ];	
	
	//scrivo altitudine
	
	long numDenumArray[2]; 
	long* arrPtr = numDenumArray; 
	[EXFUtils convertRationalToFraction:&arrPtr: [NSNumber numberWithDouble:overlayView.locationPoint.altitude]]; 
	EXFraction *fract = [[EXFraction alloc] initWith:numDenumArray[0] :numDenumArray[1]]; 
	[exifData addTagValue:fract  forKey:[NSNumber numberWithInt:EXIF_GPSAltitude] ];
	[fract release];
	
    [self changeLabelprogressTextOnMainThread:@"Inserimento informazioni standard ..."];

	//Scrivo qualche dato aggiuntivo d'esempio
	[exifData addTagValue: @"Apple" forKey:[NSNumber numberWithInt:EXIF_Make]];	
	[exifData addTagValue: @"DIA Dipartimento di Informatica" forKey:[NSNumber numberWithInt:EXIF_Copyright]];		
	[exifData addTagValue: @"Iron Track 1.1" forKey:[NSNumber numberWithInt:EXIF_Software]];	
	
	
	NSMutableArray* array = [[NSMutableArray alloc] init];
	[array addObject:[NSNumber numberWithChar:2] ];
	[array addObject:[NSNumber numberWithChar:2] ];
	[array addObject:[NSNumber numberWithChar:0] ];
	[array addObject:[NSNumber numberWithChar:0] ];
	[exifData addTagValue: array forKey:[NSNumber numberWithInt:EXIF_GPSVersion] ];
	[array release];


	[self changeProgressBarValueOnMainThread:0.2];
	[self changeLabelprogressTextOnMainThread:@"Preparazione immagine in corso ..."];
	//alloco i layer 
	OpenSSLRSA *rsa = [[OpenSSLRSA alloc] init]; 
	OpenSSLHash *hashSec = [[OpenSSLHash alloc] init];
	CommonSec *commonSec = [[CommonSec alloc] init];
	OpenSSLX509 *x509Sec = [[OpenSSLX509 alloc] init];
	OpenSSLPKCS7 *pkcs7Sec = [[OpenSSLPKCS7 alloc] init];
	OpenSSLTSP *tspSec = [[OpenSSLTSP alloc] init];
	NSString *privKeyPath;
	NSString *certPath;
	//inizializza OpenSSL
	[commonSec add_all_algorithms];
	
	//il path attuale della chiave privata
	
	// NSBundle *thisBundle = [NSBundle bundleForClass:[self class]];
	// privKeyPath = [thisBundle pathForResource:@"mykey" ofType:@"key"];
	
	// certPath = [thisBundle pathForResource:@"mycert" ofType:@"crt"];
	
	privKeyPath = [NSString stringWithFormat:@"%@/%@",[appDelegate certPath], @"mykey.key"];
	
	certPath = [NSString stringWithFormat:@"%@/%@", [appDelegate certPath], @"mycert.crt"];

	//carico la chiave privata nella struttura RSA 
	RSA *privKey = [rsa loadPrivateKey:privKeyPath Key:[passText text]];
	//In caso di non esistenza della password
	if (privKey == nil)
		privKey = [rsa loadPrivateKey:privKeyPath Key:nil];
	
	// resetto la password che viene richiesta  ad ogni firma
	passText.text = @"";
	
	//carico il certificato
	X509 *cert = [x509Sec loadCertificateWithPath:certPath];
	[self changeProgressBarValueOnMainThread:0.3];
	
	//verifico che il caricamento sia andato a buon fine
	if ((privKey == nil) || (cert == nil)) {
		
		UIAlertView* alertView = [[UIAlertView alloc] initWithTitle:@"Errore" message:@"Impossibile caricare le strutture per la firma" delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
		[alertView show];
		[alertView release];
		
	} else {
		
		[exifData addTagValue:[x509Sec getCertSubject:cert Parameter:@"CN"] forKey:[NSNumber numberWithInt:EXIF_Artist]];	
		
		//Replica delle X nei tag
		int len = [self b64signSizeTesterWithCert:cert Key:privKey];
		
	    NSString *tsaTimeFormat = @"yyyy:MM:dd HH:mm:ss";
		[exifData addTagValue:[commonSec replicateStr:@"X" number:[tsaTimeFormat length]] forKey:[NSNumber numberWithInt:EXIF_DateTime]];	
		[exifData addTagValue:[commonSec replicateStr:@"X" number:[tsaTimeFormat length]] forKey:[NSNumber numberWithInt:EXIF_DateTimeDigitized]];	
		[exifData addTagValue:[commonSec replicateStr:@"X" number:[tsaTimeFormat length]] forKey:[NSNumber numberWithInt:EXIF_DateTimeOriginal]];	
		[exifData addTagValue:[commonSec replicateStr:@"X" number:len] forKey:[NSNumber numberWithInt:CUSTOM_EXIF_SIGN]];	
		[exifData addTagValue:[commonSec replicateStr:@"X" number:TSP_SIZE] forKey:[NSNumber numberWithInt:CUSTOM_EXIF_TIMESTAMP]];	
		
	
		//inserisco i tag nella struttura da firmare 
		NSMutableData* taggedDataForTSA = [[NSMutableData alloc] init];	
		[jpegScanner populateImageData:taggedDataForTSA];
		[self changeProgressBarValueOnMainThread:0.4];
		//calcolo l'hash per la TSA 
		
		NSData *dataForTSA = [hashSec SHA1:taggedDataForTSA];
		
		[taggedDataForTSA release];
		
		//Applico protocollo TSP
		[self changeLabelprogressTextOnMainThread:@"Richiesta time-stamp in corso ..."];
		
		//creo la richiesta da inviare alla TSA tramite http POST
		NSData *dataRequest = [tspSec CreateRequestWithHash:dataForTSA HashName:@"SHA1" Policy:@"" nonce:0 CertRequired:YES];
		NSString *postLength = [NSString stringWithFormat:@"%d", [dataRequest length]];
		NSMutableURLRequest *request = [[NSMutableURLRequest alloc] init ];
		[request setURL:[NSURL URLWithString:@"http://timestamping.edelweb.fr/service/tsp"]];
		[request setHTTPMethod:@"POST"];
		[request setValue:postLength forHTTPHeaderField:@"Content-Length"];
		[request setValue:@"no-cache" forHTTPHeaderField:@"Pragma"];
		[request setValue:@"application/timestamp-query" forHTTPHeaderField:@"Content-Type"];
		[request setValue:@"application/timestamp-reply" forHTTPHeaderField:@"Accept"];
		[request setHTTPBody:dataRequest];
		
		//Invio la richiesta
		NSError *error;
		NSURLResponse *response;
		NSData *dataResponse=[NSURLConnection sendSynchronousRequest:request returningResponse:&response error:&error];
		
		//esamino la risposta dalla TSA verificandone lo stato
		//le possibili risposte tspGranted tspGrantedWithModification tspRejected tspWaiting tspRevocationWarning tspRevoked 
		//tspWrongStatusInfo tspResponseNotParsable
		TSPResponseStatus resp = [tspSec verifyStatusWithResponse:dataResponse];
		[self changeProgressBarValueOnMainThread:0.6];
		
		
		if ((resp == tspGranted) || (resp == tspGrantedWithModification)) {
			
			[self changeLabelprogressTextOnMainThread:@"Inserimento time-stamp ..."];
			
			//Prendo il tempo inviatomi dalla TSA nella response
			NSDate *dateTSA =[tspSec getDateWithResponse:dataResponse];
			
			//Utilizzo NSDateFormatter per ottenere la rappresentazione della data in formato compatibile Exif 
			//in caso di integrazione con i capmi esistenti
			NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
			
			[dateFormatter setDateFormat:tsaTimeFormat];
			
			NSString *dateStr = [dateFormatter stringFromDate:dateTSA];
			
			[dateFormatter setDateFormat:@"yyyyMMddHHmmss"];
			NSString *dateStrPath = [dateFormatter stringFromDate:dateTSA];
			[dateFormatter release];
			
			[exifData addTagValue:dateStr forKey:[NSNumber numberWithInt:EXIF_DateTime]];	
			[exifData addTagValue:dateStr forKey:[NSNumber numberWithInt:EXIF_DateTimeDigitized]];	
			[exifData addTagValue:dateStr forKey:[NSNumber numberWithInt:EXIF_DateTimeOriginal]];	
			[exifData addTagValue: [commonSec base64EncodedStringWithData:dataResponse] forKey:[NSNumber numberWithInt:CUSTOM_EXIF_TIMESTAMP]];
			
			NSMutableData* taggedDataToSign = [[NSMutableData alloc] init];	
			[jpegScanner populateImageData:taggedDataToSign];
						
			[self changeProgressBarValueOnMainThread:0.8];
			//calcolo l'hash per la firma non piu' necessario
			// NSData *hashData = [hashSec SHA1:taggedDataToSign];
			
			//ora posso firmare l'hash
			[self changeLabelprogressTextOnMainThread:@"Calcolo la firma ..."];
			// NSData *signature = [rsa signWithPrivateKey:privKey dataToSign:hashData];
			[self changeProgressBarValueOnMainThread:0.9];
			// if (signature == nil) {
			//	UIAlertView* alertView = [[UIAlertView alloc] initWithTitle:@"Errore" message:@"Impossibile applicare la firma" delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
			//	[alertView show];
			//	[alertView release];
			//} else {
					
			//applico la firma sul PKCS7			
			// PKCS7 *pkcs7 = [pkcs7Sec sign:signature Cert:cert PrivKey:privKey]; // firmo la firma ???
			PKCS7 *pkcs7 = [pkcs7Sec sign:taggedDataToSign Cert:cert PrivKey:privKey];
		
			if (pkcs7 == nil) {
				UIAlertView* alertView = [[UIAlertView alloc] initWithTitle:@"Errore" message:@"Impossibile impostare il formato PKCS7" delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
				[alertView show];
				[alertView release];
			
			} else {
			
				NSData *pkcs7Data = [pkcs7Sec getDataWithPKCS7:pkcs7];
			
				if  (pkcs7Data == nil) {
					UIAlertView* alertView = [[UIAlertView alloc] initWithTitle:@"Errore" message:@"Impossibile impostare il formato PKCS7" delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
					[alertView show];
					[alertView release];
				
				} else {
					//ora inseriamo nell'immagine jpg la vera firma sostituendola alla precedente (PKCS7)
					NSString *tagValue = [commonSec base64EncodedStringWithData:pkcs7Data];
					
					[exifData addTagValue:tagValue forKey:[NSNumber numberWithInt:CUSTOM_EXIF_SIGN]];
									
					//Path dell'immagine
					NSString *imgPath = [NSString stringWithFormat:@"%@/%@%@%@", [appDelegate imagePath], @"IMG", dateStrPath,@".jpg"];
				
					[self changeLabelprogressTextOnMainThread:@"Salvataggio immagine in corso ..."];
					[self changeProgressBarValueOnMainThread:1.0];
					//salvo l'immagine firmata e rilascio qualche struttura
					NSMutableData* taggedDataForStoring = [[NSMutableData alloc] init];	
					
					[jpegScanner populateImageData:taggedDataForStoring];
					[taggedDataForStoring writeToFile:imgPath atomically:YES];
					[taggedDataForStoring release];
					[pkcs7Sec FreePKCS7Struct:pkcs7];
				
					[self changeLabelprogressTextOnMainThread:@"Processo completo."];
				}
			// }
			}
			
		} else {
			UIAlertView* alertView = [[UIAlertView alloc] initWithTitle:@"Errore" message:@"Impossibile procedere con il time-stamp" delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
			[alertView show];
			[alertView release];
		}
	}

    [self performSelectorOnMainThread:@selector(stopProcessing:) withObject:nil waitUntilDone:YES];
   	
    [rsa freeRSAStruct:privKey];
	[x509Sec FreeX509Struct:cert];
	[rsa release];
	[hashSec release];
	[pkcs7Sec release];
	[x509Sec release];
	[tspSec release];
	[jpegScanner release];
	[commonSec cleanup];
	[commonSec release];
	
	[overlayView.locationManager stopUpdatingLocation];	
}

-(void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary *)info {
 	
	NSData *imageData = UIImageJPEGRepresentation ([info objectForKey:UIImagePickerControllerOriginalImage], 1.0 );

	[imagePickerCtrl dismissModalViewControllerAnimated:TRUE];
	
	[NSThread detachNewThreadSelector:@selector(imageProcessing:) toTarget:self withObject:imageData];  
} 

- (void)imagePickerControllerDidCancel:(UIImagePickerController *)pickerController {
	
	[overlayView.locationManager stopUpdatingLocation];	
	//Resetto la password che verrà richiesta nuovamente la password
	passText.text = @"";
	[pickerController dismissModalViewControllerAnimated:TRUE];	
	
}
/*
// The designated initializer. Override to perform setup that is required before the view is loaded.
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    if ((self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil])) {
        // Custom initialization
    }
    return self;
}
*/

/*
// Implement loadView to create a view hierarchy programmatically, without using a nib.
- (void)loadView {
}
*/

/*

-(BOOL)textFieldShouldReturn:(UITextField *)aTextField {
	
	
	[passText resignFirstResponder];
	return YES;
}
*/

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.





- (void)viewDidLoad {
    [super viewDidLoad];
	
	self.passText = [[UITextField alloc] initWithFrame:CGRectMake(20, 35, 240, 30)];
	
	self.passText.borderStyle = UITextBorderStyleBezel;
	
	//self.passText.textColor = [UIColor whiteColor];
	
	self.passText.font = [UIFont systemFontOfSize:12.0];
	
	self.passText.secureTextEntry = TRUE;
	
	self.passText.placeholder = @"Password";
	[passText setDelegate:self];
	
	passText.text=@"";
	
	[serverSwitch setOn:[[DBSession sharedSession] isLinked]];
	

	[btnCam setEnabled:YES];
	[serverSwitch setEnabled:YES];
	[progressView setHidden:TRUE];
	[progressView setProgress:0];


}


// Override to allow orientations other than the default portrait orientation.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    return (interfaceOrientation == UIInterfaceOrientationLandscapeRight);
}


- (void)viewDidAppear:(BOOL)animated {
	MyPhotoSignAppDelegate *appDelegate = (MyPhotoSignAppDelegate *) [[UIApplication sharedApplication] delegate];   
	if (![appDelegate isAppActivable]) {
		HelpSetupViewController *helperSetupViewController = [[HelpSetupViewController alloc] initWithNibName:@"HelpSetupViewController" bundle:nil];
		[self presentModalViewController:helperSetupViewController animated:YES];
		[helperSetupViewController release];
	}
}

- (void)didReceiveMemoryWarning {
	// Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
	
	// Release any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload {
	// Release any retained subviews of the main view.
	// e.g. self.myOutlet = nil;
	self.overlayView = nil;
	self.btnCam = nil;
	self.imagePickerCtrl = nil;
    self.serverSwitch = nil;
	self.passText = nil;
	self.activityIndicator = nil;
	self.lblProcess = nil;
	self.progressView = nil;
	[super viewDidUnload];
}

// Helper methods for location conversion
-(NSMutableArray*) createLocArray:(double) val{
	val = fabs(val);
	NSMutableArray* array = [[NSMutableArray alloc] init];
	double deg = (int)val;
	[array addObject:[NSNumber numberWithDouble:deg]];
	val = val - deg;
	val = val*60;
	double minutes = (int) val;
	[array addObject:[NSNumber numberWithDouble:minutes]];
	val = val - minutes;
	val = val *60;
	double seconds = val;
	[array addObject:[NSNumber numberWithDouble:seconds]];
	return array;
}
-(void) populateGPS: (EXFGPSLoc*)gpsLoc :(NSArray*) locArray{
	long numDenumArray[2];
	long* arrPtr = numDenumArray;
	[EXFUtils convertRationalToFraction:&arrPtr :[locArray objectAtIndex:0]];
	EXFraction* fract = [[EXFraction alloc] initWith:numDenumArray[0] :numDenumArray[1]];
	gpsLoc.degrees = fract;
	[fract release];
	[EXFUtils convertRationalToFraction:&arrPtr :[locArray objectAtIndex:1]];
	fract = [[EXFraction alloc] initWith:numDenumArray[0] :numDenumArray[1]];
	gpsLoc.minutes = fract;
	[fract release];
	[EXFUtils convertRationalToFraction:&arrPtr :[locArray objectAtIndex:2]];
	fract = [[EXFraction alloc] initWith:numDenumArray[0] :numDenumArray[1]];
	gpsLoc.seconds = fract;
	[fract release];
}
// end of helper methods


- (void)dealloc {
	[overlayView release];
	[activityIndicator release];
	[btnCam release];
	[imagePickerCtrl release];
	[serverSwitch release];
	[passText release];
	[lblProcess release];
	[progressView release];
	[super dealloc];
}

@end
