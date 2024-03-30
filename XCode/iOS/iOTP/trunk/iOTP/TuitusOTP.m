//
//  TuitusOTP.m
//  iOTP
//
//  Created by Giuseppe Cattaneo on 01/02/15.
//  Copyright (c) 2015 Giuseppe Cattaneo. All rights reserved.
//
//
// $Id: TuitusOTP.m 830 2016-02-18 17:29:18Z cattaneo $
//

#import "TuitusOTP.h"
#import <CommonCrypto/CommonCryptor.h>
#import <CommonCrypto/CommonKeyDerivation.h>

#include "openssl/bn.h"


@implementation TuitusOTP


//+ (NSString *)SHA256_string:(NSString *)input
//{
//    const char *cstr = [input cStringUsingEncoding:NSUTF8StringEncoding];
//    NSData *data = [NSData dataWithBytes:cstr length:input.length];
//    uint8_t digest[CC_SHA256_DIGEST_LENGTH];
//    
//    // This is an iOS5-specific method.
//    // It takes in the data, how much data, and then output format, which in this case is an int array.
//    CC_SHA512(data.bytes, data.length, digest);
//    
//    NSMutableString* output = [NSMutableString stringWithCapacity:CC_SHA256_DIGEST_LENGTH * 2];
//    
//    // Parse through the CC_SHA256 results (stored inside of digest[]).
//    for(int i = 0; i < CC_SHA256_DIGEST_LENGTH; i++) {
//        [output appendFormat:@"%02x", digest[i]];
//    }
//    
//    return output;
//}
//

+ (NSData *) hMacSHA: (CCHmacAlgorithm) crypto
                 key: (NSData *) keyBytes
                data: (NSData *) dataBytes {

    int size;
    NSData *HMAC = nil;
    
    const char *cKey  = [keyBytes bytes];
    const char *cData = [dataBytes bytes];

    switch (crypto) {
        case kCCHmacAlgSHA1:
            size = CC_SHA1_DIGEST_LENGTH; break;
        
        case kCCHmacAlgSHA256:
            size = CC_SHA256_DIGEST_LENGTH; break;
        
        case kCCHmacAlgSHA512:
            size = CC_SHA512_DIGEST_LENGTH; break;
        
        default:
        size = 0;
    }
    
    unsigned char *cHMAC = malloc(size);
    // check for NULL return value!
    if (cHMAC == NULL)
        return nil;
    
    CCHmac( crypto, cKey, [keyBytes length], cData, [dataBytes length], cHMAC);
    
    HMAC = [[NSData alloc] initWithBytes:cHMAC length:size];

    free(cHMAC);
    return HMAC;
}


/**
 * This method converts a NSData byte array to a HEX string
 *
 * @param data: the NSData byte array
 *
 * @return: a NSString
 */

+ (NSString *) bytes2HexStr: (NSData *) data {
        
    NSMutableString * ret = [NSMutableString stringWithString: @""];
    NSRange r;
    r.length = 1;
    unsigned char c[1];
    
    for (int i = 0; i < [data length]; i++) {
        
        r.location = i;
        [data getBytes: c range: r];
        [ret appendFormat: @"%02X", c[0]];
        
//        if ((i+1)% 10 == 0) {
//            [fmt appendString: @"\n"];
//        }
    }
//    [fmt appendString: @"\n"];
    
    return ret;
}



/**
 * This method converts a HEX string to Byte[]
 *
 * @param hex: the HEX string
 *
 * @return: a byte array
 */

+ (NSData *) hexStr2Bytes: (NSString *) hex {
    
    NSData * ret;
    int len ;
    // Adding one byte to get the right conversion
    // Values starting with "0" can be converted
    NSString * value = [NSString stringWithFormat: @"10%@", hex];
    
    BN_CTX * ctx = BN_CTX_new();
    BIGNUM * bn = BN_new();
    
    const char * str = value.UTF8String;
    
    BN_hex2bn( &bn, str);
    
    len = BN_num_bytes(bn);
    unsigned char * byte = malloc(len);
    
    if (byte == NULL) {
        NSLog(@"Cannot allocate byte array");
    }
    else {
        len = BN_bn2bin(bn, byte);
        
        // Copy all the REAL bytes, not the "first"
        ret = [[NSData alloc] initWithBytes: byte+1 length: len-1];
    }
    
    free(byte);
    BN_free( bn);
    BN_CTX_free(ctx);

    return ret;
}



static int DIGITS_POWER[]
// 0 1  2   3    4     5      6       7        8
= {1,10,100,1000,10000,100000,1000000,10000000,100000000 };



/**
 * This method generates a TOTP value for the given
 * set of parameters.
 *
 * @param key: the shared secret, HEX encoded
 * @param time: a value that reflects a time
 * @param returnDigits: number of digits to return
 * @param crypto: the crypto function to use
 *
 * @return: a numeric String in base 10 that includes
 *              {@link truncationDigits} digits
 */

