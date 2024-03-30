//
//  SeedRequestViewController.m
//  iOTP
//
//  Created by Giuseppe Cattaneo on 21/04/15.
//  Copyright (c) 2015 Giuseppe Cattaneo. All rights reserved.
//

#import "SeedRequestViewController.h"

#import "RemoteCommunication.h"

#import "AppInitedViewController.h"

#import "MainViewController.h"

#import "iOTPMisc.h"


@interface SeedRequestViewController ()

// textField
@property (weak, nonatomic) IBOutlet UITextView *txtMessage2;
@property (weak, nonatomic) IBOutlet UITextField *txtSMSPIN;

// bottoni
@property (weak, nonatomic) IBOutlet UIButton *btStep2;
@property (weak, nonatomic) IBOutlet UIButton *btRestart;

// subview
@property UIAlertView *savePIN;

@end

@implementation SeedRequestViewController


- (void) viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    // You code here to update the view.
}


- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.

    // set view background color
    self.view.backgroundColor = GrayBackgroundColor;
    
    // abilita i textfield per la rimozione della tastiera
    self.txtSMSPIN.delegate = self;
    self.txtSMSPIN.keyboardAppearance = UIKeyboardTypePhonePad;
    
//     self.btSave.enabled = NO;
    
    [self.btStep2 setBackgroundColor: ButtonDisabledColor];
    
    [self.txtMessage2 setBackgroundColor: GrayBackgroundColor];
    
    [self.view endEditing:YES];
        
    [self resetView];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
    
    if ([[segue identifier] isEqualToString:@"step3"])
    {
        AppInitedViewController *vc = [segue destinationViewController];
        [vc setDelegate: self.delegate];
    }
}


- (BOOL) textFieldShouldReturn:(UITextField *)textField {
    
    [textField resignFirstResponder];
    
    return YES;
}



- (IBAction)txtSMSPIN_TouchOutside:(id)sender {

    NSLog(@"txtSMSOTP Touch Outside");
}

- (IBAction)txtSMSPIN_Changed:(id)sender {
    NSLog(@"txtSMSOTP Did End Editing");
    [self.btStep2 setEnabled: ([[self.txtSMSPIN text] length] == 8)];
    if (self.btStep2.enabled) {
        [self.btStep2 setBackgroundColor:BlueColor];
    }
    else {
        [self.btStep2 setBackgroundColor:ButtonDisabledColor];
    }
}


- (IBAction)txtSMSPIN_ValueChanged:(id)sender {
    
    NSLog(@"txtSMSOTP Value Changed");

    [self.btStep2 setEnabled: ([[self.txtSMSPIN text] length] == 8)];
}



- (IBAction)btRestart_Clicked:(id)sender {
    
    [self performSegueWithIdentifier:@"unwindToRequest" sender:self];
    
}



- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex{
    
    if (alertView == self.savePIN && buttonIndex != 0) {
        NSString * pin = [[alertView textFieldAtIndex:0] text];
        NSLog(@"Entered: %@",[[alertView textFieldAtIndex:0] text]);
        
        // passa il seed alla default view
        UINavigationController * navigationVC  = (UINavigationController *)[self.delegate.tabBarController.viewControllers objectAtIndex: mainViewIndex];
        
        MainViewController * mainVC = (MainViewController *) [navigationVC.viewControllers objectAtIndex:0];
        
        NSLog(@"Title: %@", mainVC.title);
        
        [mainVC saveSeed: self.remoteSeed withPIN: pin];
        
        self.remoteSeed = nil;
        
        // passa alla prossima vista
        //    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"MainStoryboard" bundle: nil];
//        AppInitedViewController * vc = [self.storyboard instantiateViewControllerWithIdentifier:@"AppReadyViewID"];
//        [vc setSegue: self.delegate];
//        
//        [self presentViewController:vc animated:YES completion:nil];
        
        [self performSegueWithIdentifier: @"step3" sender: self];
     }
}



- (IBAction)btStep2_Clicked:(id)sender {
    
    // check if remoteConnection is in the right state
    if (self.remoteService.progressStatus != OTPSMSRequested &&
        self.remoteService.progressStatus != WrongOTP) {
        NSLog(@"remoteService wrong state");
        return;
    }
    else {
        NSString * otp = [[self.txtSMSPIN text] stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]];
        
        if ([otp length] > 0) {
            // setta il cursore dell'App
            [[UIApplication sharedApplication] setNetworkActivityIndicatorVisible:YES];
            
            // disabilita ogni altra interazione
            //[[UIApplication sharedApplication] beginIgnoringInteractionEvents];
            
            // N.B. Assolutamente asincrona
            [self.remoteService sendSMS: otp target: self];
            // [self.remoteService test2: self];
        }
    }
}


-(void) restartView: (NSString *) value result: (BOOL) code {
    
    if (code == YES) {
        self.remoteSeed = value;
        
        if ([self.remoteSeed length] > 0) {
            
            // recupera il VC con la classe MainViewController per la gestione del Seed (Tab posizione 1) N.B. è un NavigationController
            UINavigationController * navigationVC  = (UINavigationController *)[self.delegate.tabBarController.viewControllers objectAtIndex: mainViewIndex];
            
            MainViewController * mainVC = [navigationVC.viewControllers objectAtIndex:0]; // prende il controller al top dello stack
            
            if ([self.remoteSeed length] > 0) {
                
                if ([mainVC isFingerPrintAvailable] == YES) {
                    // salva il seed nel secure store senza PIN
                    // [mainVC addSeedToSecureStore: self.remoteSeed];

                    [mainVC saveSeed: self.remoteSeed withPIN: @""];

                    self.remoteSeed = nil;
                    
                    // passa alla prossima vista
                    [self performSegueWithIdentifier: @"step3" sender: self];
                    
                }
                else {
                    // chiede il pin per salvare il pin cifrato in una property
                    self.savePIN = [[UIAlertView alloc]
                                    initWithTitle:NSLocalizedString(@"SAVESEED-TITLE", nil)
                                    message:NSLocalizedString(@"INSERTPIN-MSG", nil)
                                    delegate:self cancelButtonTitle:NSLocalizedString(@"ABORT-BT", nil)
                                    otherButtonTitles:NSLocalizedString(@"OK-BT", nil), nil];
                    self.savePIN .alertViewStyle = UIAlertViewStyleSecureTextInput;
                    
                    [self.savePIN  show];

                }
            }
        }
    }
    else {
        // resta nella stessa scena tipicamente l'errore e' PIN sbagliato
        // [self performSegueWithIdentifier:@"unwindToRequest" sender:self];
        // per chiedere un nuovo PIN basta premere NOSMS
    }
}



- (void) resetView {
    // resetta anche la GUI
    [self.txtSMSPIN setText:@""];
    
    [self.btStep2 setEnabled: NO];
    
//    [self.txtDisplaySeed setText: self.remoteSeed];
//    [self.txtDisplaySeed setFont:[UIFont fontWithName:@"Courier New" size:20]];

//    if ([self.remoteSeed length] > 100) {
//        [self.btSave setEnabled:YES];
//    }
    
}


@end
