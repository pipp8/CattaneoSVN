//
//  OPENSSLPKCS7.h
//  MyPhotoMap
//
//  Created by Antonio De Marco on 09/07/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <openssl/pkcs7.h>

@interface OpenSSLPKCS7 : NSObject {

}
//-(id)init;
//-(void)release;
-(void)FreePKCS7Struct:(PKCS7 *)pkcs7;
-(PKCS7 *)loadPKCS7WithPath:(NSString *)path;
-(Boolean)storePKCS7WithPath:(NSString *)path pkcs7:(PKCS7 *)pkcs7;
-(PKCS7 *) sign:(NSData *)dataToSign Cert:(X509 *)cert PrivKey:(RSA *)privKey;
-(Boolean) verify:(PKCS7 *)pkcs7 Cert:(X509 *)cert DetachedData:(NSData *) indata;
-(X509 *) getAllSigners:(PKCS7 *)pkcs7;
-(NSData *) getMessageWithPKCS7:(PKCS7 *)pkcs7;
-(PKCS7 *)getPKCS7WithData:(NSData *)data;
-(NSData *)getDataWithPKCS7:(PKCS7 *)pkcs7;
@end