+ (NSString *) generateTimeOTP: (NSString *) key
                    timeData:   (time_t) time
                    length:     (int) codeDigits
                    hashType:   (CCHmacAlgorithm) crypto {

    // [NSString @"0000000000000000" stringByAppendingString:time];
    // paddedTime = [paddedTime substringFromIndex: [paddedTime length] - 16];
    
    // Using the counter
    // First 8 bytes are for the movingFactor
    // Compliant with base RFC 4226 (HOTP)
    // while (time.length() < 16 )
    //     time = "0" + time;
    NSString *paddedTime = [NSString stringWithFormat:@"%016lx", time];
    
    NSLog(@"Padded Time: %@", paddedTime);
    
    // Get the HEX in a Byte[]
    NSData * msg = [TuitusOTP hexStr2Bytes: paddedTime];
    // [TuitusOTP dumpData: msg];
    
    NSData * k = [TuitusOTP hexStr2Bytes: key];
    // [TuitusOTP dumpData: k];
    
    NSData * hash = [TuitusOTP hMacSHA: crypto key: k data: msg];
    // [TuitusOTP dumpData: hash];
    
    // put selected bytes into result int
    char ops[4];
    int len = (int) [hash length];
    
    NSRange r;
    r.location = len -1;
    r.length = 1;
    
    [hash getBytes: ops range: r];  // legge l'ultimo byte
    
    r.location =  ops[0] & 0xf; // calcola l'offset
    r.length = 4;
    
    [hash getBytes: ops range: r];  // get 4 bytes starting from offset
    
    int binary = ((ops[0] & 0x7f) << 24) | ((ops[1] & 0xff) << 16) |
                 ((ops[2] & 0xff) << 8) | (ops[3] & 0xff);
    
    int otp = binary % DIGITS_POWER[codeDigits];
    
    NSString *result = [[NSString alloc] initWithFormat: @"%0*d", codeDigits, otp];
    
    return result;
}


NSString * const kRNCryptManagerErrorDomain = @"it.etuitus.RNCryptManager";

const CCAlgorithm kAlgorithm = kCCAlgorithmAES128;
const NSUInteger kAlgorithmKeySize = kCCKeySizeAES128;
const NSUInteger kAlgorithmBlockSize = kCCBlockSizeAES128;
const NSUInteger kAlgorithmIVSize = kCCBlockSizeAES128;
const NSUInteger kPBKDFSaltSize = 8;
const NSUInteger kPBKDFRounds = 10000;  // ~80ms on an iPhone 4

// ===================

+ (NSData *)encryptData:(NSData *)plainTextData
               password:(NSString *)password
                     iv:(NSData **)iv
                   salt:(NSData **)salt
                  error:(NSError **)error {
    
    NSAssert(iv, @"IV must not be NULL");
    NSAssert(salt, @"salt must not be NULL");
    
    *iv = [self randomDataOfLength:kAlgorithmIVSize];
    *salt = [self randomDataOfLength:kPBKDFSaltSize];
    
    NSData *key = [self AESKeyForPassword:password salt: *salt];
    
    size_t outLength;
    NSMutableData *
    encData = [NSMutableData dataWithLength:plainTextData.length +
                  kAlgorithmBlockSize];
    
    CCCryptorStatus
    result = CCCrypt(kCCEncrypt, // operation
                     kAlgorithm, // Algorithm
                     kCCOptionPKCS7Padding, // options
                     key.bytes, // key
                     key.length, // keylength
                     (*iv).bytes,// iv
                     plainTextData.bytes, // dataIn
                     plainTextData.length, // dataInLength,
                     encData.mutableBytes, // dataOut
                     encData.length, // dataOutAvailable
                     &outLength); // dataOutMoved
    
    if (result == kCCSuccess) {
        encData.length = outLength;
    }
    else {
        if (error) {
            *error = [NSError errorWithDomain:kRNCryptManagerErrorDomain
                                         code:result
                                     userInfo:nil];
        }
        return nil;
    }
    
    return encData;
}


// ===================

+ (NSData *)decryptData:(NSData *)encryptedData
                password:(NSString *)password
                      iv:(NSData *)iv
                    salt:(NSData *)salt
                   error:(NSError **)error {
    
    NSAssert(iv, @"IV must not be NULL");
    NSAssert(salt, @"salt must not be NULL");
    
    NSData * key = [self AESKeyForPassword:password salt: salt];

    size_t outLength;
    NSMutableData *plainTextData = [NSMutableData dataWithLength: [encryptedData length]];
    
    CCCryptorStatus
    result = CCCrypt(kCCDecrypt, // operation
                     kAlgorithm, // Algorithm
                     kCCOptionPKCS7Padding, // options
                     key.bytes, // key
                     key.length, // keylength
                     iv.bytes,// iv
                     encryptedData.bytes, // dataIn
                     encryptedData.length, // dataInLength,
                     plainTextData.mutableBytes, // dataOut
                     plainTextData.length, // dataOutAvailable
                     &outLength); // dataOutMoved
    
    if (result == kCCSuccess) {
        plainTextData.length = outLength;
    }
    else {
        if (error) {
            *error = [NSError errorWithDomain:kRNCryptManagerErrorDomain
                                         code:result
                                     userInfo:nil];
        }
        return nil;
    }
    
    return plainTextData;
}


