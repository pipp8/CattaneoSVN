//
//  OPENSSLX509.m
//  MyPhotoMap
//
//  Created by Antonio De Marco on 09/07/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "OpenSSLX509.h"
#import <openssl/evp.h>
#import <openssl/bio.h>
#include <openssl/engine.h>
#include <openssl/bn.h>
#include <openssl/pem.h>

@implementation OpenSSLX509
/*
-(id)init
{
    
	if (self = [super init])
    {  
		
		//OpenSSL_add_all_algorithms();
    }
	
	
    return self;
}
*/
-(X509 *)loadCertificateWithPath:(NSString *)path {

	FILE *file;
	X509 *cert = NULL;
	const char *cpath = [path UTF8String];
	file = fopen(cpath, "rb");
	cert =  PEM_read_X509(file, nil, nil, nil); 
    fclose(file);
	
	return cert;

}

-(RSA *) getPublicKeyWithCert:(X509 *)cert {
    EVP_PKEY * pkey = NULL; 
	RSA *rsapkey = NULL;
	pkey = X509_get_pubkey(cert);
	
	rsapkey = EVP_PKEY_get1_RSA(pkey);
	
	EVP_PKEY_free(pkey);
	
	return rsapkey;

}

-(Boolean)verifyCert:(X509 *)cert caCert:(X509 *)caCert
{
	Boolean ver = NO;
	EVP_PKEY* pkey = NULL;
	pkey=X509_get_pubkey(caCert);
	
	if (X509_verify(cert, pkey) > 0) 
		ver = YES;
	else
		ver = NO;
	
	EVP_PKEY_free(pkey);
	return ver;
}

-(NSString *) getCertSubject:(X509 *) cert Parameter:(NSString *) param {
	char *str;
		
	str = X509_NAME_oneline(X509_get_subject_name(cert), 0, 0);
    NSString *subject = [NSString stringWithUTF8String: str];
		
	NSString *parameter = [NSString stringWithFormat:@"%@%@",param,@"="];
	
	NSRange toprange = [subject rangeOfString: parameter];
	NSString *value = [subject substringFromIndex:toprange.location];
	
	value = [value substringFromIndex:[parameter length]];
	
	toprange = [value rangeOfString:@"/"];
	
	if (toprange.length != 0) {
		value = [value substringToIndex:toprange.location];
	}
	
	return value;

}

-(NSDate *) getCertStartValidity: (X509 *) cert {

	ASN1_TIME *cert_time;
	char *pstring;
	cert_time = X509_get_notBefore(cert);
	pstring = (char*)cert_time->data;
	NSString *dataStr = [NSString stringWithUTF8String: pstring];
	NSString *dataStrSub = [dataStr substringToIndex:[dataStr length] -1];
	
	NSDateFormatter *dateFormat = [[NSDateFormatter alloc] init];
	NSTimeZone *timeZone = [NSTimeZone timeZoneWithName:@"UTC"];
    [dateFormat setTimeZone:timeZone];
	[dateFormat setDateFormat:@"yyMMddHHmmss"];
	NSDate *date = [dateFormat dateFromString:dataStrSub];
	
	[dateFormat release];
	
	return date;

}

-(NSDate *) getCertEndValidity: (X509 *) cert {
	
	ASN1_TIME *cert_time;
	char *pstring;
	cert_time = X509_get_notAfter(cert);
	pstring = (char*)cert_time->data;
	NSString *dataStr = [NSString stringWithUTF8String: pstring];
	NSString *dataStrSub = [dataStr substringToIndex:[dataStr length] -1];
	
	NSDateFormatter *dateFormat = [[NSDateFormatter alloc] init];
	NSTimeZone *timeZone = [NSTimeZone timeZoneWithName:@"UTC"];
    [dateFormat setTimeZone:timeZone];
	[dateFormat setDateFormat:@"yyMMddHHmmss"];
	NSDate *date = [dateFormat dateFromString:dataStrSub];
	
	[dateFormat release];
	
	return date;
	
}


-(void)FreeX509Struct:(X509 *)cert {
	X509_free(cert);
}
/*
-(void)release {
  //  EVP_cleanup();
	[super release];
	
}
*/
@end
