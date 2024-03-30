//
//  CommonSec.m
//  MyPhotoMap
//
//  Created by Antonio De Marco on 02/07/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "CommonSec.h"
#include <openssl/bio.h>
#include <openssl/evp.h>

void ERR_load_crypto_strings();
void ERR_free_strings();


@implementation CommonSec

- (void)add_all_algorithms {
    OpenSSL_add_all_algorithms();
} 


- (id) init
{
	if (self = [super init])
	{
		ERR_load_crypto_strings();
	}
	return self;
}

- (void)cleanup {
	ERR_free_strings();
    EVP_cleanup();
}


- (NSString *)base64EncodedStringWithData:(NSData *)data
{
    BIO *context = BIO_new(BIO_s_mem());
	
    BIO *command = BIO_new(BIO_f_base64());
    context = BIO_push(command, context);
	
    BIO_write(context, [data bytes], [data length]);
    BIO_flush(context);

  
    char *outputBuffer;
    long outputLength = BIO_get_mem_data(context, &outputBuffer);
    NSString *encodedString = [NSString stringWithCString: outputBuffer length: outputLength];//TODO: [DEPRECATED]
	

    BIO_free_all(context);
	
    return encodedString;
}

- (NSData *)dataByBase64DecodingString:(NSString *)decode
{   
	int BUFFSIZE = 256;
    decode = [decode stringByAppendingString:@"\n"];
    NSData *data = [decode dataUsingEncoding:NSASCIIStringEncoding];
    
   
    BIO *command = BIO_new(BIO_f_base64());
    BIO *context = BIO_new_mem_buf((void *)[data bytes], [data length]);
	
    
    context = BIO_push(command, context);

    NSMutableData *outputData = [NSMutableData data];
    
	
    int len;
    char inbuf[BUFFSIZE];
    while ((len = BIO_read(context, inbuf, BUFFSIZE)) > 0)
    {
        [outputData appendBytes:inbuf length:len];
    }
	
    BIO_free_all(context);
    [data self]; 	
    return outputData;
}

- (NSString*)getHexStringWithData:(NSData *)data {
	static const char hexdigits[] = "0123456789ABCDEF";
	const size_t numBytes = [data length];
	const unsigned char* bytes = [data bytes];
	char *strbuf = (char *)malloc(numBytes * 2 + 1);
	char *hex = strbuf;
	NSString *hexBytes = nil;
	
	for (int i = 0; i<numBytes; ++i) {
		const unsigned char c = *bytes++;
		*hex++ = hexdigits[(c >> 4) & 0xF];
		*hex++ = hexdigits[(c ) & 0xF];
	}
	*hex = 0;
	hexBytes = [NSString stringWithUTF8String:strbuf];
	free(strbuf);
	return [hexBytes lowercaseString];
}

-(NSString *)replicateStr:(NSString *)strReplay number:(NSInteger)number {
	NSString *str;
	str=@"";

	for (NSInteger i=0;i<number;i++) {
		str = [str stringByAppendingString:strReplay];

	}
	return str;
}


	


@end
