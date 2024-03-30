//
//  ViewController.m
//  iOTP
//
//  Created by Giuseppe Cattaneo on 01/02/15.
//  Copyright (c) 2015 Giuseppe Cattaneo. All rights reserved.
//
//
// $Id: MainViewController.m 843 2016-04-03 19:46:09Z cattaneo $
//

#import "MainViewController.h"

#import "ShowOTPViewController.h"

#import "iOTPMisc.h"

#import "SecureStoreManagement.h"

#import "InitSeedViewController.h"

#import "SeedRequestViewController.h"

#import "SettingsListTableViewController.h"

#import "AppSettings.h"

#import "SOLOptionsTransitionAnimator.h"

#import "SOLSlideTransitionAnimator.h"



#define AlertWrongPIN       1
#define AlertLoadPIN        2
#define AlertSavePIN        3
#define AlertPINDestroyed   4

@interface MainViewController ()

@property ShowOTPViewController *vc ;

@property (weak, nonatomic) IBOutlet UILabel *lblInsertPIN;

@property (weak, nonatomic) IBOutlet UITextField *txtPIN;

@property (weak, nonatomic) IBOutlet UIButton *btGenerate;

@property (weak, nonatomic) IBOutlet UIViewController * welcomeVC;

@property UIAlertView *alertPIN;

@property NSInteger OTPLength;

@property CCHmacAlgorithm hMacAlg;

@property NSString *_seed;

@property NSString * _passcode;

@property BOOL _displayOTP;

@end


// Segue Ids
static NSString * const kSegueShowOTPPush    = @"showOTP";
static NSString * const kSegueRequestSeedPush= @"RequestSeedSegueId";
static NSString * const kSegueOptionsPush    = @"SettingSegueId";
static NSString * const kSegueOptionsModal   = @"SettingModalSegueId";
static NSString * const kSegueOptionsDismiss = @"SettingDismissSegueId";


@interface MainViewController () <UIViewControllerTransitioningDelegate, UINavigationControllerDelegate>
@end



@implementation MainViewController


- (void) viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    // IF NOT INITED
    if (![AppSettings getSeedAvailable]) {
        
        [self performSegueWithIdentifier: kSegueRequestSeedPush sender: self];
    }
    else {
        // load/check seed
        [self enableApp: [self isAppActivable]];
    }

}


- (void)viewDidLoad {
    [super viewDidLoad];
    
    // [AppSettings setSeedInited: NO];

    // set view background color
    self.view.backgroundColor = GrayBackgroundColor;
    // Initialize the toolbar sopra
    self.navigationController.toolbarHidden = YES;
    // self.navigationController.navigationBar.tintColor = BlueColor;
    // self.navigationController.toolbar.tintColor = BlueColor;

    //set bar color
    [self.navigationController.navigationBar setBarTintColor: BlueColor];
    //optional, i don't want my bar to be translucent
    [self.navigationController.navigationBar setTranslucent:NO];
    //set title and title color
    // [self.navigationItem setTitle:@"Title"];
  
    UIImage *logo = [UIImage imageNamed:@"ID-BarIcon.png"];
    [self.navigationItem setTitleView: [[UIImageView alloc]
                                        initWithImage:[logo scaleImageToHeight: self.navigationController.navigationBar.frame.size.height]]];
    
    [self.navigationController.navigationBar setTitleTextAttributes:[NSDictionary dictionaryWithObject:[UIColor whiteColor]
                                                forKey: UITextAttributeTextColor]];
    
    //set back button color
    [[UIBarButtonItem appearanceWhenContainedIn:[UINavigationBar class], nil] setTitleTextAttributes:[NSDictionary dictionaryWithObjectsAndKeys:[UIColor whiteColor], UITextAttributeTextColor,nil] forState:UIControlStateNormal];
    //set back button arrow color
    [self.navigationController.navigationBar setTintColor:[UIColor whiteColor]];
    
    [self.navigationItem setBackBarButtonItem:[[UIBarButtonItem alloc] initWithTitle:@" " style:UIBarButtonItemStylePlain target:nil action:nil]];


    // abilita i textfield per la rimozione della tastiera
    self.txtPIN.delegate = self;
    self.txtPIN.keyboardAppearance = UIKeyboardTypeNumberPad;
    self.txtPIN.secureTextEntry = YES;
    self.txtPIN.hidden = YES;
    
    self.lblInsertPIN.text = NSLocalizedString( @"GENERATEOTP-LBL", nil);
    
}



- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}




- (BOOL) textFieldShouldReturn:(UITextField *)textField {
    
    [textField resignFirstResponder];
    
    return YES;
}


/*
 Prepare for segue
 
 navigationController.delegate:
 We need to set the UINavigationControllerDelegate everytime for push transitions.
 This is necessary because this VC presents multiple VCs, some with custom transitions
 (Options, Slide, Bounce, Fold, Drop) and one with a standard transition (Flow 1).
 The delegate is set to self for the custom transitions so that they work with
 the navigation controller. The delegate is set to nil for the standard transition
 so that the default interactive pop transition works.
 
 modalPresentationStyle:
 Specify UIModalPresentationCustom for transitions where the source VC should
 stay in the view hierarchy after the transition is complete (Options, Drop).
 For the other cases (Slide, Bounce, Fold) we don't set it which defaults it
 to UIModalPresentationFullScreen.
 
 */
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    
    // Options - modal
    if ([segue.identifier isEqualToString:kSegueOptionsModal]) {
        UIViewController *toVC = segue.destinationViewController;
        toVC.modalPresentationStyle = UIModalPresentationCustom;
        toVC.transitioningDelegate = self;

        SettingsListTableViewController * SettingsVC = (SettingsListTableViewController *) ((UINavigationController *)toVC).topViewController;
        [SettingsVC setMainVC: self];
    }
    // Options - push
    else if ([segue.identifier isEqualToString:kSegueOptionsPush]) {
        self.navigationController.delegate = self;
        SettingsListTableViewController *toVC = segue.destinationViewController;
        toVC.transitioningDelegate = self;

        [toVC setMainVC: self];
    }
    // Slide - push
    else if ([segue.identifier isEqualToString:kSegueRequestSeedPush]) {
        self.navigationController.delegate = self;
        UIViewController *toVC = segue.destinationViewController;
        toVC.transitioningDelegate = self;
        
        _welcomeVC = segue.destinationViewController;
    }
    // Slide - push
    else if ([segue.identifier isEqualToString:kSegueShowOTPPush]) {
        self.navigationController.delegate = self;
        UIViewController *toVC = segue.destinationViewController;
        toVC.transitioningDelegate = self;
        
        // passa l'OTP da mostrare
        self.vc = (ShowOTPViewController *) toVC;
        [self.vc setOTP: self.otp];
    }
    // Slide - modal
//    else if ([segue.identifier isEqualToString:kSegueSlideModal]) {
//        UIViewController *toVC = segue.destinationViewController;
//        toVC.transitioningDelegate = self;
//    }
    
    [super prepareForSegue:segue sender:sender];
}



#pragma mark - Storyboard unwinding

/*
 Unwind segue action called to dismiss the Options and Drop view controllers and
 when the Slide, Bounce and Fold view controllers are dismissed with a single tap.
 
 Normally an unwind segue will pop/dismiss the view controller but this doesn't happen
 for custom modal transitions so we have to manually call dismiss.
 */
- (IBAction)unwindToMainVC:(UIStoryboardSegue *)sender
{
    if ([sender.identifier isEqualToString:kSegueOptionsDismiss]) {
        [self dismissViewControllerAnimated:YES completion:nil];
    }
}



- (IBAction)unwindToNewOTP:(UIStoryboardSegue *)segue {
    
    NSLog(@"Unwinding (unwindToNewOTP) from %@", [segue identifier]);
}


- (IBAction)unwindFromSetting:(UIStoryboardSegue *)segue {
    
    NSLog(@"Unwinding (unwindFromSetting) from %@", [segue identifier]);
    if (_welcomeVC != nil) {
        [_welcomeVC dismissViewControllerAnimated:NO completion:nil];
        _welcomeVC = nil;
    }
}





