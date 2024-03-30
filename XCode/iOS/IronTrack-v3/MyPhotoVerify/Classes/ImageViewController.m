//
//  ImageViewController.m
//  MyPhotoVerify
//
//  Created by Antonio De Marco on 01/08/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "ImageViewController.h"
#import "MyPhotoVerifyAppDelegate.h"


@implementation ImageViewController
@synthesize image;
@synthesize imageFile;
@synthesize photoInfoViewController;



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
	MyPhotoVerifyAppDelegate *appDelegate = (MyPhotoVerifyAppDelegate *) [[UIApplication sharedApplication] delegate];
	
	self.title = [imageFile stringByDeletingPathExtension];
/*	
	UIBarButtonItem *mapButton = [[UIBarButtonItem alloc] initWithTitle:@"Mappa" style:UIBarButtonItemStyleBordered target:self action:@selector(showMap:)];
	self.navigationItem.rightBarButtonItem = mapButton; 
	[mapButton release];
*/	
	UIImage *img = [[UIImage alloc] initWithContentsOfFile:[NSString stringWithFormat:@"%@/%@", [appDelegate imagePath],imageFile]];
	image.image = img;
	[img release];
	
	isView = NO;
	
	//UIBarButtonItem *infoButton = [[UIBarButtonItem alloc] initWithTitle:@"Info" style:UIBarButtonItemStyleBordered target:self action:@selector(onBtnInfo)];
	//self.navigationItem.rightBarButtonItem = infoButton;
	//[infoButton release];
}

-(IBAction) onBtnInfo: (id) sender {
	
	//PhotoInfoViewController *photoInfoViewController = [[PhotoInfoViewController alloc] initWithNibName:@"PhotoInfoViewController" bundle:nil];
	
	[photoInfoViewController setImageFile:imageFile];
	
	if (!isView) {
		[photoInfoViewController setNavCon:[self navigationController]];
		[self.view addSubview:photoInfoViewController.view];
	
	    isView = YES;
	}
	
	[photoInfoViewController.view setHidden:NO];
	
	//[self.navigationController pushViewController:photoInfoViewController animated:YES];
	//[photoInfoViewController release];

}
/*
// Override to allow orientations other than the default portrait orientation.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations------------------------------------------------
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}
*/


- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload {
    [super viewDidUnload];
	self.image = nil;
	self.imageFile = nil;
	self.photoInfoViewController = nil;
	
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}


- (void)dealloc {
	[image release];
	[imageFile release];
	[photoInfoViewController release];
    [super dealloc];
}


@end
