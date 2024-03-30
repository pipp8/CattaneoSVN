//
//  HelpSetupViewController.m
//  MyPhotoSign
//
//  Created by Antonio De Marco on 10/11/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "HelpSetupViewController.h"
#import "PhotoSignAppDelegate.h"

@implementation HelpSetupViewController
@synthesize webServer;
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
	MongooseWrapper *mngServer = [[MongooseWrapper alloc] init];
	self.webServer = mngServer;
	[mngServer release];
}


/*
// Override to allow orientations other than the default portrait orientation.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}
*/

- (IBAction)onBtnVerifySetup: (id)sender {
	PhotoSignAppDelegate *appDelegate = (PhotoSignAppDelegate *) [[UIApplication sharedApplication] delegate];   
	if ([appDelegate isAppActivable]) {
		UIAlertView* alertView = [[UIAlertView alloc] initWithTitle:@"Avviso" message:@"Si può attivare/disattivare il servizio di sincronizzazione con DropBox agendo sullo switch ben visibile nella schermata." delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
		[alertView show];
		[alertView release];
	
		[self dismissModalViewControllerAnimated:TRUE];
		
	} else {
		UIAlertView* alertView = [[UIAlertView alloc] initWithTitle:@"Attenzione" message:@"Setup non completo. Seguire attentamente le istruzioni a video o leggere la documentazione prima di proseguire." delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
		[alertView show];
		[alertView release];
	}
}

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex {

	[webServer stop];
	
}


- (IBAction)onBtnWebServerActive: (id)sender {
	PhotoSignAppDelegate *appDelegate = (PhotoSignAppDelegate *) [[UIApplication sharedApplication] delegate];
	[webServer start:@"8080" documentRoot:[appDelegate certPath] RecURI:YES];
	UIAlertView* alertView = [[UIAlertView alloc] initWithTitle:@"Web server" message:[NSString stringWithFormat:@"%@%@:%@", @"Premere close per chiudere la connessione:\n http://",[webServer getIPAddress],@"8080"] delegate:self cancelButtonTitle:@"Close" otherButtonTitles:nil];
	[alertView setTag:0];
	[alertView show];
	[alertView release];
}

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload {
	self.webServer = nil;
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}


- (void)dealloc {
	[webServer release];
    [super dealloc];
}


@end
