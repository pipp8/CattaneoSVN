//
//  MyPhotoSignAppDelegate.m
//  MyPhotoSign
//
//  Created by Antonio De Marco on 23/07/10.
//  Copyright __MyCompanyName__ 2010. All rights reserved.
//

#import "MyPhotoSignAppDelegate.h"
#import "MyPhotoSignViewController.h"
#import "DBSession.h"
//#import <stdlib.h>
@implementation MyPhotoSignAppDelegate

@synthesize window;
@synthesize viewController;
@synthesize restClient;
@synthesize timer;
@synthesize fileCount;


BOOL gLogging = FALSE;

#pragma mark -
#pragma mark Application lifecycle

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {    

    // Override point for customization after application launch.
    self.fileCount = 0;
	NSString* consumerKey = @"233jcnzrh5w8558"; //@"<CONSUMER_KEY HERE>";
	NSString* consumerSecret = @"pefeghmuf8n1b97"; // @"<CONSUMER_SECRET HERE>";
	
	// DBSession inizializzato per l'uso nell'applicativo
	
	DBSession* session = [[DBSession alloc] initWithConsumerKey:consumerKey consumerSecret:consumerSecret];
	[DBSession setSharedSession:session];
    [session release];
		
    // Add the view controller's view to the window and display.
	[self isAppActivable];
	self.timer = [NSTimer scheduledTimerWithTimeInterval: 10.0  target: self	selector: @selector(onTimer:) userInfo: nil	 repeats: YES];
	[window addSubview:viewController.view];
    [window makeKeyAndVisible];
   
	
    return YES;
}

-(void)onTimer:(NSTimer *)timer {

    
	if (([[DBSession sharedSession] isLinked]) && fileCount == 0) {
		
		NSFileManager *fileManager = [[NSFileManager alloc] init];

		NSMutableArray* directoryContent = [[NSMutableArray alloc] initWithArray:[[fileManager contentsOfDirectoryAtPath:[self imagePath] error:nil] filteredArrayUsingPredicate:[NSPredicate predicateWithFormat:@"self ENDSWITH '.jpg'"] ]]; 
			
		for (NSInteger i=0 ; i< [directoryContent count];i++) {
			NSString *fileName = [directoryContent objectAtIndex:i];
			fileName = [fileName lastPathComponent];
			fileCount++;
		    
			[self.restClient uploadFile:fileName toPath:@"/Apps/MyPhotoSign-Verify/Images" fromPath:[NSString stringWithFormat:@"%@/%@",[self imagePath],fileName]   ];
			//Un upload alla volta
			break;
		}
		
		[fileManager release];
		[directoryContent release];
		
	}
}

- (void)restClient:(DBRestClient *)client uploadProgress:(CGFloat)progress forFile:(NSString *)srcPath {

	[viewController.activityIndicator startAnimating];
	[viewController.progressView setHidden:FALSE];
    [viewController startProcessing:nil];
	[viewController changeLabelprogressText: @"Condivisione immagine in corso ..."];
	
	[viewController changeProgressBarValue:  [NSNumber numberWithFloat:progress]];
	//NSString *value =  [NSString stringWithFormat:@"%.2f",progress];
	//float newvalue = [value floatValue];
	//int intvalue = newvalue*100;
	//viewController.lblFileProgress.text = [NSString stringWithFormat:@"(%i)=>%i%%",fileCount,intvalue];
}


- (void)restClient:(DBRestClient*)client metadataUnchangedAtPath:(NSString*)path {
	
}

- (void)restClient:(DBRestClient*)client loadMetadataFailedWithError:(NSError*)error {

	[[[[UIAlertView alloc] 
       initWithTitle:@"Errore" message:@"Impossibile prelevare la lista delle immagini\n dal servizio DropBox."
       delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil]
      autorelease]
     show];
	fileCount = 0;
	
}

- (void)restClient:(DBRestClient*)client loadedMetadata:(DBMetadata*)metadata {

}