// restituisce vero o falso a secondo che sia che il seed sia stato registrato o meno
- (Boolean)isAppActivable {
    
//    [AppSettings setOTPLength: 8];
//    [AppSettings setHMACLength: 512];
    Boolean activated = [AppSettings getSeedAvailable];
    
    if (activated) {

        self.OTPLength = [AppSettings getOTPLength];
                
        NSInteger hashLength = [AppSettings getHMACLength];
        
        switch (hashLength) {
                
            case 128: self.hMacAlg = kCCHmacAlgSHA1; break;

            case 256: self.hMacAlg = kCCHmacAlgSHA256; break;

            case 512: self.hMacAlg = kCCHmacAlgSHA512; break;

            default: self.hMacAlg = kCCHmacAlgSHA512; break;
        }
    }

    return activated;
}


- (IBAction)btGenerateClicked:(id)sender {
    
    [self loadSeed: YES];

}



- (void) alertView:(UIAlertView *)alertView didDismissWithButtonIndex: (NSInteger)buttonIndex {
//    if (alertView == self.loadPIN && buttonIndex != 0) {
//        NSString * pin = [[alertView textFieldAtIndex:0] text];
//        NSLog(@"Entered: %@",[[alertView textFieldAtIndex:0] text]); // eliminare
//        [self loadSeedWithPIN: pin];
//    }
    if (alertView.tag == AlertPINDestroyed) {

        // riparte con la generazione di un nuovo OTP
        [self performSegueWithIdentifier: kSegueRequestSeedPush sender: self];
    }
    
}


- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex{
    
    if (alertView.tag == AlertWrongPIN) {
        if (buttonIndex == 1) {
            // [self loadSeed];    // riprova ripremendo il bottone
        }
    }
}



-(void) loadSeed: (BOOL) displayOTP {
    
    if ([AppSettings getSeedAvailable] == YES) {
        
        self._displayOTP = displayOTP;
        
        if ([AppSettings getUseSecureStore] == YES) {
            
            // se disponibile cerca il seed nel secure store
            self._seed = [SecureStoreManagement getSeedFromSecureStore];
            
            if (self._seed.length > 0) {
                
                if (self._displayOTP) {
                
                    [self generateOTP];
                }
                else {

                    [self saveSeed: self._seed usingSecureStore:NO]; // da adesso usa lo standard store
                    [SecureStoreManagement deleteSeedFromSecureStore]; // viene rimosso dal secure store (prima di salvarlo)
                    self._seed = nil; // cancella il seed caricato solo per il cambio store
                }
            }
            else {
                NSLog(@"Wrong Seed from Secure Store: %@", self._seed);
            }
        }
        else {
            // Se il seed esiste lo carica usando il pin inserito nel textbox
            // NSString * pin = self.txtPIN.text;
            
            PAPasscodeViewController *passcodeViewController = [[PAPasscodeViewController alloc] initForAction:PasscodeActionEnter];
            if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone) {
                passcodeViewController.backgroundView = [[UITableView alloc] initWithFrame:[UIScreen mainScreen].bounds style:UITableViewStyleGrouped];
            }
            passcodeViewController.delegate = self;
            passcodeViewController.passcode = @"";
            passcodeViewController.alternativePasscode = nil;
            passcodeViewController.simple = YES;
            [self presentViewController:[[UINavigationController alloc] initWithRootViewController:passcodeViewController] animated:YES completion:nil];
        }
    }
    else {
        UIAlertView * alertView = [[UIAlertView alloc]
                                   initWithTitle:NSLocalizedString(@"NOTIFY-TITLE", nil)
                                   message:NSLocalizedString(@"NOSEED-MSG", nil)
                                   delegate:self
                                   cancelButtonTitle:NSLocalizedString(@"OK-BT", nil)
                                   otherButtonTitles:nil];
        [alertView show];
    }
}


-(NSString *) decryptSeed: (NSString*) password {

    NSError * error = nil;
    
    NSData *iv = [AppSettings getInitVector];
    
    NSData *salt = [AppSettings getSalt];
    
    NSData *encData = [AppSettings getEncryptedSeed];
    
    NSData * plainData = [TuitusOTP decryptData: encData password: password
                                             iv: iv salt: salt error: &error];
 
    if (error != nil) {
        self.alertPIN = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"NOTIFY-TITLE", nil)
                                                   message:[error localizedDescription]
                                                  delegate:self
                                         cancelButtonTitle:NSLocalizedString(@"OK-BT", nil)
                                         otherButtonTitles:nil];
        self.alertPIN.tag = AlertWrongPIN;
        [self.alertPIN show];
    }
    NSString * plainText =  [[NSString alloc] initWithData:plainData encoding:NSUTF8StringEncoding];
    
    return plainText;
}



