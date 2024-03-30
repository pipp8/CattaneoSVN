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

#import "AppSettings.h"

#import "iOTPMisc.h"


@interface RemoteSeedViewController ()

// Bottoni della GUI
@property (weak, nonatomic) IBOutlet UIButton *btStep1;

// TextField della GUI
@property (weak, nonatomic) IBOutlet UITextField *txtUsername;
@property (weak, nonatomic) IBOutlet UITextField *txtPassword;
@property (weak, nonatomic) IBOutlet UILabel *lblMessage1;

@property IBOutlet UIActivityIndicatorView *spinner;

@property (weak, nonatomic) IBOutlet UIBarButtonItem *btBack;

@end


@implementation RemoteSeedViewController


- (void) viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    // You code here to update the view.
}


- (void)viewDidLoad {
    [super viewDidLoad];

    // set view background color
    self.view.backgroundColor = GrayBackgroundColor;
    
//    //set bar color
//    [self.navigationController.navigationBar setBarTintColor: BlueColor];
//    //optional, i don't want my bar to be translucent
//    [self.navigationController.navigationBar setTranslucent:NO];
//    //set title color
//    [self.navigationController.navigationBar
//        setTitleTextAttributes:[NSDictionary dictionaryWithObject:[UIColor whiteColor]
//                                                           forKey:UITextAttributeTextColor]];
    
    UIImage *logo = [UIImage imageNamed:@"ID-BarIcon.png"];
    [self.navigationItem setTitleView: [[UIImageView alloc]
                                        initWithImage:[logo scaleImageToHeight: self.navigationController.navigationBar.frame.size.height]]];

//    //set back button color
//    [[UIBarButtonItem appearanceWhenContainedIn:[UINavigationBar class], nil]
//     setTitleTextAttributes:[NSDictionary dictionaryWithObjectsAndKeys:[UIColor whiteColor], UITextAttributeTextColor,nil]
//     forState:UIControlStateNormal];
//    [[UIBarButtonItem appearanceWhenContainedIn:[UINavigationBar class], nil]
//     setTitleTextAttributes:[NSDictionary dictionaryWithObjectsAndKeys:[UIColor grayColor], UITextAttributeTextColor,nil]
//     forState:UIControlStateDisabled];
//    
//    //set back button arrow color
//    [self.navigationController.navigationBar setTintColor:[UIColor whiteColor]];
    
    
    // abilita i textfield per la rimozione della tastiera
    self.txtUsername.delegate = self;
    self.txtUsername.keyboardAppearance = UIKeyboardTypeDefault;
    
    self.txtPassword.delegate = self;
    self.txtPassword.secureTextEntry =  YES;
    self.txtPassword.keyboardAppearance = UIKeyboardTypeDefault;

    [self.btStep1 setBackgroundColor: ButtonDisabledColor];
    [self.btStep1 setEnabled: NO];
    
    _lblMessage1.text = NSLocalizedString(@"InsertCredentials-LBL", nil);
    [self.view endEditing:YES];
    
    [self resetView];

    _spinner = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge];
    CGRect screenRect = [[UIScreen mainScreen] bounds];
    float kScreenWidth = screenRect.size.width / 2.0;
    float kScreenHeight = screenRect.size.height / 2.0;
    [_spinner setCenter:CGPointMake(kScreenWidth, kScreenHeight)];
    [self.view addSubview: _spinner]; // spinner is not visible until started
    
    self.remoteService = [[RemoteCommunication alloc] init];
    
 }



- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}



- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([[segue identifier] isEqualToString:@"step2SegueId"])
    {
        SeedRequestViewController *vc = [segue destinationViewController];
        [vc setRemoteService: self.remoteService];
        [vc setDelegate: self];
    }
}

// non sms restart
- (IBAction)unwindToRequest:(UIStoryboardSegue *)segue {
    NSLog(@"Unwinding from %@", [segue identifier]);
    // resta nella view automaticamente
}



- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex{
    
    if (buttonIndex == 0) {
        // abort ==> ritorna alla view principale
        // [self.tabBarController setSelectedIndex: mainViewIndex];
        [self performSegueWithIdentifier: @"UnwindToSettingSegueId" sender:self];
    }
    else {
        ; // non fa nulla e resta nella vista corrente
    }
}


- (IBAction)btBackClicked:(id)sender {
       [self dismissViewControllerAnimated:YES completion:nil]; 
}

- (IBAction)txtChanged:(id)sender {
    
    BOOL enable = (([[self.txtUsername text] length] > 0) &&
                               ([[self.txtPassword text] length] > 0));
    [self.btStep1 setEnabled: enable];
    self.btStep1.backgroundColor = (enable ? BlueColor : ButtonDisabledColor);
}




- (IBAction)txtValueChanged:(id)sender {

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
        [_spinner startAnimating];
        
        // disabilita ogni altra interazione
        //[[UIApplication sharedApplication] beginIgnoringInteractionEvents];
        
        Boolean debugGUI = [[[NSBundle mainBundle] objectForInfoDictionaryKey:@"DebugGUI"] boolValue];

        if (debugGUI) {
            [self.remoteService test1: self];
        }
        else {
            // N.B. Assolutamente asincrona
            [self.remoteService openSession: user withPassword: password target: self];
        }
    }
}



-(void) restartView: (NSString *) value result: (BOOL) code {

    if (code) {
    // tutto OK passa alla prossima vista (invio OTPSMS e richiesta seed)
//    SeedRequestViewController * vc = [self.storyboard instantiateViewControllerWithIdentifier:@"SeedRequestSBId"];
//    
//    // condivide il riferimento all'oggetto RemoteCommunication
//    [vc setRemoteService: self.remoteService];
//    [vc setSegue: self];
//    
//    [self presentViewController:vc animated:YES completion:nil];
        [AppSettings setUsername: self.txtUsername.text];
        self.txtPassword.text = @"";
        // self.txtUsername.text = @"";
        
        [self performSegueWithIdentifier: @"step2SegueId" sender: self];
    }
    else {
        // resta nella vista attuale (NON resetta i campi per non ridigitare i valori
        ;
    }
 }


-(UIActivityIndicatorView *) getSpinner {
    
    return self.spinner;
}



- (BOOL) checkAlreadyInited {
    
        //if initialized
        // chiede il pin per salvare il pin cifrato in una property
    UIAlertView *alert = [[UIAlertView alloc]
                          initWithTitle:NSLocalizedString(@"NOTIFY-TITLE", nil)
                          message:NSLocalizedString(@"ALREADYINITED-MSG", nil)
                          delegate:self cancelButtonTitle:NSLocalizedString(@"ABORT-BT", nil)
                          otherButtonTitles:NSLocalizedString(@"OK-BT", nil), nil];
    [alert show];
    return YES;
}



- (void) resetView {
    
    // resetta anche la GUI
    [self.txtUsername setText:@""];
    [self.txtPassword setText:@""];
    
    [self.btStep1 setEnabled: NO];
}


@end