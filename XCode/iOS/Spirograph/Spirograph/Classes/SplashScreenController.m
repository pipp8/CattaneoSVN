//
//  SplashScreenController.m
//  Spirograph
//
// $Header: http://maccattaneo:8081/svn/Cattaneo/XCode/iOS/Spirograph/Spirograph/Classes/SplashScreenController.m 161 2011-08-31 18:45:42Z cattaneo $
// $Id: SplashScreenController.m 161 2011-08-31 18:45:42Z cattaneo $
//
//  Created by Giuseppe Cattaneo on 25/08/11.
//  Copyright 2011 Dip. Informatica - Università di Salerno. All rights reserved.
//

#import "SplashScreenController.h"

#import "OptionsController.h"

NSInteger majorRev = 1;
NSString * revision = @"$Revision: 161 $";


@implementation SplashScreenController
@synthesize lblVersion;
@synthesize btStart;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)dealloc
{
    [btStart release];
    [lblVersion release];
    [super dealloc];
}

- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
    NSString * minorRev = [revision substringWithRange: NSMakeRange(11, [revision length] - 12)];
    NSString * dat = @"$Date: 2011-08-31 20:45:42 +0200(Mer, 31 Ago 2011) $";
    NSString * date = [dat substringWithRange: NSMakeRange(6, 20)];
    [lblVersion setText:[NSString stringWithFormat:@"Versione %d.%@ del %@", majorRev, minorRev, date]];
}

- (void)viewDidUnload
{
    [self setBtStart:nil];
    [self setLblVersion:nil];
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}
- (IBAction)startAction:(id)sender {
    UIViewController *paramCtrl = [[OptionsController alloc]
                                   initWithNibName:@"OptionsController" bundle:nil];
    
    assert(paramCtrl != nil);
    
    [self.navigationController pushViewController:paramCtrl animated:NO];
    [paramCtrl release];
}

@end
