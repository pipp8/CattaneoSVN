//
//  PhotoInfoViewController.h
//  MyPhotoVerify
//
//  Created by Antonio De Marco on 05/08/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "EXFGPS.h"

//#define SIGN_SIZE 3575
#define TSP_SIZE 3961

@interface PhotoInfoViewController : UIViewController {
  NSString *imageFile;
	IBOutlet UILabel *lblTimestamping;
	IBOutlet UILabel *lblTSASigner;
	IBOutlet UILabel *lblPhotoSigner;
	IBOutlet UILabel *lblSoftware;
	IBOutlet UILabel *lblArtist;
	IBOutlet UILabel *lblCpopyright;
	IBOutlet UILabel *lblData;
	IBOutlet UILabel *lblAltezza;
	IBOutlet UILabel *lblLarghezza;
	IBOutlet UILabel *lblProduttore;
	IBOutlet UILabel *lblGPSCoord;
	IBOutlet UIImageView *imageWarning;
	double longitude;
	double latitude;
	UINavigationController *navCon;

}

@property (nonatomic,retain) NSString *imageFile;
@property (nonatomic,retain) IBOutlet UILabel *lblTimestamping;
@property (nonatomic,retain) IBOutlet UILabel *lblTSASigner;
@property (nonatomic,retain) IBOutlet UILabel *lblSoftware;
@property (nonatomic,retain) IBOutlet UILabel *lblPhotoSigner;
@property (nonatomic,retain) IBOutlet UILabel *lblArtist;
@property (nonatomic,retain) IBOutlet UILabel *lblCpopyright;
@property (nonatomic,retain) IBOutlet UILabel *lblAltezza;
@property (nonatomic,retain) IBOutlet UILabel *lblLarghezza;
@property (nonatomic,retain) IBOutlet UILabel *lblData;
@property (nonatomic,retain) IBOutlet UILabel *lblProduttore;
@property (nonatomic,retain) IBOutlet UILabel *lblGPSCoord;
@property (nonatomic,retain) IBOutlet UIImageView *imageWarning;
@property (nonatomic,retain) UINavigationController *navCon;
@property (nonatomic) double longitude;
@property (nonatomic) double latitude;

-(void) verifyPhoto;
- (IBAction)showMap:(id) sender;





@end
