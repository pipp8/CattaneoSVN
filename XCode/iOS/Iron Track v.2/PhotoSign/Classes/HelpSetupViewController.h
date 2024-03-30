//
//  HelpSetupViewController.h
//  MyPhotoSign
//
//  Created by Antonio De Marco on 10/11/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MongooseWrapper.h"

@interface HelpSetupViewController : UIViewController {
	MongooseWrapper *webServer;
}

@property (nonatomic, retain) MongooseWrapper *webServer;
- (IBAction)onBtnVerifySetup: (id)sender;
- (IBAction)onBtnWebServerActive: (id)sender;

@end