- (void) loadSeedWithPIN: (NSString *) password {
    
    NSString * plainText = [self decryptSeed: password];
    
    NSString * magic = [plainText substringToIndex: 4];
    
    if (![magic isEqualToString: magicNumber]) {
        self.alertPIN = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"NOTIFY-TITLE", nil)
                                                   message:NSLocalizedString(@"WRONGPIN-MSG", nil)
                                                  delegate:self
                                         cancelButtonTitle:NSLocalizedString(@"OK-BT", nil)
                                         otherButtonTitles:nil];
        self.alertPIN.tag = AlertWrongPIN;
        [self.alertPIN show];
    }
    else {
        self._seed = [plainText substringFromIndex: 4];

        NSLog(@"Seed Loaded: %@", self._seed);
        
        // [self.txtPIN setText: @""]; // azzera il vecchio PIN
        if (self._displayOTP) {
            // genera l'OTP e provvede alla sua visualizzazione
            [self generateOTP]; // cancella il seed dopo la generazione
        }
        else {
            // stiamo salvando un seed precedentemente contenuto nello store normale
            [self saveSeed: self._seed withPIN: @"" usingSecureStore:YES];
            self._seed = nil;   // cancella il seed caricato solo per il cambio store
        }
    }

//    UIAlertView* alertView = [[UIAlertView alloc] initWithTitle:@"Notifica" message:@"Caricamento del seed terminato con successo"
//                                                       delegate:self cancelButtonTitle:@"OK" otherButtonTitles:nil];
//    [alertView show];
}



- (void) saveSeed: (NSString *) hexSeed usingSecureStore: (BOOL) useSecureStore {

    if ([hexSeed length] > 0) {
        
        self._seed = hexSeed;
        
        if (useSecureStore) {
            
            [self saveSeed: self._seed withPIN: @"" usingSecureStore: YES];
            // il seed viene cancellato dopo il salvataggio e l'app viene attivata
            
        }
        else {
                        
            // usa la view per prendere il PIN (N.B. il pin non viene salvato e non e' disponibile !!!
            PAPasscodeViewController *passcodeViewController = [[PAPasscodeViewController alloc] initForAction:PasscodeActionSet];
            if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone) {
                passcodeViewController.backgroundView = [[UITableView alloc] initWithFrame:[UIScreen mainScreen].bounds style:UITableViewStyleGrouped];
            }
            passcodeViewController.delegate = self;
            passcodeViewController.simple = YES;
            [self presentViewController:[[UINavigationController alloc] initWithRootViewController:passcodeViewController] animated:YES completion:nil];
        }
    }
    else {
        NSLog(@"Errore nella generazione del seed: Wrong Seed");
    }
}



- (void) saveSeed: (NSString *) hexSeed withPIN: (NSString *) pin usingSecureStore: (BOOL) useSecureStore {

    NSData * iv, *salt;
    NSError * error;
    
    if (useSecureStore) {
        
        [SecureStoreManagement addSeedToSecureStore: hexSeed];
    }
    else {
        
        // aggiunge un magic number
        NSString * saved = [NSString stringWithFormat: @"%@%@", magicNumber, hexSeed];
        
        // il seed e' già settato e viene convertito in NSData
        NSData* data=[saved dataUsingEncoding:NSUTF8StringEncoding];
        
        NSData * encData = [TuitusOTP encryptData: data  password: pin
                                               iv: &iv salt: &salt error: &error];

        NSData * plainData = [TuitusOTP decryptData: encData password: pin
                                                 iv: iv salt: salt error: &error];

        NSString * plainText = [[NSString alloc] initWithData:plainData
                                                     encoding:NSUTF8StringEncoding];
        
        BOOL eq = [saved isEqualToString: plainText];
        
        NSAssert(eq, @"AES Encryption failure plaintext does not correspond");
        
        if (eq) {
            
            // save data in the info.plist property list file
            [AppSettings setInitVector: iv];
            
            [AppSettings setSalt: salt];
            
            [AppSettings setEncryptedSeed: encData];
        }
       
#ifdef DEBUG
                
        NSData *iv2 = [AppSettings getInitVector];
        
        NSData *salt2 = [AppSettings getSalt];
        
        NSData *enc2 = [AppSettings getEncryptedSeed];
        
        NSAssert( [iv isEqualToData: iv2] && [salt isEqualToData: salt2] && [encData isEqualToData: enc2], @"Corrupted binary material");
        
        //    UIAlertView* alertView = [[UIAlertView alloc] initWithTitle:@"Notifica" message:@"Generazione del seed terminata con successo"
        //                                                   delegate:self cancelButtonTitle:@"OK" otherButtonTitles:nil];
        //    [alertView show];
#endif
        
    }
    
    self._seed = nil;   // cancella il seed dovrà essere caricato prima dell'uso
    [AppSettings setSeedAvailable: YES];
}



