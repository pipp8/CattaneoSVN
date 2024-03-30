//
//  SecureStoreManagement.h
//  iOTP
//
//  Created by Giuseppe Cattaneo on 14/11/15.
//  Copyright © 2015 Giuseppe Cattaneo. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface SecureStoreManagement : NSObject

+ (NSString *) getSeedFromSecureStore;

+ (void) addSeedToSecureStore: (NSString *) seed;

+ (void) deleteSeedFromSecureStore;

+ (BOOL) isFingerPrintAvailable;

@end
