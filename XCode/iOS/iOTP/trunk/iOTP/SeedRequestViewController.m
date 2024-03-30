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

#import "AppSettings.h"

#import "iOTPMisc.h"

#import "SecureStoreManagement.h"

#import "SettingsListTableViewController.h"




#define wrongSeedAlrt     1


@interface SeedRequestViewController ()

// textField
@property (weak, nonatomic) IBOutlet UILabel *lblMessagePIN;
@property (weak, nonatomic) IBOutlet UITextField *txtSMSPIN;
@property (weak, nonatomic) IBOutlet UILabel *lblPrefix;

// bottoni
@property (weak, nonatomic) IBOutlet UIButton *btStep2;
@property (weak, nonatomic) IBOutlet UIButton *btRestart;

@property IBOutlet UIActivityIndicatorView *spinner;

@end

@implementation SeedRequestViewController


- (void) viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    // You code here to update the view.
}


- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.

    // set navigation bar icon
    UIImage *logo = [UIImage imageNamed:@"ID-BarIcon.png"];
    [self.navigationItem setTitleView: [[UIImageView alloc]
                                        initWithImage:[logo scaleImageToHeight: self.navigationController.navigationBar.frame.size.height]]];
    
    // rimuove il bottone per la navigazione
    self.navigationItem.leftBarButtonItem = nil;
    self.navigationItem.hidesBackButton = YES;
    
    // set view background color
    self.view.backgroundColor = GrayBackgroundColor;
    
    // abilita i textfield per la rimozione della tastiera
    self.txtSMSPIN.delegate = self;
    self.txtSMSPIN.keyboardAppearance = UIKeyboardAppearanceDefault;
    self.txtSMSPIN.keyboardType = UIKeyboardTypeDecimalPad;
    
//     self.btSave.enabled = NO;
    
    [self.btStep2 setBackgroundColor: ButtonDisabledColor];
    
    _lblMessagePIN.text = NSLocalizedString(@"InsertCodiceVerifica-LBL", nil);
    
    [self.lblPrefix setText: [NSString stringWithFormat:@"%@ :", [self.remoteService OTPPrefix]]];
    
    [self.view endEditing:YES];
    
    _spinner = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge];
    CGRect screenRect = [[UIScreen mainScreen] bounds];
    float kScreenWidth = screenRect.size.width / 2.0;
    float kScreenHeight = screenRect.size.height / 2.0;
    [_spinner setCenter:CGPointMake(kScreenWidth, kScreenHeight)];
    [self.view addSubview: _spinner]; // spinner is not visible until started

    [self resetView];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex {
    
    if (alertView.tag == wrongSeedAlrt) {
        // torna alla prima view (Username e password)
        [self performSegueWithIdentifier:@"unwindToRequest" sender:self];
        // [self dismissViewControllerAnimated: YES completion:nil];
    }
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
    NSLog(@"Preparing segue from: %@", [segue identifier]);
}



- (BOOL) textFieldShouldReturn:(UITextField *)textField {
    
    [textField resignFirstResponder];
    
    return YES;
}



- (IBAction)txtSMSPIN_TouchOutside:(id)sender {

    NSLog(@"txtSMSOTP Touch Outside");
}

- (IBAction)txtSMSPIN_Changed:(id)sender {
    [self.btStep2 setEnabled: ([[self.txtSMSPIN text] length] == 8)];
    if (self.btStep2.enabled) {
        NSLog(@"txtSMSOTP_Changed 8 caratteri");
        [self.btStep2 setBackgroundColor:BlueColor];
        [self.view endEditing:YES]; // dismiss keyboard
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
    // [self dismissViewControllerAnimated: YES completion:nil];
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
            [_spinner startAnimating];
            
            // disabilita ogni altra interazione
            //[[UIApplication sharedApplication] beginIgnoringInteractionEvents];
            
            Boolean debugGUI = [[[NSBundle mainBundle] objectForInfoDictionaryKey:@"DebugGUI"] boolValue];
            
            if (debugGUI) {
                [self.remoteService test2: self];
            }
            else {
                // N.B. Assolutamente asincrona
                [self.remoteService sendSMS: otp target: self];
            }
        }
    }
}


-(void) restartView: (NSString *) value result: (BOOL) code {
    
    if ((code == NO) && (([self.remoteService progressStatus] == OTPVerifyRequest) ||
                        ([self.remoteService progressStatus] == WrongOTP))) {

        UIAlertView * alert = [[UIAlertView alloc]
                               initWithTitle:NSLocalizedString(@"Warning", nil)
                               message:NSLocalizedString(@"WRONGSMSOTP-ERROR", nil)
                               delegate:self cancelButtonTitle:NSLocalizedString(@"OK-BT", nil)
                               otherButtonTitles:nil];
        
        [alert setTag: wrongSeedAlrt];
        [alert show];
        
    }
    else if ((code == NO) || ([value length] <= 10)) {

        UIAlertView * alert = [[UIAlertView alloc]
                               initWithTitle:NSLocalizedString(@"Warning", nil)
                               message:NSLocalizedString(@"WRONGSEED-ERROR", nil)
                               delegate:self cancelButtonTitle:NSLocalizedString(@"OK-BT", nil)
                               otherButtonTitles:nil];

        [alert setTag: wrongSeedAlrt];
        [alert show];
    }
    else {
        // tutto OK salva il seed
        self.remoteSeed = value;
        // salva il seed eventualmente chiedendo il pin
        [self saveSeed: self.remoteSeed];
        // resta in questa view
    }
}

-(UIActivityIndicatorView *) getSpinner {
    
    return self.spinner;
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

- (void) saveSeed: (NSString *) hexSeed {
    
//    NSInteger numberOfViewControllers = self.navigationController.viewControllers.count;
//    
//    NSLog(@"number of controller pushed: %ld", (long)numberOfViewControllers);
    
    // supponiamo che la rootwindows sia proprio una istanza della mainViewController
    MainViewController *mvc = [self.navigationController.viewControllers objectAtIndex:0];
    
    NSString *className = NSStringFromClass([mvc class]);
    NSLog(@"mvc from class:%@", className);

    if (![mvc isMemberOfClass:[MainViewController class]]) {
        if ([mvc isMemberOfClass:[SettingsListTableViewController class]]) {
            mvc = ((SettingsListTableViewController *) mvc).mainVC;
        }
        else {
            NSLog(@"Wrong navigation stack Seed not saved");
            return;
        }
    }
    
    
    if ([hexSeed length] > 0) {

        [mvc saveSeed: hexSeed usingSecureStore: [AppSettings getUseSecureStore]];
        // riparte dal messaggio di attivazione
        [self performSegueWithIdentifier:@"RemoteSeedOKSegueId" sender:self];
    }
    else {
        // che succede in questo alla GUI ???
        NSLog(@"Errore nella generazione del seed: Wrong Seed");
    }
    
}



@end
