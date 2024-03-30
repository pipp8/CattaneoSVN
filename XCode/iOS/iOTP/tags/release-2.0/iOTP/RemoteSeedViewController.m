//
//  RemoteSeedViewController.m
//  iOTP
//
//  Created by Giuseppe Cattaneo on 23/02/15.
//  Copyright (c) 2015 Giuseppe Cattaneo. All rights reserved.
//

#import "RemoteSeedViewController.h"

#import "SeedRequestViewController.h"

#import "MainViewController.h"

#import "iOTPMisc.h"


@interface RemoteSeedViewController ()

// Bottoni della GUI
@property (weak, nonatomic) IBOutlet UIButton *btStep1;

// TextField della GUI
@property (weak, nonatomic) IBOutlet UITextField *txtUsername;
@property (weak, nonatomic) IBOutlet UITextField *txtPassword;
@property (weak, nonatomic) IBOutlet UITextView *txtMessage1;


@end


@implementation RemoteSeedViewController


- (void) viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    // You code here to update the view.
    
    [self checkAlreadyInited];
}


- (void)viewDidLoad {
    [super viewDidLoad];

    // set view background color
    self.view.backgroundColor = GrayBackgroundColor;
    
    // abilita i textfield per la rimozione della tastiera
    self.txtUsername.delegate = self;
    self.txtUsername.keyboardAppearance = UIKeyboardTypeDefault;
    
    self.txtPassword.delegate = self;
    self.txtPassword.keyboardAppearance = UIKeyboardTypeDefault;

    [self.btStep1 setBackgroundColor: BlueColor];
    
    [self.txtMessage1 setBackgroundColor: GrayBackgroundColor];
    
    [self.view endEditing:YES];
    
    [self resetView];

    self.remoteService = [[RemoteCommunication alloc] init];
    
 }



- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}



- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([[segue identifier] isEqualToString:@"step2"])
    {
        SeedRequestViewController *vc = [segue destinationViewController];
        [vc setRemoteService: self.remoteService];
        [vc setDelegate: self];
    }
}

// unused
- (IBAction)unwindToRequest:(UIStoryboardSegue *)segue {
    NSLog(@"Unwinding from %@", [segue identifier]);
    // resta nella view automaticamente
}



- (IBAction)unwindToRestart:(UIStoryboardSegue *)segue {
    NSLog(@"Unwinding from %@", [segue identifier]);
    // cambia view automaticamente
    self.tabBarController.selectedIndex = mainViewIndex; // il tabItem con la main page

}


- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex{
    
    if (buttonIndex == 0) {
        // abort ==> ritorna alla view principale
        [self.tabBarController setSelectedIndex: mainViewIndex];        
    }
    else {
        ; // non fa nulla e resta nella vista corrente
    }
}



- (IBAction)txtUsername_Changed:(id)sender {
    
    [self.btStep1 setEnabled: (([[self.txtUsername text] length] > 0) &&
                               ([[self.txtPassword text] length] > 0))];
}

- (IBAction)txtUsername_ValueChanged:(id)sender {

    [self.btStep1 setEnabled: (([[self.txtUsername text] length] > 0) &&
                               ([[self.txtPassword text] length] > 0))];
}


- (IBAction)txtPassword_Changed:(id)sender {

    [self.btStep1 setEnabled: (([[self.txtUsername text] length] > 0) &&
                               ([[self.txtPassword text] length] > 0))];
}

- (IBAction)txtPassword_ValueChanged:(id)sender {
    [self.btStep1 setEnabled: (([[self.txtUsername text] length] > 0) &&
                               ([[self.txtPassword text] length] > 0))];
}




- (BOOL) textFieldShouldReturn:(UITextField *)textField {

    [textField resignFirstResponder];
    
    return YES;
}




// send the level 1 login information and ask for a SMS OTP
- (IBAction)btStep1_Clicked:(id)sender {
    
    NSString * user = [[self.txtUsername text] stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]];
    NSString * password = [[self.txtPassword text] stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]];
    
    if ([user length] <= 0 || [password length] <= 0) {
        // gestione dell'errore
        return;
    }
    else {
        // setta il cursore dell'App
        [[UIApplication sharedApplication] setNetworkActivityIndicatorVisible:YES];
        
        // disabilita ogni altra interazione
        //[[UIApplication sharedApplication] beginIgnoringInteractionEvents];
        
        // N.B. Assolutamente asincrona
        [self.remoteService openSession: user withPassword: password target: self];
        // [self.remoteService test1: self];
    }
}



-(void) restartView: (NSString *) value result: (BOOL) code {

    if (code) {
    // tutto OK passa alla prossima vista (invio OTPSMS e richiesta seed)
//    SeedRequestViewController * vc = [self.storyboard instantiateViewControllerWithIdentifier:@"SeedRequestViewID"];
//    
//    // condivide il riferimento all'oggetto RemoteCommunication
//    [vc setRemoteService: self.remoteService];
//    [vc setSegue: self];
//    
//    [self presentViewController:vc animated:YES completion:nil];
        self.txtPassword.text = @"";
        // self.txtUsername.text = @"";
        
        [self performSegueWithIdentifier: @"step2" sender: self];
    }
    else {
        // resta nella vista attuale (NON resetta i campi per non ridigitare i valori
        ;
    }
 }


- (BOOL) checkAlreadyInited {
    
    UINavigationController * navigationVC  = (UINavigationController *)[self.tabBarController.viewControllers objectAtIndex: mainViewIndex];

    MainViewController * mainVC = (MainViewController *) [navigationVC.viewControllers objectAtIndex:0];    // prende il primo controller della navigationView

    if ([mainVC isAppActivable]) {
        //if initialized
        // chiede il pin per salvare il pin cifrato in una property
        UIAlertView *alert = [[UIAlertView alloc]
                              initWithTitle:NSLocalizedString(@"NOTIFY-TITLE", nil)
                              message:NSLocalizedString(@"ALREADYINITED-MSG", nil)
                              delegate:self cancelButtonTitle:NSLocalizedString(@"ABORT-BT", nil)
                              otherButtonTitles:NSLocalizedString(@"OK-BT", nil), nil];
        [alert show];
        
        return NO;
    }
    else {
        return YES;
    }
}



- (void) resetView {
    // resetta anche la GUI
    [self.txtUsername setText:@""];
    [self.txtPassword setText:@""];
    
    [self.btStep1 setEnabled: NO];
    
//    [self.txtDisplaySeed setText: self.remoteSeed];
//    [self.txtDisplaySeed setFont:[UIFont fontWithName:@"Courier New" size:20]];
    
//    if ([self.remoteSeed length] > 100) {
//        [self.btSave setEnabled:YES];
//    }

}
@end