//
//  AppInitedViewController.h
//  iOTP
//
//  Created by Giuseppe Cattaneo on 26/04/15.
//  Copyright (c) 2015 Giuseppe Cattaneo. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "RemoteSeedViewController.h"


@interface AppInitedViewController : UIViewController

@property (weak, nonatomic) RemoteSeedViewController * delegate;

@end
