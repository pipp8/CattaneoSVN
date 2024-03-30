//
//  OPENSSLPKCS7.m
//  MyPhotoMap
//
//  Created by Antonio De Marco on 09/07/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "OpenSSLPKCS7.h"
#import <openssl/evp.h>
#import <openssl/bio.h>
#include <openssl/engine.h>
#include <openssl/bn.h>
#include <openssl/pem.h>
#include <openssl/x509.h>


@implementation OpenSSLPKCS7

int get_written_BIO_data(BIO* wbio, char** data)
{
	int n;
	char* p;
	if (!data) return -1;
	*data = NULL;
	BIO_flush(wbio);
	n = BIO_get_mem_data(wbio,&p);
	
	if (!((*data)=malloc(n+1))) return -1;
	memcpy(*data, p, n);
	(*data)[n] = '\0';
	return n;
	
}

/*
-(id)init
{
    
	if (self = [super init])
    {  
		OpenSSL_add_all_algorithms();
		
    }
	

	
	
    return self;
}

*/
-(Boolean) verify:(PKCS7 *)pkcs7 Cert:(X509 *)cert {
    X509_STORE *st = NULL;
    st = X509_STORE_new();
	X509_STORE_add_cert(st, cert);
	
	NSInteger test = PKCS7_verify(pkcs7, NULL, st, NULL, NULL, 0);
	
	X509_STORE_free(st);
	if (test <= 0)
	     return NO;

	return YES;
} 


-(PKCS7 *) sign:(NSData *)dataToSign Cert:(X509 *)cert PrivKey:(RSA *)privKey {

   PKCS7 *pkcs7;
   EVP_PKEY *pkey = EVP_PKEY_new();
   EVP_PKEY_set1_RSA(pkey,privKey);
   BIO *bio = BIO_new(BIO_s_mem());

   BIO_write(bio, [dataToSign bytes], [dataToSign length]);
   pkcs7 = PKCS7_sign(cert, pkey, NULL, bio, PKCS7_BINARY);
  
  	
   BIO_free(bio);
   EVP_PKEY_free(pkey);
	
   return pkcs7;
}


-(NSData *) getMessageWithPKCS7:(PKCS7 *)pkcs7 {
    int i =0;
	char *data = NULL;
	char   buf[4096];
	BIO *bio = BIO_new(BIO_s_mem());
	BIO *wbio = BIO_new(BIO_s_mem());
	
    bio=PKCS7_dataInit(pkcs7,NULL);
	
	for (;;) {
		i = BIO_read(bio,buf,sizeof(buf));
		if (i <= 0) break;
		BIO_write(wbio, buf, i);
	}
	
	int len = get_written_BIO_data(wbio, &data);
	
	if (len < 0) {
		BIO_free_all(bio);
     	BIO_free_all(wbio);
		return nil;
	}
	BIO_free_all(wbio);
	wbio = NULL;
	
	BIO_free_all(bio);
	
	NSData *dataret= [NSData dataWithBytes:data length:len];
	return dataret;
	
}

-(PKCS7 *)loadPKCS7WithPath:(NSString *)path {
	
	FILE *file;
	PKCS7 *pkcs7 = NULL;
	const char *cpath = [path UTF8String];
	file = fopen(cpath, "rb");
	pkcs7 = PEM_read_PKCS7(file, NULL, NULL, NULL);
    fclose(file);
	
	return pkcs7;


}

-(PKCS7 *)getPKCS7WithData:(NSData *)data {

	BIO *bio = BIO_new(BIO_s_mem());
	BIO_write(bio, [data bytes], [data length]);
	
	PKCS7 *pkcs7 = PEM_read_bio_PKCS7(bio, NULL, NULL, NULL);
	BIO_free(bio);
	return pkcs7;

} 




-(NSData *)getDataWithPKCS7:(PKCS7 *)pkcs7 {
    int i =0;
	char *data = NULL;
	char   buf[4096];
	BIO *bio = BIO_new(BIO_s_mem());
	BIO *wbio = BIO_new(BIO_s_mem());
	int res = PEM_write_bio_PKCS7(bio, pkcs7);
	
	if (res <= 0) {
		BIO_free_all(bio);
		BIO_free_all(wbio);
		return nil;
	}
    
	for (;;) {
		i = BIO_read(bio,buf,sizeof(buf));
		if (i <= 0) break;
		BIO_write(wbio, buf, i);
	}
   
	int len = get_written_BIO_data(wbio, &data);
		
	if (len < 0) {
	  BIO_free_all(bio);
     	BIO_free_all(wbio);
	return nil;
	}
	BIO_free_all(wbio);
	wbio = NULL;
	BIO_free_all(bio);
	
	
	
	NSData *dataret= [NSData dataWithBytes:data length:len];
	return dataret;
 
}

-(Boolean)storePKCS7WithPath:(NSString *)path pkcs7:(PKCS7 *)pkcs7 {
	
	FILE *file;
    Boolean ret = NO;
	const char *cpath = [path UTF8String];
	file = fopen(cpath, "wb");
	  
	if (PEM_write_PKCS7(file, pkcs7) > 0)
	  ret = YES;
	else
	  ret = NO;
	
	fclose(file);
	return ret;
}


-(void)FreePKCS7Struct:(PKCS7 *)pkcs7 {
	
	PKCS7_free(pkcs7);
}
/*
-(void)release {
   // EVP_cleanup();
	[super release];
	
}
*/

@end
