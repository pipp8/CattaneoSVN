//
//  ShowOTPViewController.h
//  iOTP
//
//  Created by Giuseppe Cattaneo on 26/04/15.
//  Copyright (c) 2015 Giuseppe Cattaneo. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ShowOTPViewController : UIViewController

@property NSString * OTP;

- (void) displayOTP: (NSString *) otp withTime: (long) usedTime;

- (void) increaseProgressValue;

@end
