//
//  PhotoTableViewController.h
//  MyPhotoVerify
//
//  Created by Antonio De Marco on 31/07/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "DropboxSDK.h"
#import <stdlib.h>
#import "PDFViewController.h"

@class DBRestClient;

@interface TableViewController : UITableViewController <DBRestClientDelegate> {
	DBRestClient* restClient;
	NSFileManager *fileManager;
	NSArray *directoryContent;
	NSTimer *timer;
	NSInteger fileCount;
	IBOutlet UILabel *lblProgress;
	NSString *DBDocPath;	
}

@property (nonatomic, readonly) DBRestClient* restClient;
@property (nonatomic, retain) NSFileManager *fileManager;
@property (nonatomic, retain) NSArray *directoryContent;
@property (nonatomic, retain) NSTimer *timer;
@property (nonatomic, retain) IBOutlet UILabel *lblProgress;
@property (nonatomic) NSInteger fileCount;
@property (nonatomic, retain) NSString *DBDocPath;


-(UIImage *)generatePhotoThumbnail:(UIImage *)image;
-(void)onTimer:(NSTimer *)timer;
-(void)setEnabledEditButton;

@end
