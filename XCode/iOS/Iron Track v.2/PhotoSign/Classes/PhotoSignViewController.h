//
//  MyPhotoSignViewController.h
//  MyPhotoSign
//
//  Created by Antonio De Marco on 23/07/10.
//  Copyright __MyCompanyName__ 2010. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <MessageUI/MessageUI.h>
#import <MessageUI/MFMailComposeViewController.h>

#import "SKPSMTPMessage.h"

#import "GPSOverlayViewController.h"
#import "EXFUtils.h"
#import "EXF.h"
//#import "MongooseWrapper.h"
#import "DropboxSDK.h"

//#define SIGN_SIZE 3575
#define TSP_SIZE 3961

@interface PhotoSignViewController : UIViewController <UINavigationControllerDelegate,UIImagePickerControllerDelegate,
			UITextFieldDelegate,DBLoginControllerDelegate,MFMailComposeViewControllerDelegate, SKPSMTPMessageDelegate> {
	IBOutlet GPSOverlayViewController *overlayView;
    IBOutlet UIImagePickerController *imagePickerCtrl;
	IBOutlet UIButton *btnCam;
	IBOutlet UISwitch *serverSwitch;
	IBOutlet UIActivityIndicatorView *activityIndicator;
	IBOutlet UILabel *lblProcess;
	IBOutlet UIProgressView *progressView;
	UITextField *passText;
}

@property (nonatomic, retain) IBOutlet UIImagePickerController *imagePickerCtrl;
@property (nonatomic, retain) IBOutlet GPSOverlayViewController *overlayView;
@property (nonatomic, retain) IBOutlet UIButton *btnCam;
@property (nonatomic, retain) IBOutlet UISwitch *serverSwitch;
@property (nonatomic, retain) IBOutlet UIActivityIndicatorView *activityIndicator;
@property (nonatomic, retain) IBOutlet UILabel *lblProcess;
@property (nonatomic, retain) IBOutlet UIProgressView *progressView;
@property (nonatomic, retain) UITextField *passText;



-(NSMutableArray*) createLocArray:(double) val;
-(void) populateGPS: (EXFGPSLoc*)gpsLoc :(NSArray*) locArray;
-(IBAction)onBtnCamTouch: (id)sender;
-(IBAction)onServerSwitchChange: (id)sender;
-(void)showAlertPassword;
-(Boolean) checkLoadKey;
-(void) showCam;
-(void) switchDropBoxService:(id)sender;
-(void) imageProcessing:(NSData *)imageData;
-(void) sendPECAttachment: (NSString *) filePath;
-(void) stopProcessing: (id)sender;
-(void) startProcessing: (id)sender;
-(void) changeLabelprogressTextOnMainThread: (NSString *)text;
-(void) changeProgressBarValueOnMainThread: (float)value;
-(void) changeProgressBarValue: (NSNumber *) number;
-(void) changeLabelprogressText: (NSString *)text;

@end

