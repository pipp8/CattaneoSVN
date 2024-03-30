//
//  InitSeedViewController.m
//  iOTP
//
//  Created by Giuseppe Cattaneo on 08/02/15.
//  Copyright (c) 2015 Giuseppe Cattaneo. All rights reserved.
//
// $Id: InitSeedViewController.m 764 2015-10-21 09:34:11Z cattaneo $
//
//

#import "InitSeedViewController.h"

#import "iOTPMisc.h"

#import "TuitusOTP.h"

#import "MainViewController.h"



@interface InitSeedViewController ()

@property (weak, nonatomic) IBOutlet UILabel *lblKey;

@property (weak, nonatomic) IBOutlet UITextField *txtReceivedKey;

@property (weak, nonatomic) IBOutlet UITextView *txtDisplaySeed;

@property (weak, nonatomic) IBOutlet UIBarButtonItem *btSave;
@property (weak, nonatomic) IBOutlet UIBarButtonItem *btCancel;

@property NSString *seed;

@property UIAlertView *savePIN;

@end


@implementation InitSeedViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    // set view background color
    self.view.backgroundColor = GrayBackgroundColor;
    
    self.seed = nil;
    self.txtReceivedKey.keyboardAppearance = UIKeyboardTypeDefault;
    self.txtReceivedKey.delegate = self;
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)btSaveClicked:(id)sender {
    
    // switch to the default view
    // recupera il VC con la classe MainViewController per la gestione del Seed (Tab posizione 1) N.B. è un NavigationController
    UINavigationController * navigationVC  = (UINavigationController *)[self.tabBarController.viewControllers objectAtIndex: mainViewIndex];
    
    MainViewController * mainVC = [navigationVC.viewControllers objectAtIndex:0]; // prende il controller al top dello stack
    
    if ([self.seed length] > 0) {
        
        if ([mainVC isFingerPrintAvailable] == YES) {
  
            // [mainVC addSeedToSecureStore: self.seed];
            
            [mainVC saveSeed: self.seed withPIN: @""];
            
            // cambia view automaticamente
            self.tabBarController.selectedIndex = mainViewIndex; // il tabItem con la main page
            
        }
        else {
            self.savePIN = [[UIAlertView alloc]
                            initWithTitle:NSLocalizedString(@"SAVESEED-TITLE", nil)
                            message:NSLocalizedString(@"INSERTPIN-MSG", nil)
                            delegate:self cancelButtonTitle:NSLocalizedString(@"ABORT-BT", nil)
                            otherButtonTitles:NSLocalizedString(@"OK-BT", nil), nil];
            
            self.savePIN.alertViewStyle = UIAlertViewStyleSecureTextInput;
            
            [[self.savePIN textFieldAtIndex:0] setKeyboardType: UIKeyboardTypeNumberPad];
            [self.savePIN setTag:3];
            [self.savePIN show];
        }
    }
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
        UIAlertView* alertView = [[UIAlertView alloc] initWithTitle:@"Errore" message:@"La chiave deve essere di 8 caratteri"
                                                       delegate:self cancelButtonTitle:@"OK"
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

}

- (IBAction)txtKeyTouchOutside:(id)sender {
    NSLog(@"textfield touched outside");
}


- (IBAction)txtKeyValueChanged:(id)sender {
    NSLog(@"textfield Value Changed");
}


#pragma mark - Navigation


- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [textField resignFirstResponder];
    
    [self generateSeed];

    return YES;
}


- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex{
    
    if (alertView == self.savePIN && buttonIndex != 0) {
        NSString * pin = [[alertView textFieldAtIndex:0] text];
        NSLog(@"Entered: %@",[[alertView textFieldAtIndex:0] text]);
        
        // passa il seed alla default view
        // MainViewController * mainViewController = [self.storyboard instantiateViewControllerWithIdentifier:@"OTPMainViewController"];
        // non funziona perche' crea una nuova referenza e non quella alla view già caricata !!!

        // questo funziona ma dipende dall posizione della view nella listas
        UINavigationController * navigationVC  = (UINavigationController *)[self.tabBarController.viewControllers objectAtIndex: mainViewIndex];
        
        MainViewController * mainVC = [navigationVC.viewControllers objectAtIndex:0]; // prende il controller al top dello stack

        [mainVC saveSeed: self.seed withPIN: pin];
        
        // cambia view automaticamente
        self.tabBarController.selectedIndex = mainViewIndex; // il tabItem con la main page
        
    }
}




@end