- (void) destroySeed {
    
    self._seed = nil;   // cancella il seed dovrà essere caricato prima dell'uso
    [AppSettings setSeedAvailable: NO];
    
    if ([AppSettings getUseSecureStore] == YES) {
        
        // se attivo il securestore lo rimuove solo dal secure store
        [SecureStoreManagement deleteSeedFromSecureStore];
    }
    else {

        [AppSettings setInitVector: nil];
        
        [AppSettings setSalt: nil];
        
        [AppSettings setEncryptedSeed: nil];
    }

    NSLog(@"Seed removed, App must be reInited");
}



- (BOOL) moveToSecureStore {
    
    if ([AppSettings getSeedAvailable] == YES) {
        [self loadSeed:NO];
        return YES;
    }
    return NO;
}


- (BOOL) moveToPIN {
    // tutto da rifare !!! Load and Save seed devono avere come parametro lo store.
    if ([AppSettings getSeedAvailable] == YES) {
        [self loadSeed: NO];
        return YES;
    }
    return NO;
}



- (void) enableApp: (BOOL) value {
    
    // abilita il bottone per la generazione
    [self.btGenerate setEnabled: value];

    [self.btGenerate setTitle:NSLocalizedString(@"NOTREADY-STR", nil) forState: UIControlStateDisabled];
    [self.btGenerate setTitle:NSLocalizedString(@"READY-STR", nil) forState: UIControlStateNormal];
    
    if (value) {
        [self.btGenerate setBackgroundColor: BlueColor];
    }
    else {
        [self.btGenerate setBackgroundColor: ButtonDisabledColor];
    }
    
//    if ([AppSettings getUseSecureStore]) {

//        [self.txtPIN setHidden: YES];
//        [self.lblInsertPIN setHidden: YES];
//    }
 }




- (void) generateOTP {
    
    time_t epoch = [[NSDate date] timeIntervalSince1970];
    
    time_t minutes = epoch / clockRound;    // deve essere un parametro di configurazione

    NSLog( @"EPOCH = %ld, (x %d)", minutes, clockRound);

    //Proviamo sia il minuto calcolato che quelli prima e dopo
    self.otp = [NSString stringWithFormat: @"%@",
                        [TuitusOTP generateTimeOTP:self._seed timeData:minutes
                        length: (int) self.OTPLength hashType: self.hMacAlg]];
    
    NSLog(@"otp: %@", self.otp);
        
    self._seed = nil;    // azzera il seed
    // [self.lblOTP setText: otp];
    
    [self performSegueWithIdentifier: @"showOTP" sender: self];

    [NSThread sleepForTimeInterval: 0.100];
    
    [self.vc displayOTP: self.otp withTime: epoch];

}


#pragma mark - UIViewControllerTransitioningDelegate

/*
 Called when presenting a view controller that has a transitioningDelegate
 */
