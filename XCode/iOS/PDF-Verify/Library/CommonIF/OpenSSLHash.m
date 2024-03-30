//
//  HashSec.m
//  MyPhotoMap
//
//  Created by Antonio De Marco on 02/07/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "OpenSSLHash.h"
#include <openssl/sha.h>
#include <openssl/evp.h>

@implementation OpenSSLHash



-(NSData *) SHA1:(NSData *)data {

	const unsigned char *message = [data bytes];
	unsigned long messagelen = [data length];
	unsigned char *shaout;
	shaout = (unsigned char*) malloc(SHA_DIGEST_LENGTH); 
	SHA1(message, messagelen, shaout);
	
	NSData *shaData= [NSData dataWithBytes:shaout length:SHA_DIGEST_LENGTH];
	free(shaout);

	return shaData;
}

@end
