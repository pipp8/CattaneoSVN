//
//  MyPhotoVerifyAppDelegate.m
//  MyPhotoVerify
//
//  Created by Antonio De Marco on 28/07/10.
//  Copyright __MyCompanyName__ 2010. All rights reserved.
//

#import "MyPhotoVerifyAppDelegate.h"

#import <DropboxSDK/DBSession.h>

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
	
	
    // Set these variables before launching the app
    NSString* appKey = @"kmmg9tige2mrbg2"; // @"CONSUMER_KEY_HERE";
	NSString* appSecret = @"96ytgh9x0urg28w"; // @"CONSUMER_SECRET_HERE";
    // You can determine if you have App folder access or Full Dropbox along with your consumer key/secret
    // from https://dropbox.com/developers/apps
    NSString *root = kDBRootDropbox; // Should be set to either kDBRootAppFolder or kDBRootDropbox
	
	// Look below where the DBSession is created to understand how to use DBSession in your app
//	
//	DBSession* session = [[DBSession alloc] initWithConsumerKey:appKey consumerSecret:consumerSecret];
//	[DBSession setSharedSession:session];
//    [session release];
    
    // Look below where the DBSession is created to understand how to use DBSession in your app
    
    NSString* errorMsg = nil;
    if ([appKey rangeOfCharacterFromSet:[[NSCharacterSet alphanumericCharacterSet] invertedSet]].location != NSNotFound) {
        errorMsg = @"Make sure you set the app key correctly in MyPhotoVerifyAppDelegate.m";
    } else if ([appSecret rangeOfCharacterFromSet:[[NSCharacterSet alphanumericCharacterSet] invertedSet]].location != NSNotFound) {
        errorMsg = @"Make sure you set the app secret correctly in MyPhotoVerifyAppDelegate.m";
    } else if ([root length] == 0) {
        errorMsg = @"Set your root to use either App Folder of full Dropbox";
    } else {
        NSString *plistPath = [[NSBundle mainBundle] pathForResource:@"Info" ofType:@"plist"];
        NSData *plistData = [NSData dataWithContentsOfFile:plistPath];
        NSDictionary *loadedPlist =
        [NSPropertyListSerialization
         propertyListFromData:plistData mutabilityOption:0 format:NULL errorDescription:NULL];
        NSString *scheme = [[[[loadedPlist objectForKey:@"CFBundleURLTypes"] objectAtIndex:0] objectForKey:@"CFBundleURLSchemes"] objectAtIndex:0];
        if ([scheme isEqual:@"db-APP_KEY"]) {
            errorMsg = @"Set your URL scheme correctly in PyPhotoVerify-Info.plist";
        }
    }
    
    DBSession* session = [[DBSession alloc] initWithAppKey:appKey appSecret:appSecret root:root];
    session.delegate = self; // DBSessionDelegate methods allow you to handle re-authenticating
    [DBSession setSharedSession:session];
    [session release];
    
    [DBRequest setNetworkRequestDelegate:self];
    
    if (errorMsg != nil) {
        [[[[UIAlertView alloc]
           initWithTitle:@"Error Configuring Session" message:errorMsg
           delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil]
          autorelease]
         show];
    }

    if (![[DBSession sharedSession] isLinked]) {
        // always link the dropbox session
        [[DBSession sharedSession] linkFromController:self];
    }
    
	// [window addSubview:navigationController.view];
    [window setRootViewController:navigationController];    // XCode 7
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

#pragma mark DBSessionDelegate methods

- (void)sessionDidReceiveAuthorizationFailure:(DBSession*)session userId:(NSString *)userId {
    relinkUserId = [userId retain];
    [[[[UIAlertView alloc]
       initWithTitle:@"Dropbox Session Ended" message:@"Do you want to relink?" delegate:self
       cancelButtonTitle:@"Cancel" otherButtonTitles:@"Relink", nil]
      autorelease]
     show];
}


//#pragma mark -
//#pragma mark UIAlertViewDelegate methods
//
//- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)index {
//    if (index != alertView.cancelButtonIndex) {
//        [[DBSession sharedSession] linkUserId:relinkUserId fromController:rootViewController];
//    }
//    [relinkUserId release];
//    relinkUserId = nil;
//}
//

#pragma mark -
#pragma mark DBNetworkRequestDelegate methods

static int outstandingRequests;

- (void)networkRequestStarted {
    outstandingRequests++;
    if (outstandingRequests == 1) {
        [[UIApplication sharedApplication] setNetworkActivityIndicatorVisible:YES];
    }
}

- (void)networkRequestStopped {
    outstandingRequests--;
    if (outstandingRequests == 0) {
        [[UIApplication sharedApplication] setNetworkActivityIndicatorVisible:NO];
    }
}



- (void)dealloc {

	[navigationController release];
	[window release];

	[super dealloc];
}


@end

