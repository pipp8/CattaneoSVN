//
//  ImageViewController.m
//  MyPhotoVerify
//
//  Created by Antonio De Marco on 01/08/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "PDFViewController.h"
#import "PDFSignVerifyAppDelegate.h"


@implementation PDFViewController
@synthesize image;
@synthesize imageFile;
@synthesize pdfFile;
@synthesize directoryContent;


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
	PDFSignVerifyAppDelegate *appDelegate = (PDFSignVerifyAppDelegate *) [[UIApplication sharedApplication] delegate];
	
	self.title = [pdfFile stringByDeletingPathExtension];
	
	NSString *pdfAbsolutePath = [NSString stringWithFormat:@"%@/%@", appDelegate.docPath,pdfFile];
	pdfURL = CFURLCreateWithFileSystemPath (NULL, pdfAbsolutePath, kCFURLPOSIXPathStyle, 0);
	[webView loadRequest:[NSURLRequest requestWithURL:(NSURL *)pdfURL]];
	
	
	
	pdf2 = [[PdfSigUtils alloc] init];
	
	[pdf2 Initialize];
	
	if (![pdf2 PDFFromURL:pdfURL WithAbsolutePath:pdfAbsolutePath]) {
		NSLog(@"viewDidLoad: Errore nel caricamento del PDF");
	}
	
	fileManager = [[NSFileManager alloc] init ];
	
	NSMutableArray *array = [[NSMutableArray alloc] initWithArray:[[fileManager contentsOfDirectoryAtPath:[appDelegate certPath] error:nil] filteredArrayUsingPredicate:[NSPredicate predicateWithFormat:@"self ENDSWITH '.crt'"] ]]; 
	
	self.directoryContent =   array  ;
	[array release];
	[fileManager release];
		
	NSString *str;
	for (int i=0; i<[directoryContent count]; i++) {
		str = [NSString stringWithFormat:@"%@/%@",[appDelegate certPath],[directoryContent objectAtIndex:i]];
		[pdf2 AddRootCertificatesFromCA:str];
	}
	
	
	isView = NO;
}


/*
// Override to allow orientations other than the default portrait orientation.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations------------------------------------------------
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}
*/






-(IBAction)VerificaConLib:(id) sender
{

	// 1] Call PDFFromBundle to load the PDF
	// 2] Check if it is signed and how many signaure are there (IsDigitalSigned, SignatureCount)
	// 3] Eventually add root certificates with AddRootCertificatesFromBundle
	// 4] Loop on verifing with VerifySignature	
	
	
	UIAlertView* alertView;
	
	if (pdf2 != NULL) {
		
		if (pdf2.IsDigitalSigned && pdf2.SignatureCount > 0) {
				for (int i=0; i<pdf2.SignatureCount; i++) {
					
					if ([pdf2 VerifySignature:i])
						alertView = [[UIAlertView alloc] initWithTitle:@"YEAAAAAAA!!!" message:[pdf2 GetSigner:i] delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil]; 
					else	
						alertView = [[UIAlertView alloc] initWithTitle:@"NOOOOOOOO!!!" message:[pdf2 GetSigner:i] delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
					
					[alertView show];
					[alertView release]; 
				}
		} else {
			alertView = [[UIAlertView alloc] initWithTitle:@"Info" message:@"File non firmato!" delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil]; 
			[alertView show];
			[alertView release]; 
		}

	}
	
		
		

}


-(IBAction)FirmaCADESConLib:(id) sender
{
	NSLog(@"ci sono a firmare");
	
	
	if (pdf2 != NULL) {
		
		
		
	}
	
	
}




- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload {
    [super viewDidUnload];
	self.image = nil;
	self.imageFile = nil;
	self.pdf2 = nil;
	
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}


- (void)dealloc {
	[image release];
	[imageFile release];
	[pdf2 release];
    [super dealloc];
}


@end
