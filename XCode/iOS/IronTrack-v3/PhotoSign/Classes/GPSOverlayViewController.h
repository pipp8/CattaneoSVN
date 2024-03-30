//
//  GPSOverlayViewController.h
//  MyPhotoMap
//
//  Created by Antonio De Marco on 06/06/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <CoreLocation/CoreLocation.h>

@interface GPSOverlayViewController : UIViewController <CLLocationManagerDelegate> {
	CLLocationManager *locationManager;
	CLLocation        *locationPoint;
    IBOutlet UILabel  *accuracy;
	
}

@property (retain, nonatomic, strong) CLLocationManager *locationManager;
@property (retain, atomic, strong) CLLocation *locationPoint;
@property (retain, atomic) IBOutlet UILabel *accuracy;


@end
