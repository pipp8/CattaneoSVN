//
//  MyPhotoVerifyAppDelegate.h
//  MyPhotoVerify
//
//  Created by Antonio De Marco on 28/07/10.
//  Copyright __MyCompanyName__ 2010. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "DropboxSDK.h"
#import <stdlib.h>




@interface PDFSignVerifyAppDelegate : NSObject <UIApplicationDelegate,DBRestClientDelegate> {
    
    UIWindow *window;
    UINavigationController *navigationController;

	NSString *docPath;
	NSString *certPath;
	
}

@property (nonatomic, retain) IBOutlet UIWindow *window;
@property (nonatomic, retain) IBOutlet UINavigationController *navigationController;

@property (nonatomic, retain) NSString *docPath;
@property (nonatomic, retain) NSString *certPath;

- (NSString *)getCertPath;
- (NSString *)getDocPath;

@end

