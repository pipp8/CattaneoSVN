//
//  SpirographAppDelegate.m
//  Spirograph
//
// $Header: http://maccattaneo:8081/svn/Cattaneo/XCode/iOS/Spirograph/Spirograph/Classes/SpirographAppDelegate.m 159 2011-08-31 18:02:00Z cattaneo $
// $Id: SpirographAppDelegate.m 159 2011-08-31 18:02:00Z cattaneo $
//
//  Created by Giuseppe Cattaneo on 16/08/11.
//  Copyright 2011 Dip. Informatica - Università di Salerno. All rights reserved.
//

#import "SpirographAppDelegate.h"
#import "CanvasController.h"
#import "OptionsController.h"
#import "SplashScreenController.h"


@implementation SpirographAppDelegate


@synthesize navigationCtrl=_navigationCtrl;
@synthesize splashView;
@synthesize window=_window;


- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    assert(self.window != nil);
    assert(self.navigationCtrl != nil);
/*
    UIViewController *paramCtrl = [[OptionsController alloc]
                                         initWithNibName:@"OptionsController" bundle:nil];
    
    assert(paramCtrl != nil);
    
    [self.navigationCtrl pushViewController:paramCtrl animated:NO];
    [paramCtrl release];
*/
/*
    [self.splashView setImage:[[UIImage imageNamed:@"Default.png"] retain]];
 */
    [self.window addSubview:self.navigationCtrl.view];
	[self.window makeKeyAndVisible];

    return YES;
}

- (void)applicationWillResignActive:(UIApplication *)application
{
    /*
     Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
     Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
     */
}

- (void)applicationDidEnterBackground:(UIApplication *)application
{
    /*
     Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later. 
     If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
     */
}

- (void)applicationWillEnterForeground:(UIApplication *)application
{
    /*
     Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
     */
}

- (void)applicationDidBecomeActive:(UIApplication *)application
{
    /*
     Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
     */
}

- (void)applicationWillTerminate:(UIApplication *)application
{
    /*
     Called when the application is about to terminate.
     Save data if appropriate.
     See also applicationDidEnterBackground:.
     */
}

- (void)dealloc
{
    [_window release];
    [navigationCtrl release];
    [splashView release];
    [super dealloc];
}


@end
