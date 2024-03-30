//
//  TuitusOTP.h
//  iOTP
//
//  Created by Giuseppe Cattaneo on 01/02/15.
//  Copyright (c) 2015 Giuseppe Cattaneo. All rights reserved.
//
//
// $Id: TuitusOTP.h 720 2015-03-08 18:29:14Z cattaneo $
//

#import <Foundation/Foundation.h>

#import <CommonCrypto/CommonHMAC.h>

@interface TuitusOTP : NSObject


+ (NSData *) hMacSHA: (CCHmacAlgorithm) crypto
                 key: (NSData *) keyBytes
                data: (NSData *) dataBytes;

+ (NSString *) bytes2HexStr: (NSData *) data;

+ (NSData *) hexStr2Bytes: (NSString *) hex;

+ (NSString *) generateTimeOTP: (NSString *) key
                      timeData:   (time_t) time
                        length:     (int) codeDigits
                      hashType:   (CCHmacAlgorithm) crypto;

+ (NSData *)encryptData:(NSData *)plainTextData
               password:(NSString *)password
                     iv:(NSData **)iv
                   salt:(NSData **)salt
                  error:(NSError **)error;

+ (NSData *)decryptData:(NSData *)encryptedData
               password:(NSString *)password
                     iv:(NSData *)iv
                   salt:(NSData *)salt
                  error:(NSError **)error;

+ (NSData *)randomDataOfLength:(size_t)length;

+ (NSData *)AESKeyForPassword:(NSString *)password
                         salt:(NSData *)salt ;

+ (void) test;

+ (void) dumpData: (NSData *) data;
@end
