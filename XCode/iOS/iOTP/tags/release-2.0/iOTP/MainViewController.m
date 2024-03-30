//
//  ViewController.m
//  iOTP
//
//  Created by Giuseppe Cattaneo on 01/02/15.
//  Copyright (c) 2015 Giuseppe Cattaneo. All rights reserved.
//
//
// $Id: MainViewController.m 756 2015-09-19 21:16:33Z cattaneo $
//

#import "MainViewController.h"

#import "QuartzCore/QuartzCore.h"

#import "ShowOTPViewController.h"

#import "iOTPMisc.h"


@import LocalAuthentication;


#define magicNumber @"1234"


@interface MainViewController ()

@property BOOL isSeedAvailable;

@property ShowOTPViewController *vc ;

@property (weak, nonatomic) IBOutlet UILabel *lblInsertPIN;

@property (weak, nonatomic) IBOutlet UITextField *txtPIN;

@property (weak, nonatomic) IBOutlet UIButton *btGenerate;

@property UIAlertView *alertPIN;

@property UIAlertView *loadPIN;

@property NSInteger OTPLength;

@property CCHmacAlgorithm hMacAlg;

@property NSString *seed;

@end



@implementation MainViewController


- (void) viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    // You code here to update the view.
    [self enableApp: self.isSeedAvailable];
}


- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view, typically from a nib.

    // loadSSL ErrMessages ????
    
    // [[NSUserDefaults standardUserDefaults] setBool: NO forKey:@"SeedCreated"];

    // set view background color
    self.view.backgroundColor = GrayBackgroundColor;
    
    // abilita i textfield per la rimozione della tastiera
    self.txtPIN.delegate = self;
    self.txtPIN.keyboardAppearance = UIKeyboardTypeNumberPad;
    self.txtPIN.secureTextEntry = YES;
    
    // load/check seed
    [self enableApp: [self isAppActivable]];
    
}



- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
    if ([[segue identifier] isEqualToString:@"showOTP"])
    {
        // passa l'OTP da mostrare
        self.vc = [segue destinationViewController];
        [self.vc setOTP: self.otp];
    }
}


- (IBAction)unwindToNewOTP:(UIStoryboardSegue *)segue {
    
    NSLog(@"Unwinding from %@", [segue identifier]);
}


- (BOOL) textFieldShouldReturn:(UITextField *)textField {
    
    [textField resignFirstResponder];
    
    return YES;
}



// restituisce vero o falso a secondo che sia che il seed sia stato registrato o meno
- (Boolean)isAppActivable {
    
//    [[NSUserDefaults standardUserDefaults] setInteger: 8 forKey:@"OTPLength"];
//    [[NSUserDefaults standardUserDefaults] setInteger: 512 forKey:@"HMacLength"];
 
    
    self.OTPLength = [[NSUserDefaults standardUserDefaults] integerForKey:@"OTPLength"];
    
    if (self.OTPLength < 4 || self.OTPLength > 8)
        self.OTPLength = 8;
    
    NSInteger hashLength = [[NSUserDefaults standardUserDefaults] integerForKey:@"HMacLength"];
    
    switch (hashLength) {
            
        case 128: self.hMacAlg = kCCHmacAlgSHA1; break;

        case 256: self.hMacAlg = kCCHmacAlgSHA256; break;

        case 512: self.hMacAlg = kCCHmacAlgSHA512; break;

        default: self.hMacAlg = kCCHmacAlgSHA512; break;
    }

    return [[NSUserDefaults standardUserDefaults] boolForKey:@"SeedCreated"];
}


- (IBAction)btGenerateClicked:(id)sender {
    
    [self loadSeed];
}

