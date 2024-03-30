//
//  ViewController.h
//  iOTP
//
//  Created by Giuseppe Cattaneo on 01/02/15.
//  Copyright (c) 2015 Giuseppe Cattaneo. All rights reserved.
//
//
// $Id: MainViewController.h 800 2015-11-28 14:00:37Z cattaneo $
//

#import <UIKit/UIKit.h>

#import "TuitusOTP.h"

#import "PAPasscodeViewController.h"


@interface MainViewController : UIViewController<UITextFieldDelegate, PAPasscodeViewControllerDelegate>

@property NSString *otp;


// public methods
- (void) saveSeed: (NSString *) hexSeed usingSecureStore: (BOOL) useSecureStore;

- (BOOL) moveToSecureStore;
- (BOOL) moveToPIN;



// private methods
- (void)viewDidLoad;
- (void)didReceiveMemoryWarning ;
- (Boolean)isAppActivable;
- (void) alertView:(UIAlertView *)alertView didDismissWithButtonIndex: (NSInteger)buttonIndex;
- (void) alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex;

- (void) loadSeed: (BOOL) displayOTP;
- (void) loadSeedWithPIN: (NSString *) password;
- (void) saveSeed: (NSString *) hexSeed withPIN: (NSString *) pin usingSecureStore: (BOOL) useSecureStore;

- (void) enableApp: (BOOL) value;

- (void) generateOTP;

- (IBAction)unwindToNewOTP:(UIStoryboardSegue *)segue;
- (IBAction)unwindFromSetting:(UIStoryboardSegue *)segue;

@end

