//
//  RootViewController.h
//  MyPhotoVerify
//
//  Created by Antonio De Marco on 28/07/10.
//  Copyright __MyCompanyName__ 2010. All rights reserved.
//

#import <UIKit/UIKit.h>

@class DBRestClient;

@interface MenuViewController : UITableViewController {
	NSArray *menuContent;

}
@property (nonatomic,retain) NSArray *menuContent;

@end
