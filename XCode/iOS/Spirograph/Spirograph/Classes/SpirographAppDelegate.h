//
//  SpirographAppDelegate.h
//  Spirograph
//
// $Header: http://maccattaneo:8081/svn/Cattaneo/XCode/iOS/Spirograph/Spirograph/Classes/SpirographAppDelegate.h 159 2011-08-31 18:02:00Z cattaneo $
// $Id: SpirographAppDelegate.h 159 2011-08-31 18:02:00Z cattaneo $
//
//  Created by Giuseppe Cattaneo on 16/08/11.
//  Copyright 2011 Dip. Informatica - Università di Salerno. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface SpirographAppDelegate : NSObject <UIApplicationDelegate> {
	IBOutlet UIWindow *window;
    IBOutlet UINavigationController *navigationCtrl;
    UIImageView *splashView;
}
@property (nonatomic, retain) IBOutlet UINavigationController *navigationCtrl;
@property (nonatomic, retain) IBOutlet UIImageView *splashView;

@property (nonatomic, readwrite, retain) IBOutlet UIWindow *window;

@end