-(void) loadSeed {
    
    if (self.isSeedAvailable == YES) {
        
        if ([self isFingerPrintAvailable] == YES) {
            
            // se disponibile cerca il seed nel secure store
            [self getSeedFromSecureStore];
            
        }
        else {
            // Se il seed esiste lo carica usando il pin inserito nel textbox
            NSString * pin = self.txtPIN.text;
            NSLog(@"Entered: %@", pin); // eliminare
            
            [self loadSeed: pin];

//            self.loadPIN  = [[UIAlertView alloc]
//                             initWithTitle:NSLocalizedString(@"SEEDLOADING-TITLE", nil)
//                                   message:NSLocalizedString(@"INSERTPIN-MSG", nil)
//                                  delegate:self
//                         cancelButtonTitle:NSLocalizedString(@"ABORT-BT", nil)
//                         otherButtonTitles:NSLocalizedString(@"OK-BT", nil), nil];
//            
//            self.loadPIN.alertViewStyle = UIAlertViewStyleSecureTextInput;
//            [self.loadPIN  show];

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


- (void) alertView:(UIAlertView *)alertView didDismissWithButtonIndex: (NSInteger)buttonIndex {
    if (alertView == self.loadPIN && buttonIndex != 0) {
        NSString * pin = [[alertView textFieldAtIndex:0] text];
        NSLog(@"Entered: %@",[[alertView textFieldAtIndex:0] text]); // eliminare
        [self loadSeed: pin];
    }
}


- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex{
    
    if (alertView == self.loadPIN && buttonIndex != 0) {
        // rimuove l'alertView
        [alertView dismissWithClickedButtonIndex:-1 animated:YES];
        
        [self.view setNeedsDisplay]; // refresh
    }
    else if (alertView == self.alertPIN) {
        if (buttonIndex == 1) {
            // [self loadSeed];    // riprova ripremendo il bottone
        }
    }
}


- (void) loadSeed: (NSString *) password {
    
    NSError * error;

    
    NSData *iv = [[NSUserDefaults standardUserDefaults] valueForKey:@"InitVector"];
    
    NSData *salt = [[NSUserDefaults standardUserDefaults] valueForKey:@"Salt"];
    
    NSData *encData = [[NSUserDefaults standardUserDefaults] valueForKey:@"EncryptedSeed"];
    

    NSData * plainData = [TuitusOTP decryptData: encData password: password
                                             iv: iv salt: salt error: &error];
    
    NSString * saved =  [[NSString alloc] initWithData:plainData encoding:NSUTF8StringEncoding];
    
    NSString * magic = [saved substringToIndex: 4];
    
    if (![magic isEqualToString: magicNumber]) {
        self.alertPIN = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"NOTIFY-TITLE", nil)
                                                   message:NSLocalizedString(@"WRONGPIN-MSG", nil)
                                                  delegate:self
                                         cancelButtonTitle:NSLocalizedString(@"OK-BT", nil)
                                         otherButtonTitles:nil];
        [self.alertPIN show];
    }
    else {
        [self setSeed: [saved substringFromIndex: 4]];

        NSLog(@"Seed Loaded: %@", self.seed);
        
        [self.txtPIN setText: @""]; // azzera il vecchio PIN
        
        [self generateOTP]; // e genera l'OTP
    }

//    UIAlertView* alertView = [[UIAlertView alloc] initWithTitle:@"Notifica" message:@"Caricamento del seed terminato con successo"
//                                                       delegate:self cancelButtonTitle:@"OK" otherButtonTitles:nil];
//    [alertView show];
}



- (void) saveSeed: (NSString *) hexSeed withPIN: (NSString *) pin {

    NSData * iv, *salt;
    NSError * error;
    
    if ([self isFingerPrintAvailable]) {
        
        [self addSeedToSecureStore: hexSeed];
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
            [[NSUserDefaults standardUserDefaults] setValue: iv forKey:@"InitVector"];

            [[NSUserDefaults standardUserDefaults] setValue: salt forKey:@"Salt"];

            [[NSUserDefaults standardUserDefaults] setValue: encData forKey:@"EncryptedSeed"];
        }
        
        //    if ([self.seed writeToFile: self.keyFile atomically: NO
        //                      encoding:NSUTF8StringEncoding error: &writeError] != YES) {
        //    if ([encData writeToFile: self.keyFile options:NSDataWritingAtomic
        //                             error: &error] != YES) {
        //            NSString * msg = [NSString stringWithFormat: @"Errore di scrittura del seed: error (%d) %@",
        //                    [error code], [error localizedDescription]];
        //        UIAlertView* alertView = [[UIAlertView alloc] initWithTitle:@"Errore" message: msg
        //                    delegate:self cancelButtonTitle:@"OK" otherButtonTitles:nil];
        //        [alertView show];
        //    }
        
        NSData *iv2 = [[NSUserDefaults standardUserDefaults] valueForKey:@"InitVector"];

        NSData *salt2 = [[NSUserDefaults standardUserDefaults] valueForKey:@"Salt"];
        
        NSData *enc2 = [[NSUserDefaults standardUserDefaults] valueForKey:@"EncryptedSeed"];
        
        
        NSAssert( [iv isEqualToData: iv2] && [salt isEqualToData: salt2] && [encData isEqualToData: enc2], @"Corrupted binary material");
        
        //    UIAlertView* alertView = [[UIAlertView alloc] initWithTitle:@"Notifica" message:@"Generazione del seed terminata con successo"
        //                                                   delegate:self cancelButtonTitle:@"OK" otherButtonTitles:nil];
        //    [alertView show];
    }
    
    [[NSUserDefaults standardUserDefaults] setBool: YES forKey:@"SeedCreated"];

    // cambia stato all'App
    [self enableApp: YES];
}



