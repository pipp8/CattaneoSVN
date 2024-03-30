//
//  OPENSSLTSP.h
//  MyPhotoMap
//
//  Created by Antonio De Marco on 13/07/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#include <openssl/ts.h>
#include <openssl/evp.h>

@interface OpenSSLTSP : NSObject {

	}

typedef enum TSPResponseStatus {tspGranted, tspGrantedWithModification,tspRejected,tspWaiting,tspRevocationWarning,tspRevoked,tspWrongStatusInfo,tspResponseNotParsable} TSPResponseStatus;

//-(id)init;
//-(void)release;
-(NSData *)CreateRequestWithHash:(NSData *)hash HashName:(NSString *)hashname Policy:(NSString *)policy nonce:(long int)nonce CertRequired:(Boolean)certRequired;
-(TSPResponseStatus) verifyStatusWithResponse:(NSData *)response;
-(NSData *)getMessageImprintWithResponse:(NSData *)response;
-(PKCS7 *)getTokenWithResponse:(NSData *)response;
-(NSDate *)getDateWithResponse:(NSData *)response;
-(Boolean)verifySignatureWithResponse:(NSData *)responseData Request:(NSData *)requestData Cert:(X509 *)cert CACert:(X509 *)cacert;

@end
