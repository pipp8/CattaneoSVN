//
//  GPSOverlayViewController.m
//  MyPhotoMap
//
//  Created by Antonio De Marco on 06/06/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "GPSOverlayViewController.h"
#import "PhotoSignAppDelegate.h"


@implementation GPSOverlayViewController

@synthesize locationManager;
@synthesize locationPoint;
@synthesize accuracy;



/*
 // The designated initializer.  Override if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    if (self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil]) {
        // Custom initialization
    }
    return self;
}
*/
/*
- (void) awakeFromNib {
	[[NSNotificationCenter defaultCenter]
	 addObserver:self
	 selector:@selector(startActivityIndicator:)
	 name:@"startActivityIndicator"
	 object:nil ] ;
	
	[[NSNotificationCenter defaultCenter]
	 addObserver:self
	 selector:@selector(stopActivityIndicator:)
	 name:@"stopActivityIndicator"
	 object:nil ] ;
	
}
*/
// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];  
	
	locationManager = [[CLLocationManager alloc] init];
	locationManager.delegate = self;
    // locationManager.desiredAccuracy = kCLLocationAccuracyBest;
    locationManager.desiredAccuracy = kCLLocationAccuracyKilometer;
    locationManager.distanceFilter = 500; // meters
    
    locationPoint = nil;
    
    CLAuthorizationStatus locationStatus = [CLLocationManager authorizationStatus];
    
    if (locationStatus == kCLAuthorizationStatusRestricted ||
        locationStatus == kCLAuthorizationStatusDenied)
    {

        UIAlertView * myAlertView =  [[UIAlertView alloc]
                                  initWithTitle:@"GPS not enabled"
                                  message:@"Please enable GPS location service. App aborted"
                                  delegate:self cancelButtonTitle:@"OK"
                                  otherButtonTitles:nil];
        [myAlertView show];
        [myAlertView release];
        
        NSLog(@"No Location Service Available");
        
        // exit TBI
    }
    else if (locationStatus ==  kCLAuthorizationStatusNotDetermined)
    {
        [locationManager requestWhenInUseAuthorization];
    }
    
    if ([locationManager locationServicesEnabled ] == NO)
    {
        NSLog(@"No Location Service Available");
        
    }
    
	[locationManager startUpdatingLocation];
    
    [locationManager requestLocation];
	
}



/*
// Override to allow orientations other than the default portrait orientation.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    return (interfaceOrientation == UIInterfaceOrientationLandscapeRight);
}

*/

- (void)didReceiveMemoryWarning {
	// Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
	
	// Release any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload {
	// Release any retained subviews of the main view.
	// e.g. self.myOutlet = nil;
	self.locationManager = nil;
	self.locationPoint = nil;
	
	self.accuracy = nil;


	[super viewDidUnload];	
}


// Delegate method from the CLLocationManagerDelegate protocol.

- (void)locationManager:(CLLocationManager *)manager  didUpdateLocations:(NSArray *)locations {
    
    // If it's a relatively recent event, turn off updates to save power.
    
    if (locationPoint != nil) {
        [locationPoint release];
    }
    
    locationPoint = [[locations lastObject] copy];
    
    NSDate* eventDate = locationPoint.timestamp;
    
    NSTimeInterval howRecent = [eventDate timeIntervalSinceNow];
    
    if (abs(howRecent) < 15.0) {
        // If the event is recent, do something with it.
    }
    accuracy.text = [[NSString alloc] initWithFormat:@"%.0fm", locationPoint.horizontalAccuracy];

    NSLog(@"latitude %+.6f, longitude %+.6f\n",
          locationPoint.coordinate.latitude,
          locationPoint.coordinate.longitude);
    
}

// da ios 6.0 non invoca piu' il metodo seguente $£%"%$£"%"£%$"%£$"%$
- (void)locationManager:(CLLocationManager *)manager didUpdateToLocation:(CLLocation *)newLocation fromLocation:(CLLocation *)oldLocation {
	if (locationPoint == nil) 
		self.locationPoint = newLocation;

	//lblLongitude.text = [[NSString alloc] initWithFormat:@"%g",newLocation.coordinate.longitude];
	
	//MKCoordinateRegion region;
	//region.center=newLocation.coordinate;
	//Set Zoom level using Span
	//MKCoordinateSpan span;
	//span.latitudeDelta=.005;
	//span.longitudeDelta=.005;
	//region.span=span;
	//[mapView setRegion:region animated:TRUE];	


	//latitude = [[NSString alloc] initWithFormat:@"%g",newLocation.coordinate.latitude];  
    accuracy.text = [[NSString alloc] initWithFormat:@"%.0fm",newLocation.horizontalAccuracy]; 	
	//accuracy.text = [[NSString alloc] initWithFormat:@"%gm",newLocation.horizontalAccuracy];
	//verticalAccuracy = [[NSString alloc] initWithFormat:@"%g",newLocation.verticalAccuracy]; 	
	//altitude = [[NSString alloc] initWithFormat:@"%g",newLocation.altitude]; 
}	

- (void)locationManager:(CLLocationManager *)manager didFailWithError:(NSError *)error {
	NSString *errorMessage;

	if (error.code == kCLErrorDenied)
		errorMessage = @"Accesso negato";
	else 
		errorMessage = @"Errore generale";
	
	UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Si è verificato un errore" message:errorMessage delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
    [alert show];
	[alert release];
}

- (IBAction)onbtnMapVisible:(id)sender {
  
}


- (void)dealloc {
	[accuracy release];
    [locationManager release];
	[locationPoint release];


	[super dealloc];
}


@end
