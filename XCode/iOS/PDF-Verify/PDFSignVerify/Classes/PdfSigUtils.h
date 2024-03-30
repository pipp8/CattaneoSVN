//
//  PdfSigUtils.h
//  mobidike
//
//  Created by Indio on 12/10/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import  <Foundation/Foundation.h>
#import <Openssl/pkcs7.h>
#import <Openssl/evp.h>
#import <Openssl/pem.h>
#import <Openssl/ssl.h>
#import <Openssl/err.h>
#import <Openssl/x509.h>
#import <Openssl/rsa.h>
 
#define ACROFROM     "AcroForm"
#define SIGFLAGS     "SigFlags"
#define FIELDS       "Fields"
#define V		     "V"
#define SUBFILTER    "SubFilter"
#define ADOBEPKCS7   "adbe.pkcs7.detached"
#define PKCS7CONTENT "Contents"
#define BYTERANGE	 "ByteRange"

typedef enum {RSA_Signature, DSA_Signature, Unknown_Signature} SignAlgo;

#define optAon 0x00000001
#define optBon 0x00000010

#define DEBUG_ON 

// Usage:
// 1] Call PDFFromBundle to load the PDF
// 2] Check if it is signed and how many signaure are there (IsDigitalSigned, SignatureCount)
// 3] Eventually add root certificates with AddRootCertificatesFromBundle
// 4] Loop on verifing with VerifySignature

@interface PdfSigUtils : NSObject {
NSString            *thisPDFpath;
@private
	CGPDFDocumentRef	thisPDF;
	CFURLRef            thisPDFurl;
	
	
	NSString			*caFile; 
	
	Boolean				IsDigitalSigned;
	NSInteger			SignatureCount;
	
	CGPDFArrayRef       SigArray; //Fields
	
	X509_STORE          *CertStore;
}

@property (nonatomic, readonly) Boolean		IsDigitalSigned;
@property (nonatomic, readonly) NSInteger	SignatureCount;
@property (nonatomic, retain ) NSString    *thisPDFpath;
@property (nonatomic, retain ) NSString    *caFile;

-(void) Initialize;
-(CFURLRef) getPDFURL;

-(Boolean) PDFFromBundle:(NSString *)filename WithExtension:(NSString *)extension;
-(Boolean) PDFFromURL:(CFURLRef)pdfURL WithAbsolutePath:(NSString *) pathAbsolute;
-(Boolean) PDFParse;



-(NSString*) GetSignatureSubfilter:(NSInteger)SigIndex;
-(NSData*) GetDetachedPKCS7:(NSInteger)SigIndex;

-(Boolean) VerifySignature:(NSInteger)SigIndex;

-(NSData*) GetPKCS7DetachedData:(NSInteger)SigIndex;
-(CGPDFArrayRef) GetByteRange:(NSInteger)SigIndex;

-(Boolean) AddRootCertificatesFromBundle:(NSString *)filename WithExtension:(NSString *)extension;
-(Boolean) AddRootCertificatesFromCA:(NSString *)pathToFile;
-(Boolean) ParseCACert;

-(NSString *) GetSigner:(NSInteger)SigIndex;

-(Boolean*) Sign:(NSString *)certPath UsingKey:(NSString *)privKeyPath withPIN:(NSString *)PIN;
-(Boolean) Verify;

+(NSData*) GenPKCS7:(NSData*)PDFBuffer WithCert:(NSString *)certPath UsingKey:(NSString *)privKeyPath withPIN:(NSString *)PIN;

@end