// ===================

+ (NSData *)randomDataOfLength:(size_t)length {
    NSMutableData *data = [NSMutableData dataWithLength:length];

#ifdef DEBUG
    int result = SecRandomCopyBytes(kSecRandomDefault,
                                    length,
                                    data.mutableBytes);
    NSAssert(result == 0, @"Unable to generate random bytes: %d",
             errno);
#endif
    return data;
}

// ===================

// Replace this with a 10,000 hash calls if you don't have CCKeyDerivationPBKDF
+ (NSData *)AESKeyForPassword:(NSString *)password
                         salt:(NSData *)salt {
    NSMutableData *
    derivedKey = [NSMutableData dataWithLength:kAlgorithmKeySize];
    
    int    result = CCKeyDerivationPBKDF(kCCPBKDF2,     // algorithm
                                  password.UTF8String,  // password
                                  [password lengthOfBytesUsingEncoding:NSUTF8StringEncoding],  // passwordLength
                                  salt.bytes,           // salt
                                  salt.length,          // saltLen
                                  kCCPRFHmacAlgSHA1,    // PRF
                                  kPBKDFRounds,         // rounds
                                  derivedKey.mutableBytes, // derivedKey
                                  derivedKey.length); // derivedKeyLen
    
    // Do not log password here
    NSAssert(result == kCCSuccess,
             @"Unable to create AES key for password: %d", result);
    return derivedKey;
}


+ (void) test {
    // Seed for HMAC-SHA1 - 20 bytes, specified as 40 hex digits
    NSString * seed = @"3132333435363738393031323334353637383930";
    // Seed for HMAC-SHA256 - 32 bytes, specified as 64 hex digits
    NSString * seed32 = @"3132333435363738393031323334353637383930313233343536373839303132";
    // Seed for HMAC-SHA512 - 64 bytes, specified as 128 hex digits
    NSString * seed64 = @"31323334353637383930313233343536373839303132333435363738393031323334353637383930313233343536373839303132333435363738393031323334";
    time_t T0 = 0;
    time_t X = 30;
    time_t testTime[] = {59L, 1111111109L, 1111111111L, 1234567890L, 2000000000L, 20000000000L};
    
    NSDateFormatter* formatter = [[NSDateFormatter alloc] init] ;
    
    //Set the required date format
    [formatter setDateFormat:@"dd-MM-yyyy HH:MM:SS"];
    
    NSDate* ct;
    
    // DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    // df.setTimeZone(TimeZone.getTimeZone("UTC"));

        NSLog(@"+---------------+-----------------------+------------------+--------+--------+");
        NSLog(@"|  Time(sec)    |   Time (UTC format)   | Value of T(Hex)  |  TOTP  | Mode   |");
        NSLog(@"+---------------+-----------------------+------------------+--------+--------+");
        
    for (int i=0; i<6; i++) {
            time_t steps = (testTime[i] - T0)/X;
            // steps = Long.toHexString(T).toUpperCase();
            // while (steps.length() < 16) steps = "0" + steps;
            
            // String fmtTime = String.format("%1$-11s", testTime[i]);
            // String utcTime = df.format(new Date(testTime[i]*1000));

            //Get the string date
            ct = [NSDate dateWithTimeIntervalSince1970: testTime[i]*1000];
            NSString* utcTime = [formatter stringFromDate: ct];
            NSString* fmtTime = [[NSString alloc] initWithFormat: @"%11ld", testTime[i]];

            NSString *otp = [TuitusOTP generateTimeOTP: seed timeData: steps
                                                length: 8 hashType: kCCHmacAlgSHA1];
            NSLog(@"|  %@  |  %@  | 0x%011lX  | %@ | SHA1   |",
                  fmtTime, utcTime, steps, otp);
            
            otp = [TuitusOTP generateTimeOTP: seed32 timeData: steps
                                                length: 8 hashType: kCCHmacAlgSHA256];
            NSLog(@"|  %@  |  %@  | 0x%011lX  | %@ | SHA256 |",
                  fmtTime, utcTime, steps, otp);

            
            otp = [TuitusOTP generateTimeOTP: seed64 timeData: steps
                                      length: 8 hashType: kCCHmacAlgSHA512];
            NSLog(@"|  %@  |  %@  | 0x%011lX  | %@ | SHA512 |",
                  fmtTime, utcTime, steps, otp);
            
            NSLog(@"+---------------+-----------------------+------------------+--------+--------+");
        }
   }


+ (void) dumpData: (NSData *) data {
    NSString * out = @"";
    NSRange r;
    unsigned char buf[1];
    
    r.length = 1;
    for(int i = 0; i < [data length]; i++) {
        r.location = i;
        [data getBytes: buf range: r];
        out = [out stringByAppendingFormat: @" 0x%2x", buf[0]];
        if ((i + 1) % 10 == 0)
            out = [out stringByAppendingString: @"\n"];
    }
    out = [out stringByAppendingString: @"\n"];
    NSLog(@"%@", out);
}
@end