- (id<UIViewControllerAnimatedTransitioning>)animationControllerForPresentedController:(UIViewController *)presented
                                                                  presentingController:(UIViewController *)presenting
                                                                      sourceController:(UIViewController *)source
{
    id<UIViewControllerAnimatedTransitioning> animationController;
    
    // Options modal
//    if ([presented isKindOfClass:[UINavigationController class]] &&
//        [((UINavigationController *)presented).topViewController isKindOfClass:[SettingsListTableViewController class]]) {
//
//        SOLOptionsTransitionAnimator *animator = [[SOLOptionsTransitionAnimator alloc] init];
//        animator.appearing = YES;
//        animator.duration = 0.35;
//        animationController = animator;
//    }
    // Options Push
    if ([presented isKindOfClass:[SettingsListTableViewController class]]) {
        SOLSlideTransitionAnimator *animator = [[SOLSlideTransitionAnimator alloc] init];
        animator.appearing = YES;
        animator.duration = kDuration;
        animator.edge = SOLEdgeRight;
        animationController = animator;
    }
    // Slide
    else if ([presented isKindOfClass:[ShowOTPViewController class]]) {
        SOLSlideTransitionAnimator *animator = [[SOLSlideTransitionAnimator alloc] init];
        animator.appearing = YES;
        animator.duration = kDuration;
        animator.edge = SOLEdgeRight;
        animationController = animator;
    }
    // Slide
    else if ([presented isKindOfClass:[RemoteSeedViewController class]]) {
        SOLSlideTransitionAnimator *animator = [[SOLSlideTransitionAnimator alloc] init];
        animator.appearing = YES;
        animator.duration = kDuration;
        animator.edge = SOLEdgeRight;
        animationController = animator;
    }
    
    return animationController;
}



/*
 Called when dismissing a view controller that has a transitioningDelegate
 */
- (id<UIViewControllerAnimatedTransitioning>)animationControllerForDismissedController:(UIViewController *)dismissed
{
    id<UIViewControllerAnimatedTransitioning> animationController;
    
    // Options modal
//    if ([dismissed isKindOfClass:[UINavigationController class]] &&
//        [((UINavigationController *)dismissed).topViewController isKindOfClass:[SettingsListTableViewController class]]) {
//        
//        SOLOptionsTransitionAnimator *animator = [[SOLOptionsTransitionAnimator alloc] init];
//        animator.appearing = NO;
//        animator.duration = 0.35;
//        animationController = animator;
//    }
    // Options push
    if ([dismissed isKindOfClass:[SettingsListTableViewController class]]){
        SOLSlideTransitionAnimator *animator = [[SOLSlideTransitionAnimator alloc] init];
        animator.appearing = NO;
        animator.duration = kDuration;
        animationController = animator;
    }
    // Slide
    else if ([dismissed isKindOfClass:[ShowOTPViewController class]]) {
        SOLSlideTransitionAnimator *animator = [[SOLSlideTransitionAnimator alloc] init];
        animator.appearing = NO;
        animator.duration = kDuration;
        animator.edge = SOLEdgeLeft;
        animationController = animator;
    }
    // Slide
    else if ([dismissed isKindOfClass:[RemoteSeedViewController class]]) {
        SOLSlideTransitionAnimator *animator = [[SOLSlideTransitionAnimator alloc] init];
        animator.appearing = NO;
        animator.duration = kDuration;
        animator.edge = SOLEdgeLeft;
        animationController = animator;
    }
    
    return animationController;
}



#pragma mark - UINavigationControllerDelegate

/*
 Called when pushing/popping a view controller on a navigation controller that has a delegate
 */
- (id<UIViewControllerAnimatedTransitioning>)navigationController:(UINavigationController *)navigationController
                                  animationControllerForOperation:(UINavigationControllerOperation)operation
                                               fromViewController:(UIViewController *)fromVC
                                                 toViewController:(UIViewController *)toVC
{
    id<UIViewControllerAnimatedTransitioning> animationController;
    
    
    // Options - Push
    if ([toVC isKindOfClass:[SettingsListTableViewController class]] && operation == UINavigationControllerOperationPush) {
        SOLSlideTransitionAnimator *animator = [[SOLSlideTransitionAnimator alloc] init];
        animator.appearing = YES;
        animator.duration = kDuration;
        animator.edge = SOLEdgeRight;
        animationController = animator;
    }
    // Slide - Pop
    else if ([fromVC isKindOfClass:[SettingsListTableViewController class]] && operation == UINavigationControllerOperationPop) {
        SOLSlideTransitionAnimator *animator = [[SOLSlideTransitionAnimator alloc] init];
        animator.appearing = NO;
        animator.duration = kDuration;
        animator.edge = SOLEdgeRight;
        animationController = animator;
    }
    // Slide - Push
    if (([toVC isKindOfClass:[ShowOTPViewController class]] ||
         [toVC isKindOfClass:[RemoteSeedViewController class]]) && operation == UINavigationControllerOperationPush) {
        SOLSlideTransitionAnimator *animator = [[SOLSlideTransitionAnimator alloc] init];
        animator.appearing = YES;
        animator.duration = kDuration;
        animator.edge = SOLEdgeRight;
        animationController = animator;
    }
    // Slide - Pop
    else if (([fromVC isKindOfClass:[ShowOTPViewController class]] ||
              [fromVC isKindOfClass:[RemoteSeedViewController class]]) && operation == UINavigationControllerOperationPop) {
        SOLSlideTransitionAnimator *animator = [[SOLSlideTransitionAnimator alloc] init];
        animator.appearing = NO;
        animator.duration = kDuration;
        animator.edge = SOLEdgeRight;
        animationController = animator;
    }
    
    return animationController;
}


