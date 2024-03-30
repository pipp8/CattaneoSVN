//
//  MyPhotoVerifyAppDelegate.m
//  MyPhotoVerify
//
//  Created by Antonio De Marco on 28/07/10.
//  Copyright __MyCompanyName__ 2010. All rights reserved.
//

#import "MyPhotoVerifyAppDelegate.h"

#import "DBSession.h"
#import <stdlib.h>

@implementation MyPhotoVerifyAppDelegate

@synthesize window;
@synthesize navigationController;

BOOL gLogging = FALSE;

#pragma mark -
#pragma mark Application lifecycle

- (NSString *)certPath {
	
	NSString *certPath;
	certPath = [NSString stringWithFormat:@"%@/%@", @"Documents",@"Certs"];
	certPath = [NSHomeDirectory() stringByAppendingPathComponent:certPath];
	return certPath;
	
}

- (NSString *)imagePath {
	
	NSString *imgPath;
	imgPath = [NSString stringWithFormat:@"%@/%@", @"Documents",@"Images"];
	imgPath = [NSHomeDirectory() stringByAppendingPathComponent:imgPath];
	return imgPath;
}


- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {    
    
    // Override point for customization after application launch.
   
	
	BOOL isDir;
	
	NSFileManager *fileManager = [[NSFileManager alloc] init];
	if (!(([fileManager fileExistsAtPath:[self imagePath] isDirectory:&isDir]) && isDir)) {
	    [fileManager createDirectoryAtPath:[self imagePath] withIntermediateDirectories:FALSE attributes:nil error:nil];
		
	}
	
	if (!(([fileManager fileExistsAtPath:[self certPath] isDirectory:&isDir]) && isDir)) {
	    [fileManager createDirectoryAtPath:[self certPath] withIntermediateDirectories:FALSE attributes:nil error:nil];
		
	}
	
	[fileManager release];
	
	
	NSString* consumerKey = @"233jcnzrh5w8558"; // @"CONSUMER_KEY_HERE";
	NSString* consumerSecret = @"pefeghmuf8n1b97"; // @"CONSUMER_SECRET_HERE";
	
	// Look below where the DBSession is created to understand how to use DBSession in your app
	
	DBSession* session = [[DBSession alloc] initWithConsumerKey:consumerKey consumerSecret:consumerSecret];
	[DBSession setSharedSession:session];
    [session release];
	
	
	// Add the navigation controller's view to the window and display.
	
	
   	
	[window addSubview:navigationController.view];
    [window makeKeyAndVisible];

    return YES;
}


- (void)applicationWillResignActive:(UIApplication *)application {
    /*
     Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
     Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
     */
}


- (void)applicationDidEnterBackground:(UIApplication *)application {
    /*
     Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later. 
     If your application supports background execution, called instead of applicationWillTerminate: when the user quits.
     */
}


- (void)applicationWillEnterForeground:(UIApplication *)application {
    /*
     Called as part of  transition from the background to the inactive state: here you can undo many of the changes made on entering the background.
     */
}


- (void)applicationDidBecomeActive:(UIApplication *)application {
    /*
     Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
     */
}


- (void)applicationWillTerminate:(UIApplication *)application {
    /*
     Called when the application is about to terminate.
     See also applicationDidEnterBackground:.
     */
}


#pragma mark -
#pragma mark Memory management

- (void)applicationDidReceiveMemoryWarning:(UIApplication *)application {
    /*
     Free up as much memory as possible by purging cached data objects that can be recreated (or reloaded from disk) later.
     */
}


- (void)dealloc {

	[navigationController release];
	[window release];

	[super dealloc];
}


@end

