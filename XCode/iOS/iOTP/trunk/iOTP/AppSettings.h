//
//  AppSettings.h
//  iOTP
//
//  Created by Giuseppe Cattaneo on 14/11/15.
//  Copyright © 2015 Giuseppe Cattaneo. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface AppSettings : NSObject

+(NSInteger) getOTPLength;
+(void) setOTPLength: (int) len;

+(NSInteger) getHMACLength;
+(void) setHMACLength: (int) len;

+(BOOL) getSeedAvailable;
+(void) setSeedAvailable: (BOOL) status;

+(BOOL) getUseSecureStore;
+(void) setUseSecureStore: (BOOL) status;

+(NSData *) getInitVector;
+(void) setInitVector: (NSData *) data;

+(NSData *) getSalt;
+(void) setSalt: (NSData *) data;

+(NSData *) getEncryptedSeed;
+(void) setEncryptedSeed: (NSData *) data;

+(NSString *) getUsername;
+(void) setUsername: (NSString *) user;

@end
