//
//  MyPhotoSignAppDelegate.h
//  MyPhotoSign
//
//  Created by Antonio De Marco on 23/07/10.
//  Copyright __MyCompanyName__ 2010. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "DropboxSDK.h"
#import <stdlib.h>
@class MyPhotoSignViewController;
@class DBRestClient;

@interface MyPhotoSignAppDelegate : NSObject <UIApplicationDelegate,DBRestClientDelegate > {
    UIWindow *window;
    MyPhotoSignViewController *viewController;
	DBRestClient* restClient;
	NSTimer *timer;
	NSInteger fileCount;


	
}
@property (nonatomic, readonly) DBRestClient* restClient;
@property (nonatomic, retain) IBOutlet UIWindow *window;
@property (nonatomic, retain) IBOutlet MyPhotoSignViewController *viewController;
@property (nonatomic, retain) NSTimer *timer;
@property (nonatomic) NSInteger fileCount;


-(Boolean)isAppActivable;

-(void)onTimer:(NSTimer *)timer;
- (NSString *)imagePath;
- (NSString *)certPath;
@end

