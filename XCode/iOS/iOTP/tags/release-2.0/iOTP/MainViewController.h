//
//  ViewController.h
//  iOTP
//
//  Created by Giuseppe Cattaneo on 01/02/15.
//  Copyright (c) 2015 Giuseppe Cattaneo. All rights reserved.
//
//
// $Id: MainViewController.h 743 2015-06-02 19:50:24Z cattaneo $
//

#import <UIKit/UIKit.h>

#import "TuitusOTP.h"


@interface MainViewController : UIViewController<UITextFieldDelegate>

@property NSString *otp;


// public methods
- (void) saveSeed: (NSString *) hexSeed withPIN: (NSString *) pin;

// private methods
- (void)viewDidLoad;
- (void)didReceiveMemoryWarning ;
- (Boolean)isAppActivable;
- (void) alertView:(UIAlertView *)alertView didDismissWithButtonIndex: (NSInteger)buttonIndex;
- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex;
- (void) loadSeed: (NSString *) password;

- (void) enableApp: (BOOL) value;

- (void) generateOTP;

- (BOOL) isFingerPrintAvailable;

@end

