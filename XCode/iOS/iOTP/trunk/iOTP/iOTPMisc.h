//
//  iOTPMisc.h
//  iOTP
//
//  Created by Giuseppe Cattaneo on 21/04/15.
//  Copyright (c) 2015 Giuseppe Cattaneo. All rights reserved.
//

#ifndef iOTP_iOTPMisc_h
#define iOTP_iOTPMisc_h

#import "ScaledImage.h"

#define magicNumber         @"1234"

// finestra di validità del token in secondi
#define clockRound          30

#define minPINLength        4

#define maxFailedPINAttempts        5
#define warningFailedPINAttempts    3


static float const kDuration = 1.5;

//
//// posizione della mainView nella lista di view del tabbarcontroller
//#define mainViewIndex   1
//
//// posizione della SeedSetupView nella lista di view del tabbarcontroller
//#define seedSetupViewIndex   2

// 0x1961A7
// #define BlueColor [UIColor colorWithRed:0x19/255.0f green:0x61/255.0f blue:0xA7/255.0f alpha:1.0f]
// 0x2B80BD
#define BlueColor           [UIColor colorWithRed:0.168 green:0.5 blue:0.741 alpha:1.0]

// 0xC7C9CC
#define GrayBackgroundColor [UIColor colorWithRed:0xE1/255.0f green:0xE1/255.0f blue:0xE1/255.0f alpha:1.0f]

//
#define ButtonDisabledColor [UIColor colorWithRed:0x90/255.0f green:0x90/255.0f blue:0x90/255.0f alpha:1.0f]


@protocol RestartViewControllerDelegate <NSObject>


-(void) restartView: (NSString *) value result: (BOOL) code;

-(UIActivityIndicatorView *) getSpinner;

@end

#endif
