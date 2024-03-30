//
//  RemoteSeedViewController.h
//  iOTP
//
//  Created by Giuseppe Cattaneo on 23/02/15.
//  Copyright (c) 2015 Giuseppe Cattaneo. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "iOTPMisc.h"

#import "RemoteCommunication.h"

@interface RemoteSeedViewController : UIViewController <UITextFieldDelegate, RestartViewControllerDelegate>

@property RemoteCommunication *remoteService;

-(void) restartView: (NSString *) value result: (BOOL) code;

@end
