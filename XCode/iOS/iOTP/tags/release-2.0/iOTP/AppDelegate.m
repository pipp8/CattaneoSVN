//
//  AppDelegate.m
//  iOTP
//
//  Created by Giuseppe Cattaneo on 01/02/15.
//  Copyright (c) 2015 Giuseppe Cattaneo. All rights reserved.
//
//
// $Id: AppDelegate.m 743 2015-06-02 19:50:24Z cattaneo $
//

#import "AppDelegate.h"


@interface AppDelegate ()

@end

@implementation AppDelegate


- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
   
    // Override point for customization after application launch.
    // personalizzazione della navigation bar
    if([[[UIDevice currentDevice] systemVersion] floatValue] < 7.0)
    {
        // [[UINavigationBar appearance] setBackgroundImage:[UIImage imageNamed:@"navigatio_for_ios6"] forBarMetrics:UIBarMetricsDefault];
        
        [[UINavigationBar appearance] setTitleVerticalPositionAdjustment:0.0 forBarMetrics:UIBarMetricsDefault];
    }
    else
    {
        [[UINavigationBar appearance] setBarTintColor:[UIColor colorWithRed:0x00/255.0f
                                                                      green:0x85/255.0f
                                                                       blue:0xc8/255.0f
                                                                      alpha:1.0f]];
        
        // Uncomment to change the color of back button
        [[UINavigationBar appearance] setTintColor:[UIColor whiteColor]];
        
        // Uncomment to assign a custom backgroung image
        // [[UINavigationBar appearance] setBackgroundImage:[UIImage imageNamed:@"navigon_bg_ios7.png"] forBarMetrics:UIBarMetricsDefault];
        
        [[UINavigationBar appearance] setBackgroundColor:[UIColor whiteColor]];
//        // [[UINavigationBar appearance] setBackIndicatorTransitionMaskImage:[UIImage imageNamed:@"back10-32x32.png"]];
//        
//        NSShadow *shadow = [[NSShadow alloc] init];
//        shadow.shadowColor = [UIColor colorWithRed:0.0 green:0.0 blue:0.0 alpha:0.8];
//        shadow.shadowOffset = CGSizeMake(0, 1);
//        [[UINavigationBar appearance] setTitleTextAttributes: [NSDictionary dictionaryWithObjectsAndKeys:
//                                                               [UIColor colorWithRed:245.0/255.0 green:245.0/255.0 blue:245.0/255.0 alpha:1.0], NSForegroundColorAttributeName,
//                                                               shadow, NSShadowAttributeName,
//                                                               [UIFont fontWithName:@"HelveticaNeue-CondensedBlack" size:21.0], NSFontAttributeName, nil]];
    }
//    for (NSString* family in [UIFont familyNames])
//    {
//        NSLog(@"%@", family);
//        
//        for (NSString* name in [UIFont fontNamesForFamilyName: family])
//        {
//            NSLog(@"  %@", name);
//        }
//    }
    return YES;
}

- (void)applicationWillResignActive:(UIApplication *)application {
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
}

- (void)applicationDidEnterBackground:(UIApplication *)application {
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
}

- (void)applicationWillEnterForeground:(UIApplication *)application {
    // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
}

- (void)applicationDidBecomeActive:(UIApplication *)application {
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
}

- (void)applicationWillTerminate:(UIApplication *)application {
    // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
}

@end