- (void) enableApp: (BOOL) value {
    
    self.isSeedAvailable = value;
    
    
    // ablita il bottone per la generazione
    [self.btGenerate setEnabled: value];

    [self.btGenerate setTitle:NSLocalizedString(@"NOTREADY-STR", nil) forState: UIControlStateDisabled];
    [self.btGenerate setTitle:NSLocalizedString(@"READY-STR", nil) forState: UIControlStateNormal];
    
    if (value) {
        [self.btGenerate setBackgroundColor: BlueColor];
    }
    else {
        [self.btGenerate setBackgroundColor: ButtonDisabledColor];
    }
    
    if ([self isFingerPrintAvailable]) {

        [self.txtPIN setHidden: YES];
        [self.lblInsertPIN setHidden: YES];
    }
 }




- (void) generateOTP {
    
    time_t epoch = [[NSDate date] timeIntervalSince1970];
    
    time_t minutes = epoch / 60;    // 60 deve essere un parametro di configurazione

    NSLog( @"EPOCH = %ld", minutes);

    //Proviamo sia il minuto calcolato che quelli prima e dopo
    self.otp = [NSString stringWithFormat: @"%@",
                        [TuitusOTP generateTimeOTP:self.seed timeData:minutes
                        length: (int) self.OTPLength hashType: self.hMacAlg]];
    
    NSLog(@"otp: %@", self.otp);
        
    self.seed = nil;    // azzera il seed
    // [self.lblOTP setText: otp];
    
    [self performSegueWithIdentifier: @"showOTP" sender: self];

    [NSThread sleepForTimeInterval: 0.100];
    
    [self.vc displayOTP: self.otp withTime: epoch];

}



/*
 * metodi per la gestione del TouchID
 */


- (void)getSeedFromSecureStore
{
    NSDictionary *query = @{
                            (__bridge id)kSecClass: (__bridge id)kSecClassGenericPassword,
                            (__bridge id)kSecAttrService: @"iOTPService",
                            (__bridge id)kSecReturnData: @YES,
                            (__bridge id)kSecUseOperationPrompt: NSLocalizedString(@"TouchID-MSG", nil)
                            };
    
    
//      dispatch_async(dispatch_get_global_queue( DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^(void){
    
        // meglio la chiamata sincrona altrimenti la GUI non funziona
        CFTypeRef dataTypeRef = NULL;
        
        OSStatus status = SecItemCopyMatching((__bridge CFDictionaryRef)(query), &dataTypeRef);
        NSData *resultData = (__bridge NSData *)dataTypeRef;
        
        self.seed = [[NSString alloc] initWithData:resultData encoding:NSUTF8StringEncoding];
        
        NSString *msg = [NSString stringWithFormat: @"Get Seed Status:%@", [self keychainErrorToString:status]];
        
        if (resultData)
            msg = [msg stringByAppendingString:[NSString stringWithFormat:@"Result: %@", self.seed]];
        
        NSLog(@"%@", msg);
        
        [self generateOTP];

  //  });
}



