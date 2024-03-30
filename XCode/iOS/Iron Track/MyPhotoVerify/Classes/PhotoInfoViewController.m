//
//  PhotoInfoViewController.m
//  MyPhotoVerify
//
//  Created by Antonio De Marco on 05/08/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "PhotoInfoViewController.h"
#import "OpenSSLRSA.h"
#import "OpenSSLHash.h"
#import "OpenSSLX509.h"
#import "OpenSSLPKCS7.h"
#import "OpenSSLTSP.h"
#import "CommonSec.h"
#import "EXFUtils.h"
#import "EXF.h"
#import "MapViewController.h"
#import "MyPhotoVerifyAppDelegate.h"

@implementation PhotoInfoViewController
@synthesize imageFile;
@synthesize lblTimestamping;
@synthesize lblTSASigner;
@synthesize lblPhotoSigner;
@synthesize lblSoftware;
@synthesize lblArtist;
@synthesize lblCpopyright;
@synthesize lblData;
@synthesize lblAltezza;
@synthesize lblLarghezza;
@synthesize lblProduttore;
@synthesize lblGPSCoord;
@synthesize imageWarning;
@synthesize latitude;
@synthesize longitude;
@synthesize navCon;

/*
 // The designated initializer.  Override if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    if ((self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil])) {
        // Custom initialization
    }
    return self;
}
*/

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];

	[imageWarning setHidden:YES];
	self.title = @"Info";

    [self verifyPhoto];
}


- (IBAction)showMap:(id) sender {
	MyPhotoVerifyAppDelegate *appDelegate = (MyPhotoVerifyAppDelegate *) [[UIApplication sharedApplication] delegate];
	MapViewController *mapViewController= [[MapViewController alloc] initWithNibName:@"MapViewController" bundle:nil];
	[mapViewController setLatitude:latitude];
	[mapViewController setLongitude:longitude];

	[mapViewController setPathThumbImage:[NSString stringWithFormat:@"%@/%@.png", [appDelegate imagePath],[imageFile stringByDeletingPathExtension] ]];
	//[self.navigationController pushViewController:mapViewController animated:YES];
	[self.navCon presentModalViewController:mapViewController animated:YES];
	//[self presentModalViewController:mapViewController animated:YES];	
	[mapViewController release];
	
}