#pragma mark - PAPasscodeViewControllerDelegate

- (void)PAPasscodeViewControllerDidCancel:(PAPasscodeViewController *)controller {
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (void)PAPasscodeViewControllerDidEnterAlternativePasscode:(PAPasscodeViewController *)controller {
    [self dismissViewControllerAnimated:YES completion:^() {
        // [[[UIAlertView alloc] initWithTitle:nil message:@"Alternative Passcode entered correctly" delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil] show];
    }];
}

- (void)PAPasscodeViewControllerDidEnterPasscode:(PAPasscodeViewController *)controller {
    [self dismissViewControllerAnimated:YES completion:^() {
        // [[[UIAlertView alloc] initWithTitle:nil message:@"Passcode entered correctly" delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil] show];
        
        NSString * pin = controller.passcode;
        NSLog(@"Entered: %@", pin); // eliminare
        
        [self loadSeedWithPIN: pin];
    }];
}

- (void)PAPasscodeViewControllerDidSetPasscode:(PAPasscodeViewController *)controller {
    [self dismissViewControllerAnimated:YES completion:^() {
        
        // self._passcode = controller.passcode;
        NSString * pin = controller.passcode;
        NSLog(@"Saving Encrypted Seed with PIN: %@", pin);
        // salva il seed cifrato con il pin nello store standard
        [self saveSeed: self._seed withPIN: pin usingSecureStore:NO];
    }];
}


- (void)PAPasscodeViewControllerDidChangePasscode:(PAPasscodeViewController *)controller {
    [self dismissViewControllerAnimated:YES completion:^() {
        self._passcode = controller.passcode;
    }];
}


- (BOOL) PAPasscodeViewControllerCheckPasscode:(PAPasscodeViewController *)controller withPassword: (NSString*) password {
    
    NSLog(@"Entered PIN: %@", password);
    
    NSString * plaintext = [self decryptSeed: password];
    NSString * magic = [plaintext substringToIndex: 4];
    
    return [magic isEqualToString: magicNumber];
}

// gestione del numero massimo di tentativi
- (void) PAPasscodeViewController:(PAPasscodeViewController *)controller didFailToEnterPasscode:(NSInteger )failedAttempt {

    if ((failedAttempt >= warningFailedPINAttempts) && (failedAttempt < maxFailedPINAttempts)) {
 
        UIAlertView * alertView = [[UIAlertView alloc]
                                   initWithTitle:NSLocalizedString(@"NOTIFY-TITLE", nil)
                                   message:NSLocalizedString(@"DESTROYSEED-MSG", nil)
                                   delegate:self
                                   cancelButtonTitle:NSLocalizedString(@"OK-BT", nil)
                                   otherButtonTitles:nil];
        [alertView show];
    }
    else if (failedAttempt >= maxFailedPINAttempts) {
        // chiude il controller per l'inserimento del PIN
        [self dismissViewControllerAnimated:YES completion:^() {
            // distrugge il seed
            [self destroySeed];
        }];

        UIAlertView * alertView = [[UIAlertView alloc]
                                   initWithTitle:NSLocalizedString(@"NOTIFY-TITLE", nil)
                                   message:NSLocalizedString(@"SEEDDESTROYED-MSG", nil)
                                   delegate:self
                                   cancelButtonTitle:NSLocalizedString(@"OK-BT", nil)
                                   otherButtonTitles:nil];
        
        [alertView setTag: AlertPINDestroyed];
        [alertView show];
    }
}

@end
