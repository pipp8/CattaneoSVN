//
//  iOTPMisc.h
//  iOTP
//
//  Created by Giuseppe Cattaneo on 21/04/15.
//  Copyright (c) 2015 Giuseppe Cattaneo. All rights reserved.
//

#ifndef iOTP_iOTPMisc_h
#define iOTP_iOTPMisc_h

// posizione della mainView nella lista di view del tabbarcontroller
#define mainViewIndex   1

// posizione della SeedSetupView nella lista di view del tabbarcontroller
#define seedSetupViewIndex   2

// 0x1961A7
#define BlueColor [UIColor colorWithRed:0x19/255.0f green:0x61/255.0f blue:0xA7/255.0f alpha:1.0f]

// 0xC7C9CC
#define GrayBackgroundColor [UIColor colorWithRed:0xE1/255.0f green:0xE1/255.0f blue:0xE1/255.0f alpha:1.0f]

//
#define ButtonDisabledColor [UIColor colorWithRed:0x90/255.0f green:0x90/255.0f blue:0x90/255.0f alpha:1.0f]


@protocol RestartViewControllerDelegate <NSObject>


-(void) restartView: (NSString *) value result: (BOOL) code;

@end

#endif
