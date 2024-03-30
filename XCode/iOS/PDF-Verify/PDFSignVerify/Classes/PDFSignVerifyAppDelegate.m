//
//  MyPhotoVerifyAppDelegate.m
//  MyPhotoVerify
//
//  Created by Antonio De Marco on 28/07/10.
//  Copyright __MyCompanyName__ 2010. All rights reserved.
//

#import "PDFSignVerifyAppDelegate.h"

#import "DBSession.h"
#import <stdlib.h>

@implementation PDFSignVerifyAppDelegate

@synthesize window;
@synthesize navigationController;

@synthesize docPath;
@synthesize certPath;


BOOL gLogging = FALSE;

#pragma mark -
#pragma mark Application lifecycle

- (NSString *)getCertPath {
	
	NSString *localCertPath;
	localCertPath = [NSString stringWithFormat:@"%@/%@", @"Documents",@"Certs"];
	localCertPath = [NSHomeDirectory() stringByAppendingPathComponent:localCertPath];
	return localCertPath;
	
}

- (NSString *)getDocPath {
	
	NSString *localDocPath;
	localDocPath = [NSString stringWithFormat:@"%@/%@", @"Documents",@"SignedDocuments"];
	localDocPath = [NSHomeDirectory() stringByAppendingPathComponent:localDocPath];
	return localDocPath;
}


- (void) setDefaultPaths {

	NSString *docDir;
	NSString *certDir;
	NSString *localDir;
	
	NSBundle* myBundle = [NSBundle mainBundle];
	
	// If we succeeded, look for our property.
	if ( myBundle != NULL ) {
		docDir = [myBundle objectForInfoDictionaryKey:@"DocumentsDir"];
		certDir = [myBundle objectForInfoDictionaryKey:@"CertificateDir"];
		localDir = [myBundle objectForInfoDictionaryKey:@"LocalDocDir"];
	}
	
	[self setDocPath:[NSString stringWithFormat:@"%@/%@/%@", NSHomeDirectory(), localDir, docDir]];
	[self setCertPath:[NSString stringWithFormat:@"%@/%@/%@", NSHomeDirectory(), localDir, certDir]];
}
				 

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {    
    
    // Override point for customization after application launch.
	
	BOOL isDir;
	
	[self setDefaultPaths];
	
	NSFileManager *fileManager = [[NSFileManager alloc] init];
	if (!(([fileManager fileExistsAtPath:self.docPath isDirectory:&isDir]) && isDir)) {
	    [fileManager createDirectoryAtPath:self.docPath withIntermediateDirectories:FALSE attributes:nil error:nil];
		
	}
	
	if (!(([fileManager fileExistsAtPath:self.certPath isDirectory:&isDir]) && isDir)) {
	    [fileManager createDirectoryAtPath:self.certPath withIntermediateDirectories:FALSE attributes:nil error:nil];
		
	}
	
	[fileManager release];
	
	
	/* fabio */
	//NSString* consumerKey = @"vbujt3ygsb3y4lo"; // @"CONSUMER_KEY_HERE";
	//NSString* consumerSecret = @"vo6lymscq3iv6am"; // @"CONSUMER_SECRET_HERE";
	
	
	/* Maurizio */
	// NSString* consumerKey = @"sqivlyty0e0l4i1"; // @"CONSUMER_KEY_HERE";
	// NSString* consumerSecret = @"o3gpmpt0xcsnva8"; // @"CONSUMER_SECRET_HERE";

	/* Pippo */
	NSString* consumerKey = @"iqblq3rg2kvmv8r"; //@"<CONSUMER_KEY HERE>";
	NSString* consumerSecret = @"kwv4yitinq0r7y9"; // @"<CONSUMER_SECRET HERE>";

	
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
	[docPath release];
	[certPath release];
	[super dealloc];
}


@end

