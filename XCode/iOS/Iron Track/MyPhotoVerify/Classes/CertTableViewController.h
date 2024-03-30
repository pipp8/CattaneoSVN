//
//  CertTableViewController.h
//  MyPhotoVerify
//
//  Created by Antonio De Marco on 01/08/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "DropboxSDK.h"
#import <stdlib.h>

@class DBRestClient;

@interface CertTableViewController : UITableViewController <DBRestClientDelegate> {
	DBRestClient* restClient;
	NSFileManager *fileManager;
	NSMutableArray *directoryContent;
	NSTimer *timer;
	NSInteger fileCount;
	
}
@property (nonatomic, readonly) DBRestClient* restClient;
@property (nonatomic, retain) NSFileManager *fileManager;
@property (nonatomic, retain) NSMutableArray *directoryContent;
@property (nonatomic, retain) NSTimer *timer;

@property (nonatomic) NSInteger fileCount;
-(void)onTimer:(NSTimer *)timer;
-(void)setEnabledEditButton;

@end