- (void)addSeedToSecureStore: (NSString *) seed {
    CFErrorRef error = NULL;
    SecAccessControlRef sacObject;
    
    // Should be the secret invalidated when passcode is removed? If not then use kSecAttrAccessibleWhenUnlocked
    sacObject = SecAccessControlCreateWithFlags(kCFAllocatorDefault,
                                                kSecAttrAccessibleWhenPasscodeSetThisDeviceOnly,
                                                kSecAccessControlUserPresence, &error);
    if(sacObject == NULL || error != NULL)
    {
        NSLog(@"can't create sacObject: %@", error);
 //       self.txtDisplaySeed.text = [_txtDisplaySeed.text stringByAppendingString:[NSString stringWithFormat:@"Non posso salvare il seed nello store securo: %@", error]];
        return;
    }
    
    // we want the operation to fail if there is an item which needs authenticaiton so we will use
    // kSecUseNoAuthenticationUI
    NSDictionary *attributes = @{
                                 (__bridge id)kSecClass: (__bridge id)kSecClassGenericPassword,
                                 (__bridge id)kSecAttrService: @"iOTPService",
                                 (__bridge id)kSecValueData: [seed dataUsingEncoding:NSUTF8StringEncoding],
                                 (__bridge id)kSecUseNoAuthenticationUI: @YES,
                                 (__bridge id)kSecAttrAccessControl: (__bridge id)sacObject
                                 };
    
    dispatch_async(dispatch_get_global_queue( DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^(void){
        OSStatus status =  SecItemAdd((__bridge CFDictionaryRef)attributes, nil);
        
        NSString *msg = [NSString stringWithFormat:@"\nSave status: %@", [self keychainErrorToString:status]];
        NSLog(@"%@", msg);
        
        // chiamati da saveSeed
        // [[NSUserDefaults standardUserDefaults] setBool: YES forKey:@"SeedCreated"];
        // cambia stato all'App
        // [self enableApp: YES];
    });
}



- (NSString *)keychainErrorToString: (NSInteger)error
{
    
    NSString *msg = [NSString stringWithFormat:@"%ld",(long)error];
    
    switch (error) {
        case errSecSuccess:
            msg = NSLocalizedString(@"SUCCESS", nil);
            break;
        case errSecDuplicateItem:
            msg = NSLocalizedString(@"ERROR_ITEM_ALREADY_EXISTS", nil);
            break;
        case errSecItemNotFound :
            msg = NSLocalizedString(@"ERROR_ITEM_NOT_FOUND", nil);
            break;
        case -26276: // this error will be replaced by errSecAuthFailed
            msg = NSLocalizedString(@"ERROR_ITEM_AUTHENTICATION_FAILED", nil);
            
        default:
            break;
    }
    
    return msg;
}



- (BOOL) isFingerPrintAvailable
{
    LAContext *context = [[LAContext alloc] init];
    __block  NSString *msg;
    NSError *error;
    BOOL success;
    
    // test if we can evaluate the policy, this test will tell us if Touch ID is available and enrolled
    success = [context canEvaluatePolicy: LAPolicyDeviceOwnerAuthenticationWithBiometrics error:&error];
    
    if (success) {
        msg = @"\nTouchID è disponibile";
    } else {
        msg = @"\nTouchID non è disponibile";
    }
    NSLog(@"%@", msg);
    
    // [self.txtDisplaySeed setText:[ _txtDisplaySeed.text stringByAppendingString:msg]];
    
    return success;
}


@end
