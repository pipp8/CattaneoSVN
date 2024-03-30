//
//  ServicesViewController.h
//  MyPhotoVerify
//
//  Created by Antonio De Marco on 28/07/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MongooseWrapper.h"
#import "DropboxSDK.h"



@interface ServicesViewController : UITableViewController <DBLoginControllerDelegate> {
	NSArray *servicesList;
	MongooseWrapper *webServer;
	

}
@property (nonatomic,retain) NSArray *servicesList;
@property (nonatomic,retain) MongooseWrapper *webServer;
- (void)switchWebServer:(id)sender;
- (void)switchDropBoxService:(id)sender;
- (NSString *)imagePath;

@end
