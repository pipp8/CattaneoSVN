//
//  MapViewController.m
//  MyPhotoVerify
//
//  Created by Antonio De Marco on 12/08/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "MapViewController.h"


@implementation MapViewController
@synthesize mapView;
@synthesize mapType;
@synthesize geoCoder;
@synthesize latitude;
@synthesize longitude;
@synthesize pathThumbImage;
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
	[self setTitle:@"Mappa"];
	//mapView=[[MKMapView alloc] initWithFrame:self.view.bounds];
	//mapView.showsUserLocation=TRUE;
	//mapView.mapType=MKMapTypeStandard;
	//mapView.delegate=self;
	
	/*Region and Zoom*/
	MKCoordinateRegion region;
	MKCoordinateSpan span;
	span.latitudeDelta=.005;
	span.longitudeDelta=.005;
	
	CLLocationCoordinate2D location;  //mapView.userLocation.coordinate;
	
	location.latitude=latitude;
	location.longitude=longitude;
	region.span=span;
	region.center=location;
	
	/*Geocoder Stuff*/
	
	MKReverseGeocoder *geoTMP=[[MKReverseGeocoder alloc] initWithCoordinate:location];
	
	self.geoCoder = geoTMP;
	[geoTMP release];
	
	geoCoder.delegate=self;
	[geoCoder start];
		
	[mapView setRegion:region animated:TRUE];
	[mapView regionThatFits:region];
	//[self.view insertSubview:mapView atIndex:0];
	
}

- (IBAction)changeType:(id)sender{
	if(mapType.selectedSegmentIndex==0){
		mapView.mapType=MKMapTypeStandard;
	}
	else if (mapType.selectedSegmentIndex==1){
		mapView.mapType=MKMapTypeSatellite;
	}
	else if (mapType.selectedSegmentIndex==2){
		mapView.mapType=MKMapTypeHybrid;
	}
}

- (void)reverseGeocoder:(MKReverseGeocoder *)geocoder didFailWithError:(NSError *)error{
	
	
}

- (void)reverseGeocoder:(MKReverseGeocoder *)geocoder didFindPlacemark:(MKPlacemark *)placemark{
	mPlacemark=placemark;
	[mapView addAnnotation:placemark];
}
/*
- (MKAnnotationView *) mapView:(MKMapView *)mapView viewForAnnotation:(id <MKAnnotation>) annotation{

	
	MKPinAnnotationView *annView=[[MKPinAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:@"currentloc" ];
	
	annView.animatesDrop=TRUE;
	return annView; 
	

}

*/

/*
// Override to allow orientations other than the default portrait orientation.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
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
	self.mapView = nil;
	self.mapType = nil;
	self.geoCoder = nil;
	self.pathThumbImage=nil;
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (IBAction)dismiss:(id) sender {

	[self dismissModalViewControllerAnimated:TRUE];

}

- (void)dealloc {
	[mapView release];
	[mapType release];
	[geoCoder release];
	[pathThumbImage release];
    [super dealloc];
}


@end