-(void) verifyPhoto {
	MyPhotoVerifyAppDelegate *appDelegate = (MyPhotoVerifyAppDelegate *) [[UIApplication sharedApplication] delegate];
	//init
	OpenSSLRSA *rsa = [[OpenSSLRSA alloc] init ]; 
	OpenSSLHash *hashSec = [[OpenSSLHash alloc] init ];
	OpenSSLX509 *x509Sec = [[OpenSSLX509 alloc] init ];
	OpenSSLPKCS7 *pkcs7Sec = [[OpenSSLPKCS7 alloc] init ];
	OpenSSLTSP *tspSec = [[OpenSSLTSP alloc] init ];
	CommonSec *commonSec = [[CommonSec alloc] init ];
	
	[commonSec add_all_algorithms];
	
	//carico la chiave pubblica
		
	//carico l'immagine e la relitiva struttura
	EXFJpeg *jpegScanner = [[EXFJpeg alloc] init];
	
	
	NSData *imageData = [[NSData alloc] initWithContentsOfFile:[NSString stringWithFormat:@"%@/%@", [appDelegate imagePath],imageFile ]];
	
	
	[jpegScanner scanImageData:imageData];
	EXFMetaData *exifData = [jpegScanner exifMetaData];
	
	//recupero la firma in base64
	NSString *b64Sign;
	NSString *b64TSP;
	
    b64Sign = [NSString stringWithFormat:@"%@",[exifData tagValue:[NSNumber numberWithInt:CUSTOM_EXIF_SIGN]]];
    
	b64TSP = [NSString stringWithFormat:@"%@",[exifData tagValue:[NSNumber numberWithInt:CUSTOM_EXIF_TIMESTAMP]]];
		
	//trasformo la firma in dati (PKCS7)
	NSData* dataPKCS7 = [commonSec dataByBase64DecodingString:b64Sign];
	
	//trasformo in dati il timestamp reply
	NSData* dataTSP = [commonSec dataByBase64DecodingString:b64TSP];
	
	TSPResponseStatus respTSP = [tspSec verifyStatusWithResponse:dataTSP];
	
	//Valori Exif standard
	lblSoftware.text = [exifData tagValue:[NSNumber numberWithInt:EXIF_Software]];
	lblArtist.text    = [exifData tagValue:[NSNumber numberWithInt:EXIF_Artist]];
	lblCpopyright.text    = [exifData tagValue:[NSNumber numberWithInt:EXIF_Copyright]];
	lblProduttore.text    = [exifData tagValue:[NSNumber numberWithInt:EXIF_Make]];
	
	//GPS
	EXFGPSLoc *gpsLocLat = [exifData tagValue:[NSNumber numberWithInt:EXIF_GPSLatitude]];
	NSString *latRef = [exifData tagValue:[NSNumber numberWithInt:EXIF_GPSLatitudeRef]];
	EXFGPSLoc *gpsLocLong = [exifData tagValue:[NSNumber numberWithInt:EXIF_GPSLongitude]];	
	NSString *longRef = [exifData tagValue:[NSNumber numberWithInt:EXIF_GPSLongitudeRef]];
    double latTMP = [gpsLocLat descriptionAsDecimal];
	latTMP = fabs(latTMP);
	if ([latRef isEqualToString:@"S"]) {
		latitude = latTMP*(-1);
	} else {
		latitude = latTMP;
	}

	
	double longTMP = [gpsLocLong descriptionAsDecimal];
	longTMP = fabs(longTMP);
	if ([longRef isEqualToString:@"W"]) {
		longitude = longTMP*(-1);
	}  else {
		longitude = longTMP;
	}
	
	lblGPSCoord.text = [NSString stringWithFormat:@"%@ %@ %@ %@",latRef,[NSString stringWithFormat:@"%@\xC2\xB0 %@' %@\"",gpsLocLat.degrees, gpsLocLat.minutes,gpsLocLat.seconds],longRef,[NSString stringWithFormat:@"%@\xC2\xB0 %@' %@\"",gpsLocLong.degrees, gpsLocLong.minutes,gpsLocLong.seconds]];
	
	if (((respTSP == tspGranted) || (respTSP == tspGrantedWithModification)) && ([b64Sign length] > 0)) {
		//Prendo la struttura PKCS7 
		PKCS7 *pkcs7 = [pkcs7Sec getPKCS7WithData:dataPKCS7];
		
		if (pkcs7 != nil) {
			
			//Prendo il messaggio dal PKCS7
			// NSData *extractedHash = [pkcs7Sec getMessageWithPKCS7:pkcs7];
			
			NSMutableData* taggedDataForSign = [[NSMutableData alloc] init];	
					
			[exifData addTagValue:[commonSec replicateStr:@"X" number:[b64Sign length]] forKey:[NSNumber numberWithInt:CUSTOM_EXIF_SIGN]];	
			
			[jpegScanner populateImageData:taggedDataForSign];
			
			// legge il certificato allegato alla firma
			X509 * tmpCert = [pkcs7Sec getAllSigners:pkcs7];
			NSString * signerSN = [x509Sec getCertSerialNumber:tmpCert];
			
			
			// verifica della firma
			X509 *cert = nil;
			NSInteger certNum = -1;
			NSFileManager *fileManager = [[NSFileManager alloc] init];
			NSArray *directoryContent = [[NSArray alloc] initWithArray: [[fileManager contentsOfDirectoryAtPath:[appDelegate certPath] error:nil] filteredArrayUsingPredicate:[NSPredicate predicateWithFormat:@"self ENDSWITH '.crt'"] ]];
			
			// cerco il certificato con lo stesso serial number tra tutti quelli presenti nella directory
			for (NSInteger i = 0; i < [directoryContent count]; i++) {
				// carico il certificato
				cert = [x509Sec loadCertificateWithPath:[NSString stringWithFormat:@"%@/%@",
															   [appDelegate certPath], [directoryContent objectAtIndex:i]]];
				if ([signerSN isEqualToString:[x509Sec getCertSerialNumber: cert]] == YES) {
					// OK trovato un cetificato con lo stesso serial number
					certNum = i;					
					break;
				}
				if (cert != nil)				
					[x509Sec FreeX509Struct:cert];		
			}
						
			Boolean verify = NO;
			if (certNum >= 0 && cert != nil) {
				//Verifico il pkcs7 con il certificato
				verify = [pkcs7Sec verify:pkcs7 Cert:cert DetachedData:taggedDataForSign];

				if (!verify) {
					// il certificato c'e' ma la verifica e' fallita
					[imageWarning setHidden:NO];
					[[[[UIAlertView alloc] 
					   initWithTitle:@"Attenzione" message:@"La verifica della firma dell'immagine è fallita. Il certificato era presente nello store dei certificati."
					   delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil]
					  autorelease]
					 show];
				}
				else {
					// firma OK
					lblPhotoSigner.text =  [x509Sec getCertSubject:cert Parameter:@"CN"];
				}
				// in entrambi i casi prova a rilasciare il certificato
				if (cert != nil)				
					[x509Sec FreeX509Struct:cert];				
			}
			else {
				// il certificato non e' stato trovato
				[imageWarning setHidden:NO];
				[[[[UIAlertView alloc] 
				   initWithTitle:@"Attenzione" message:@"Non è stato possibile reperire il certicato usato per la firma dell'immagine. Verificare l'installazione del certificato dell'autore nello store dei certificati."
				   delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil]
				  autorelease]
				 show];
			}
				
			[pkcs7Sec FreePKCS7Struct:pkcs7];
			[taggedDataForSign release];
						
			
			// verifica Marcatura Temporale
			
			[exifData addTagValue:[commonSec replicateStr:@"X" number:TSP_SIZE] forKey:[NSNumber numberWithInt:CUSTOM_EXIF_TIMESTAMP]];	
				
			NSString *tsaTimeFormat = @"yyyy:MM:dd hh:mm:ss";
			[exifData addTagValue:[commonSec replicateStr:@"X" number:[tsaTimeFormat length]] forKey:[NSNumber numberWithInt:EXIF_DateTime]];	
			[exifData addTagValue:[commonSec replicateStr:@"X" number:[tsaTimeFormat length]] forKey:[NSNumber numberWithInt:EXIF_DateTimeDigitized]];	
			[exifData addTagValue:[commonSec replicateStr:@"X" number:[tsaTimeFormat length]] forKey:[NSNumber numberWithInt:EXIF_DateTimeOriginal]];
			NSMutableData* taggedDataForTSP = [[NSMutableData alloc] init];	
			
			[jpegScanner populateImageData:taggedDataForTSP];
			
			//calcolo l'hash per il TSP
			
			NSData *dataToVerifyForTSP = [hashSec SHA1:taggedDataForTSP];
			[taggedDataForTSP release];
			
			//Verifico La risposta dalla TSA
			
			NSData *dataRequest = [tspSec CreateRequestWithHash:dataToVerifyForTSP HashName:@"SHA1" Policy:@"" nonce:0 CertRequired:YES];
			certNum = -1;
			
			for (NSInteger i=0; i< [directoryContent count]; i++) {
				
				cert = [x509Sec loadCertificateWithPath:[NSString stringWithFormat:@"%@/%@", [appDelegate certPath], [directoryContent objectAtIndex:i]]];
				
				verify = [tspSec verifySignatureWithResponse:dataTSP Request:dataRequest Cert:nil CACert:cert];
				
				if (verify) {
					certNum = i;
					break;
				}
				
				if (cert != nil)				
					[x509Sec FreeX509Struct:cert];
				
			}
			
			if (certNum >= 0) {
				//Prendo il tempo inviatomi dalla TSA nella response
				NSDate *dateTSA =[tspSec getDateWithResponse:dataTSP];
				
				//Utilizzo NSDateFormatter per ottenere la rappresentazione della data in formato compatibile Exif 
				//in caso di integrazione con i campi esistenti
				NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
				
				[dateFormatter setDateFormat:@"dd/MM/yyyy HH:mm:ss"];
				
				NSString *dateStr = [dateFormatter stringFromDate:dateTSA];
				[dateFormatter release];	
				lblTimestamping.text = dateStr;
				
				lblTSASigner.text = [x509Sec getCertSubject:cert Parameter:@"CN"];
				
				if (cert != nil)				
					[x509Sec FreeX509Struct:cert];
			} 
			else {
				[imageWarning setHidden:NO];
				[[[[UIAlertView alloc] 
				   initWithTitle:@"Attenzione" message:@"La verifica della firma della TSA è fallita. Assicurarsi di avere installato il certificato della TSA nello store dei certificati."
				   delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil]
				  autorelease]
				 show];
			}
			
			certNum = -1;
			
			[directoryContent release];
			[fileManager release];
		}
		
	} else {
		[imageWarning setHidden:NO];
		[[[[UIAlertView alloc] 
		   initWithTitle:@"Attenzione" message:@"La Verifica del timestamping fallita.\nControllare che i tag Exif sign e timestamp siano correttamente riempiti"
		   delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil]
		  autorelease]
		 show];
	}
	
	[jpegScanner release];
	[imageData release];
	[rsa release];
	[x509Sec release];
	[hashSec release];
	[tspSec release];
	[pkcs7Sec release];
	[commonSec cleanup];
	[commonSec release];
}


/*
// Override to allow orientations other than the default portrait orientation.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}
*/

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event {
	UITouch *touch = [touches anyObject];
	if ([touch view] == [self view]) 
	  [self.view setHidden:TRUE];	
}


- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload {
    [super viewDidUnload];
	self.imageFile = nil;
	self.lblTimestamping = nil;
	self.lblTSASigner = nil;
	self.lblPhotoSigner = nil;
	self.lblSoftware = nil;
	self.lblArtist = nil;
	self.lblCpopyright=nil;
	self.lblData=nil;
	self.lblAltezza=nil;
	self.lblLarghezza=nil;
	self.lblProduttore=nil;
	self.lblGPSCoord=nil;
	self.imageWarning = nil;
	self.navCon = nil;
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}


//Helpers method 



- (void)dealloc {
	[imageFile release];	
	[lblTimestamping release];
	[lblTSASigner release];
	[lblPhotoSigner release];
	[lblSoftware release];
	[lblArtist release];
	[lblCpopyright release];
	[lblData release];
	[lblAltezza release];
	[lblLarghezza release];
	[lblProduttore release];
	[lblGPSCoord release];
	[imageWarning release];
	[navCon release];
    [super dealloc];
}


@end
