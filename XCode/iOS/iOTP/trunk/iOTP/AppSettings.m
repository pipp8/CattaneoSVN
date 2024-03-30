//
//  AppSettings.m
//  iOTP
//
//  Created by Giuseppe Cattaneo on 14/11/15.
//  Copyright © 2015 Giuseppe Cattaneo. All rights reserved.
//

#import "AppSettings.h"



@implementation AppSettings


+(NSInteger) getOTPLength
{
    NSInteger val = [[NSUserDefaults standardUserDefaults] integerForKey:@"OTPLength"];
    
    if ((val <= 0) || (val > 8)) {
        val = 8;    // gestisce il default value
        [[NSUserDefaults standardUserDefaults] setInteger: val forKey:@"OTPLength"];
    }
    return val;
}

+(void) setOTPLength: (int) len
{
    [[NSUserDefaults standardUserDefaults] setInteger: len forKey:@"OTPLength"];
    [[NSUserDefaults standardUserDefaults] synchronize];
}


+(NSInteger) getHMACLength
{
    return [[NSUserDefaults standardUserDefaults] integerForKey:@"HMacLength"];
}

+(void) setHMACLength: (int) len
{
    [[NSUserDefaults standardUserDefaults] setInteger: len forKey:@"HMacLength"];
    [[NSUserDefaults standardUserDefaults] synchronize];
}


+(BOOL) getSeedAvailable
{
    return [[NSUserDefaults standardUserDefaults] boolForKey:@"SeedInited"];
}

+(void) setSeedAvailable: (BOOL) status
{
    [[NSUserDefaults standardUserDefaults] setBool: status forKey:@"SeedInited"];
    [[NSUserDefaults standardUserDefaults] synchronize];
}


+(BOOL) getUseSecureStore
{
    return [[NSUserDefaults standardUserDefaults] boolForKey:@"UseSecureStore"];
}

+(void) setUseSecureStore: (BOOL) status
{
    [[NSUserDefaults standardUserDefaults] setBool: status forKey:@"UseSecureStore"];
    [[NSUserDefaults standardUserDefaults] synchronize];
}


+(NSData *) getInitVector
{
    return [[NSUserDefaults standardUserDefaults] valueForKey:@"InitVector"];
}

+(void) setInitVector: (NSData *) data
{
    [[NSUserDefaults standardUserDefaults] setValue: data forKey:@"InitVector"];
    [[NSUserDefaults standardUserDefaults] synchronize];
}


+(NSData *) getSalt
{
    return [[NSUserDefaults standardUserDefaults] valueForKey:@"Salt"];
}


+(void) setSalt: (NSData *) data
{
    [[NSUserDefaults standardUserDefaults] setValue: data forKey:@"Salt"];
    [[NSUserDefaults standardUserDefaults] synchronize];
}


+(NSData *) getEncryptedSeed
{
    return [[NSUserDefaults standardUserDefaults] valueForKey:@"EncryptedSeed"];
}


+(void) setEncryptedSeed: (NSData *) data
{
    [[NSUserDefaults standardUserDefaults] setValue: data forKey:@"EncryptedSeed"];
    [[NSUserDefaults standardUserDefaults] synchronize];
}


+(NSString *) getUsername
{
    return [[NSUserDefaults standardUserDefaults] stringForKey:@"Registereduser"];
}

+(void) setUsername: (NSString *) user
{
    [[NSUserDefaults standardUserDefaults] setObject: user forKey: @"Registereduser"];
    [[NSUserDefaults standardUserDefaults] synchronize];
}


@end
