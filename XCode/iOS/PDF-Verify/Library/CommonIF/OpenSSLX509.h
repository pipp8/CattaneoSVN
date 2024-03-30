//
//  OPENSSLX509.h
//  MyPhotoMap
//
//  Created by Antonio De Marco on 09/07/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <openssl/x509.h>
#include <openssl/rsa.h>

@interface OpenSSLX509 : NSObject {

}

//-(id)init;
//-(void)release;
-(X509 *)loadCertificateWithPath:(NSString *)path;
-(void)FreeX509Struct:(X509 *)cert;
-(RSA *) getPublicKeyWithCert:(X509 *)cert;
-(Boolean)verifyCert:(X509 *)cert caCert:(X509 *)caCert;
-(NSString *) getCertSubject:(X509 *) cert Parameter:(NSString *) param;
-(NSDate *) getCertStartValidity: (X509 *) cert;
-(NSDate *) getCertEndValidity: (X509 *) cert;

@end
