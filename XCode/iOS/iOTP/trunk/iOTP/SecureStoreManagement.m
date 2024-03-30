//
//  SecureStoreManagement.m
//  iOTP
//
//  Created by Giuseppe Cattaneo on 14/11/15.
//  Copyright © 2015 Giuseppe Cattaneo. All rights reserved.
//

#import "SecureStoreManagement.h"

#import "QuartzCore/QuartzCore.h"

#import "AppSettings.h"




@import LocalAuthentication;


@implementation SecureStoreManagement

/*
 * metodi per la gestione del TouchID
 */


+ (NSString *) getSeedFromSecureStore
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
    
    NSString *seed = [[NSString alloc] initWithData:resultData encoding:NSUTF8StringEncoding];
    
    NSString *msg = [NSString stringWithFormat: @"Get Seed Status:%@", [self keychainErrorToString:status]];
    
    if (resultData) {
        msg = [msg stringByAppendingString:[NSString stringWithFormat:@"Result: %@", seed]];
    

        NSLog(@"%@", msg);
    }

    return seed;    
}



+ (void) addSeedToSecureStore: (NSString *) seed {
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
    });
}



+ (void) deleteSeedFromSecureStore
{
    NSDictionary *query = @{
                            (__bridge id)kSecClass: (__bridge id)kSecClassGenericPassword,
                            (__bridge id)kSecAttrService: @"iOTPService"
                            };
    
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^(void){
        OSStatus status = SecItemDelete((__bridge CFDictionaryRef)(query));
        
        NSString *msg = [NSString stringWithFormat: @"Seed Delete status: %@", [self keychainErrorToString:status]];
        NSLog(@"%@", msg);
    });
}



+ (NSString *)keychainErrorToString: (NSInteger)error
{
    
    NSString *msg = [NSString stringWithFormat:@"%ld",(long)error];
    
    switch (error) {
        case errSecSuccess:
            msg = NSLocalizedString(@"SUCCESS-STR", nil);
            break;
        case errSecDuplicateItem:
            msg = NSLocalizedString(@"ITEM_ALREADY_EXISTS-ERR", nil);
            break;
        case errSecItemNotFound :
            msg = NSLocalizedString(@"ITEM_NOT_FOUND-ERR", nil);
            break;
        case -26276: // this error will be replaced by errSecAuthFailed
            msg = NSLocalizedString(@"ITEM_AUTHENTICATION_FAILED-ERR", nil);
            
        default:
            break;
    }
    
    return msg;
}



+ (BOOL) isFingerPrintAvailable
{
    LAContext *context = [[LAContext alloc] init];

    NSError *error;
    BOOL success;
    
    // test if we can evaluate the policy, this test will tell us if Touch ID is available and enrolled
    success = [context canEvaluatePolicy: LAPolicyDeviceOwnerAuthenticationWithBiometrics error:&error];
    
    return success;
}


@end
