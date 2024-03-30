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
    IBOutlet UILabel *accuracy; 



	
}
@property (retain, nonatomic) CLLocationManager *locationManager;
@property (retain, nonatomic) CLLocation *locationPoint;
@property (retain, nonatomic) IBOutlet UILabel *accuracy;


@end
