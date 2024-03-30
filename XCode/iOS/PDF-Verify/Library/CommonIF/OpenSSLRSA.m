//
//  OpenSSLRSA.m
//  MyPhotoMap
//
//  Created by Antonio De Marco on 24/06/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "OpenSSLRSA.h"
#include <openssl/engine.h>
#include <openssl/bn.h>
#include <openssl/pem.h>
#include <openssl/bio.h>
#include <openssl/evp.h>
#include <openssl/err.h>
#import <time.h>
#include <stdlib.h>


@implementation OpenSSLRSA

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
-(NSInteger)sizeByteOfMod:(RSA *)key {
	return RSA_size(key);

} 

-(Boolean)storePrivateKey:(RSA *)privKey path:(NSString *)path Key:(NSString *)key {
  	Boolean res = NO;
	FILE *file;
	unsigned char *ckey = NULL;
	int ckeylen = 0;
	if (key != nil) {
	  ckey = [key UTF8String];
	  ckeylen = [key length];
	}
	const char *cpath = [path UTF8String];
	 
	file = fopen(cpath, "wb");

	if (PEM_write_RSAPrivateKey(file, privKey, EVP_aes_256_cbc(), ckey, ckeylen, NULL, NULL) > 0) {
		res = YES;
	} else {
		res = NO;
	}
	fclose(file);
	return res;	
	
}

-(RSA *)loadPrivateKey:(NSString *)path Key:(NSString *)key; {

	FILE *file;
	RSA *keyret = RSA_new();
    unsigned char *ckey = NULL;
	
	if (key != nil) 
		ckey = [key UTF8String];
	
	
	const char *cpath = [path UTF8String];
	file = fopen(cpath, "rb");

    keyret = PEM_read_RSAPrivateKey(file, NULL, NULL, ckey);

	fclose(file);
	return keyret;	
	
}

-(NSData *)signWithPrivateKey:(RSA *)privKey dataToSign:(NSData *)data {
    unsigned char* sigret;
	unsigned int siglen; 
	int res;
	
	const unsigned char *message = [data bytes];
	unsigned int messagelen = [data length]; 
	sigret = (unsigned char*) malloc(RSA_size(privKey)); 
	res = RSA_sign(NID_sha256WithRSAEncryption,message, messagelen, sigret, &siglen, privKey);
	
	if (messagelen > RSA_size(privKey) ) 
		return nil;
	
	
	NSData *datasign= [NSData dataWithBytes:sigret length:siglen];
	
	
	free(sigret);
	if (res != 1) 
		return nil;
	
	return datasign;
	
}


-(Boolean)verifyWithPublicKey:(RSA *)pubKey dataToVerify:(NSData *)data signToVerify:(NSData *)sign {
    const unsigned  char *message = [data bytes];
	unsigned int messagelen = [data length];
		
	const unsigned char *signature = [sign bytes];
	unsigned int signaturelen = [sign length];

	int tested = RSA_verify(NID_sha256WithRSAEncryption, (unsigned char*) message, messagelen, signature, signaturelen, pubKey); 
     
	if (tested == 1)	
	  return YES;
	else {
		return NO;
	}
}


-(void)freeRSAStruct: (RSA *)key {
	if (key != NULL) {
		key->n = NULL;
		key->e = NULL;
		key->d = NULL;
		key->p = NULL;
		key->q = NULL;
		RSA_free(key);
	}
}
/*
-(void)release {
   // EVP_cleanup();
	[super release];
	
}
*/


@end
