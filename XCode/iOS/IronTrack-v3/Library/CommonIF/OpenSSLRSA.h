//
//  OpenSSLRSA.h
//  MyPhotoMap
//
//  Created by Antonio De Marco on 24/06/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#include <openssl/rsa.h>
#include <openssl/pkcs7.h>

@interface OpenSSLRSA : NSObject {


}
//-(id)init;
//-(void)release;
-(NSInteger) sizeByteOfMod: (RSA *)key;
-(void)freeRSAStruct: (RSA *)key;
-(NSData *)signWithPrivateKey:(RSA *)privKey dataToSign:(NSData *)data;
-(Boolean)verifyWithPublicKey:(RSA *)pubKey dataToVerify:(NSData *)data signToVerify:(NSData *)sign;
-(Boolean)storePrivateKey:(RSA *)privKey path:(NSString *)path Key:(NSString *)key;
-(RSA *)loadPrivateKey:(NSString *)path Key:(NSString *)key;



@end