- (void)restClient:(DBRestClient *)client uploadedFile:(NSString *)srcPath {
	NSFileManager *fileManager = [[NSFileManager alloc] init];
	
	[fileManager removeItemAtPath:srcPath error:nil];
	
	[fileManager release];
	
	fileCount--;
	
	if (fileCount <= 0) 
		fileCount = 0;
	//[viewController.activityIndicator stopAnimating];
	//[viewController.progressView setHidden:TRUE];
	//[viewController.progressView setProgress:0];
	[viewController stopProcessing:nil];
	//[viewController.lblProcess setText:@""];
}

- (void)restClient:(DBRestClient *)client uploadFileFailedWithError:(NSError *)error {

	fileCount--;
	
	if (fileCount <= 0)
		fileCount = 0;
	
	//[viewController.activityIndicator stopAnimating];
	//[viewController.progressView setHidden:TRUE];
	//[viewController.progressView setProgress:0];
	[viewController stopProcessing:nil];
//	[viewController.lblProcess setText:@""];
	
}

- (NSString *)imagePath {
	
	NSString *imagePath;
	imagePath = [NSString stringWithFormat:@"%@/%@", @"Documents",@"Images"];
	imagePath = [NSHomeDirectory() stringByAppendingPathComponent:imagePath];
	return imagePath;
	
}

- (NSString *)certPath {
	
	NSString *certFilePath;
	certFilePath = [NSString stringWithFormat:@"%@/%@", @"Documents",@"Private"];
	certFilePath = [NSHomeDirectory() stringByAppendingPathComponent:certFilePath];
	return certFilePath;
	

}

-(Boolean)isAppActivable {
	Boolean test = NO;
  	BOOL isDir;
	
	NSFileManager *fileManager = [[NSFileManager alloc] init];
	if (!(([fileManager fileExistsAtPath:[self imagePath] isDirectory:&isDir]) && isDir)) {
	    [fileManager createDirectoryAtPath:[self imagePath] withIntermediateDirectories:FALSE attributes:nil error:nil];
		
	}
	
	// NSBundle *thisBundle = [NSBundle bundleForClass:[self class]];
	// NSString *keyPath = [thisBundle pathForResource:@"mykey" ofType:@"key"];
	// NSString *certFilePath = [thisBundle pathForResource:@"mycert" ofType:@"crt"];
	// NSString *htmlPath = [thisBundle pathForResource:@"mycert" ofType:@"index.html"];
	
	if (!(([fileManager fileExistsAtPath:[self certPath] isDirectory:&isDir]) && isDir)) {
	    [fileManager createDirectoryAtPath:[self certPath] withIntermediateDirectories:FALSE attributes:nil error:nil];
		NSString *htmlPath;		
		htmlPath = [NSString stringWithFormat:@"%@/%@", [self certPath],@"index.html"];
		NSString *indexPath = [[NSBundle mainBundle] pathForResource:@"index" ofType:@"html"];
		[fileManager copyItemAtPath:indexPath toPath:htmlPath error:nil];
	}
	
	NSString *certFilePath;
	certFilePath = [NSString stringWithFormat:@"%@/%@", [self certPath],@"mycert.crt"];
		
	NSString *keyPath;
	keyPath = [NSString stringWithFormat:@"%@/%@", [self certPath],@"mykey.key"];
	
	if (![fileManager fileExistsAtPath:certFilePath isDirectory:&isDir]) {
	    test = NO;
		
	} else {
		if ([fileManager fileExistsAtPath:keyPath isDirectory:&isDir]) 
			test = YES;		
	}
		
	[fileManager release];
	
	return test;
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

- (DBRestClient*)restClient {
    if (restClient == nil) {
    	restClient = [[DBRestClient alloc] initWithSession:[DBSession sharedSession]];
    	restClient.delegate = self;
    }
    return restClient;
}


- (void)dealloc {
    [timer invalidate];
	[timer release];
	[restClient release];
	[viewController release];
	
    [window release];
    [super dealloc];
}


@end
