//
//  ImageViewController.h
//  MyPhotoVerify
//
//  Created by Antonio De Marco on 01/08/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PhotoInfoViewController.h"

@interface ImageViewController : UIViewController {
	IBOutlet UIImageView *image; 
	NSString *imageFile;
	 IBOutlet PhotoInfoViewController *photoInfoViewController;
	Boolean isView; 

}
@property (nonatomic,retain) IBOutlet UIImageView *image;
@property (nonatomic,retain) NSString *imageFile;
@property (nonatomic,retain) IBOutlet PhotoInfoViewController *photoInfoViewController;

-(IBAction) onBtnInfo: (id) sender;



@end
