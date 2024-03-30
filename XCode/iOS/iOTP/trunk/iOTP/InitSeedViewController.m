//
//  InitSeedViewController.m
//  iOTP
//
//  Created by Giuseppe Cattaneo on 08/02/15.
//  Copyright (c) 2015 Giuseppe Cattaneo. All rights reserved.
//
// $Id: InitSeedViewController.m 823 2016-02-17 13:48:47Z cattaneo $
//
//

#import "InitSeedViewController.h"

#import "iOTPMisc.h"

#import "TuitusOTP.h"

#import "MainViewController.h"

#import "SecureStoreManagement.h"

#import "AppSettings.h"




@interface InitSeedViewController ()

@property (weak, nonatomic) IBOutlet UILabel *lblKey;

@property (weak, nonatomic) IBOutlet UITextField *txtReceivedKey;

@property (weak, nonatomic) IBOutlet UITextView *txtDisplaySeed;

@property (weak, nonatomic) IBOutlet UIBarButtonItem *btSave;

@property (weak, nonatomic) IBOutlet UIBarButtonItem *btCancel;


@property UIAlertView *savePIN;

@end


@implementation InitSeedViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    // set view background color
    self.view.backgroundColor = GrayBackgroundColor;

//    //set bar color
//    [self.navigationController.navigationBar setBarTintColor: BlueColor];
//    //optional, i don't want my bar to be translucent
//    [self.navigationController.navigationBar setTranslucent:NO];
//    //set title color
//    [self.navigationController.navigationBar setTitleTextAttributes:[NSDictionary dictionaryWithObject:[UIColor whiteColor] forKey:UITextAttributeTextColor]];
//    //set back button color
//    [[UIBarButtonItem appearanceWhenContainedIn:[UINavigationBar class], nil]
//        setTitleTextAttributes:[NSDictionary dictionaryWithObjectsAndKeys:[UIColor whiteColor], UITextAttributeTextColor,nil]
//        forState:UIControlStateNormal];
//    [[UIBarButtonItem appearanceWhenContainedIn:[UINavigationBar class], nil]
//        setTitleTextAttributes:[NSDictionary dictionaryWithObjectsAndKeys:[UIColor grayColor], UITextAttributeTextColor,nil]
//        forState:UIControlStateDisabled];
//
//    //set back button arrow color
//    [self.navigationController.navigationBar setTintColor:[UIColor whiteColor]];

    
    self.seed = nil;
    self.txtReceivedKey.keyboardAppearance = UIKeyboardTypeDefault;
    self.txtReceivedKey.delegate = self;
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


- (IBAction)btSaveClicked:(id)sender {

    self.seedGenerated = (self.seed.length > 0) ? YES : NO;
    
    [self saveSeed: self.seed];
    
    [self.btSave setEnabled:NO];    // disabilita il tasto save per le prossime generazioni
}


- (IBAction)btCancelClicked:(id)sender {
}


- (IBAction)txtKeyEditingBegin:(id)sender {
    NSLog(@"textfield did begin");
}


- (IBAction)txtKeyEndOnExit:(id)sender {

    NSLog(@"textfield did end");
    [self generateSeed];
}


- (void) generateSeed {
    
    NSString * smsKey = [self.txtReceivedKey text];
    
    if ([smsKey length] < 8) {
        UIAlertView* alertView = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"ERROR-TITLE", nil)
                                                    message:NSLocalizedString(@"KEYTOOSHORT-ERROR", nil)
                                                    delegate:self cancelButtonTitle: NSLocalizedString(@"OK-BT", nil)
                                                    otherButtonTitles: nil];

        [alertView show];
        return;
    }
    
    // Get the HEX in a Byte[]
    // NSData * msg = [TuitusOTP hexStr2Bytes: smsKey];
    NSData* msg =[smsKey dataUsingEncoding:NSUTF8StringEncoding];
    
    // NSData * k = [TuitusOTP hexStr2Bytes: AppSeed];
    // [TuitusOTP dumpData: k];
    
    NSData * binSeed = [TuitusOTP hMacSHA: kCCHmacAlgSHA512 key: msg data: msg];
    
    self.seed = [TuitusOTP bytes2HexStr: binSeed];
    
    [self.txtDisplaySeed setText: self.seed];
    
    [self.txtDisplaySeed setFont:[UIFont fontWithName:@"Courier New" size:22]];
//     [self.txtDisplaySeed setFont:[UIFont systemFontOfSize:24]];
    
    [self.btSave setEnabled:YES];

}

- (IBAction)txtKeyTouchOutside:(id)sender {
    NSLog(@"textfield touched outside");
}


- (IBAction)txtKeyValueChanged:(id)sender {
    NSLog(@"textfield Value Changed");
}


- (void) saveSeed: (NSString *) hexSeed {
    
    
    // supponiamo che la rootwindows sia proprio una istanza della mainViewController
    MainViewController *mvc = [self.navigationController.viewControllers objectAtIndex:0];
    
    if (![mvc isMemberOfClass:[MainViewController class]]) {
        NSLog(@"Wrong navigation stack Seed not saved");
        return;
    }
    
    NSString *className = NSStringFromClass([mvc class]);
    NSLog(@" mvc from class:%@", className);
    
    if ([hexSeed length] > 0) {
        
        [mvc saveSeed: hexSeed usingSecureStore: [AppSettings getUseSecureStore]];
    }
    else {
        NSLog(@"Errore nella generazione del seed: Wrong Seed");
    }
}





#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
        
    NSLog(@"Preparing segue from: %@", [segue identifier]);
    
    if (sender == self.btCancel) {

        self.seedGenerated = NO;
        self.seed = @"";
    }
    [self.btSave setEnabled:NO];    // disabilita il tasto save per le prossime generazioni
}


- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [textField resignFirstResponder];
    
    [self generateSeed];

    return YES;
}


@end
