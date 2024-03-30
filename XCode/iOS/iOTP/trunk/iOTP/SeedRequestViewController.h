//
//  SeedRequestViewController.h
//  iOTP
//
//  Created by Giuseppe Cattaneo on 21/04/15.
//  Copyright (c) 2015 Giuseppe Cattaneo. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "RemoteSeedViewController.h"

#import "iOTPMisc.h"

@interface SeedRequestViewController : UIViewController <UITextFieldDelegate, RestartViewControllerDelegate>

@property (weak, nonatomic) RemoteCommunication *remoteService;

@property (weak, nonatomic) RemoteSeedViewController * delegate;

@property (weak, nonatomic) NSString *remoteSeed;

@property BOOL  seedGenerated;


// send the SMS OTP and ask for SEED
- (IBAction)btStep2_Clicked:(id)sender;

-(UIActivityIndicatorView *) getSpinner;

@end
