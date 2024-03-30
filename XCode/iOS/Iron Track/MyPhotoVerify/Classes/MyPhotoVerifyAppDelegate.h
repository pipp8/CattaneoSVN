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




@interface MyPhotoVerifyAppDelegate : NSObject <UIApplicationDelegate,DBRestClientDelegate> {
    
    UIWindow *window;
    UINavigationController *navigationController;

	

}

@property (nonatomic, retain) IBOutlet UIWindow *window;
@property (nonatomic, retain) IBOutlet UINavigationController *navigationController;

- (NSString *)certPath;
- (NSString *)imagePath;

@end

