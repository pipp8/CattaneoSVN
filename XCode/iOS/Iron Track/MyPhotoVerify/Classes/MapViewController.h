//
//  MapViewController.h
//  MyPhotoVerify
//
//  Created by Antonio De Marco on 12/08/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <MapKit/MapKit.h>
#import <MapKit/MKAnnotation.h>
#import <MapKit/MKReverseGeocoder.h>


@interface MapViewController : UIViewController <MKReverseGeocoderDelegate,MKMapViewDelegate>{
	IBOutlet MKMapView *mapView;
	MKReverseGeocoder *geoCoder;
	MKPlacemark *mPlacemark;
	IBOutlet UISegmentedControl *mapType;
	double longitude;
	double latitude;
	NSString *pathThumbImage;
	
}
@property (nonatomic,retain) IBOutlet MKMapView *mapView;
@property (nonatomic,retain) IBOutlet UISegmentedControl *mapType;
@property (nonatomic,retain) MKReverseGeocoder *geoCoder;
@property (nonatomic,retain) NSString *pathThumbImage;
@property (nonatomic) double longitude;
@property (nonatomic) double latitude;

- (IBAction)changeType:(id) sender;
- (IBAction)dismiss:(id) sender;

@end
