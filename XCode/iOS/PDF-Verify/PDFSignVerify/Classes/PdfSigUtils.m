//
//  PdfSigUtils.m
//  mobidike
//
//  Created by Indio on 12/10/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "PdfSigUtils.h"


@implementation PdfSigUtils

@synthesize IsDigitalSigned;
@synthesize SignatureCount;
@synthesize thisPDFpath; 
@synthesize caFile; 

-(void)Initialize
{
	//initialize OpenSSL
	SSL_load_error_strings();
	OpenSSL_add_all_algorithms();
	
	self.thisPDFpath = NULL;
	
	IsDigitalSigned = FALSE;
	SignatureCount = 0;
	CertStore = NULL;
}


-(NSString *) GetSigner:(NSInteger)SigIndex
{
	// First load the PKCS7 signature BLOB
	NSData *temp = [self GetDetachedPKCS7:SigIndex];
	unsigned long len = [temp length];
	unsigned char *p  = (unsigned char *)[temp bytes];
	
	// Anche qui non posso fare la release... why!
	// [temp release];
	
	PKCS7 *p7 = d2i_PKCS7(NULL, &p, len);
	if (!p7) {
		NSLog(@"GetSigner: Contents is not a valid pkcs7 object!");
		
		return FALSE;
	}
	
	// Get all the signers info from the PKCS7
	STACK_OF(PKCS7_SIGNER_INFO) *sk = PKCS7_get_signer_info(p7);
	if(!sk || !sk_PKCS7_SIGNER_INFO_num(sk)) {
		//NSLog(@"parsePKCS7: %s", PKCS7err(PKCS7_F_PKCS7_VERIFY,PKCS7_R_NO_SIGNATURES_ON_DATA));
		return FALSE;
	}
	
	// Verify il the signer info is available
	int signcount = sk_PKCS7_SIGNER_INFO_num(sk);
	if(signcount < SigIndex) {
		NSLog(@"GetSigner: signature index not in range");
		return FALSE;
	}
	
	// Get the first certificate as long as each PKCS7 in PDF has only one signtaure in it
	PKCS7_SIGNER_INFO *si=sk_PKCS7_SIGNER_INFO_value(sk, 0);
	if(si==NULL) {
		NSLog(@"GetSigner: Pkcs7 get signer info value failed");
		return FALSE;
	}
	
	// Exsercise: Get the digest Alg
	X509 *sign_cert = X509_find_by_issuer_and_serial(p7->d.signed_and_enveloped->cert,si->issuer_and_serial->issuer,si->issuer_and_serial->serial);
	if(sign_cert==NULL) {
		NSLog(@"GetSigner: pkcs7 get sign cert from signer info failed\n");
		return FALSE;
	}
	
	NSString *ret = [NSString stringWithUTF8String: X509_NAME_oneline(X509_get_subject_name(sign_cert), NULL,0)];
	
	PKCS7_free(p7);

	return ret;
}

/*
void print_certificate(X509 *cert) 
{ 
	char s[256]; 
	printf(" version %li\n", X509_get_version(cert)); 
	printf(" not before %s\n", X509_get_notBefore(cert)->data); 
	printf(" not after %s\n", X509_get_notAfter(cert)->data); 
	printf(" signature type %s\n", OBJ_nid2sn(X509_get_signature_type(cert))); 
	printf(" serial no %li\n", ASN1_INTEGER_get(X509_get_serialNumber(cert))); 

	X509_NAME_oneline(X509_get_issuer_name(cert), s, 256); 
	printf(" issuer %s\n", s); 
	X509_NAME_oneline(X509_get_subject_name(cert), s, 256); 
	
	printf(" subject %s\n", s); 
	printf(" cert type %i\n", X509_certificate_type(cert, X509_get_pubkey(cert))); 
} 
*/
 
-(Boolean) AddRootCertificatesFromBundle:(NSString *)filename WithExtension:(NSString *)extension
{
	// Load CA Certificate 
	caFile = [[NSBundle mainBundle] pathForResource:filename ofType:extension];
	if (caFile == NULL) {
		NSLog(@"AddRootCertificatesFromBundle: Certificate not in Bundle");
		return FALSE;
	}
	
	return [self ParseCACert];
}

-(Boolean) AddRootCertificatesFromCA:(NSString *)pathToFile
{
	caFile = pathToFile;
	
	return [self ParseCACert];
}


-(Boolean) ParseCACert
{
	const char* fileName = [caFile UTF8String];
	
	NSLog(@"%s", fileName);
	FILE *fp = fopen(fileName, "r");
	if (!fp) {
		NSLog(@"AddRootCertificatesFromBundle: Error opening CA file");
		return FALSE;
	}
	X509 *X509_CA = PEM_read_X509(fp, NULL, NULL, NULL);
	fclose(fp);	
	
	if (CertStore == NULL) {
		CertStore = X509_STORE_new();
	}
	
	X509_STORE_add_cert(CertStore, X509_CA);
	
	// Do i need it?
	//X509_free(X509_CA);
	
	return TRUE;
	
}







-(Boolean) PDFFromBundle:(NSString *)filename WithExtension:(NSString *)extension
{
	// Load the PDF from Bundle
	NSString *tmp = [[filename stringByAppendingString:@"."] stringByAppendingString:extension];
	
	thisPDFurl  = CFBundleCopyResourceURL(CFBundleGetMainBundle(), (CFStringRef)tmp, NULL, NULL);
	self.thisPDFpath = [[NSBundle mainBundle] pathForResource:filename ofType:extension];
	thisPDF     = CGPDFDocumentCreateWithURL((CFURLRef)thisPDFurl);
	
	// Perchè se faccio la release ottengo questo?
	// -[CFString release]: message sent to deallocated instance 0x4e80bd0
	// [tmp release];
	
	if (thisPDF == NULL) {
		NSLog(@"Impossible to load the PDF");
		return FALSE;
	}

	return [self PDFParse];
}


//TODO parte del codice è duplicato da PDFFromBundle
-(Boolean) PDFFromURL:(CFURLRef)pdfURL WithAbsolutePath:(NSString *) pathAbsolute
														  
{
	// Load the PDF from URL
	
	
	thisPDFurl = pdfURL;
	
	NSString *tmp = [NSString stringWithString:@"s"];
	self.thisPDFpath = [NSString stringWithString:pathAbsolute];
	self.caFile = [NSString stringWithString:@"s"];
	
	thisPDF     = CGPDFDocumentCreateWithURL((CFURLRef)thisPDFurl);
	
	// Perchè se faccio la release ottengo questo?
	// -[CFString release]: message sent to deallocated instance 0x4e80bd0
	// [tmp release];
	
	if (thisPDF == NULL) {
		NSLog(@"Impossible to load the PDF");
		return FALSE;
	}
	
	return [self PDFParse];
}

-(void)setThisPDFPath:(NSString *) pathToFile
{

	self.thisPDFpath = pathToFile;
}

-(Boolean) PDFParse
{
	// Check if the PDF is signed by looking for some dictionaries
	IsDigitalSigned = FALSE;
	SignatureCount  = 0;
	
	CGPDFDictionaryRef  Catalog = CGPDFDocumentGetCatalog(thisPDF);;
	CGPDFDictionaryRef  cont;
	
	// Get the PDF catalog
	if(CGPDFDictionaryGetDictionary(Catalog, ACROFROM, &cont)) {
		
		// Check the signature FLAGS
		CGPDFInteger SifFlags = 0;
		if (CGPDFDictionaryGetInteger(cont, SIGFLAGS, &SifFlags)){
			
			Byte b = (Byte) SifFlags;
			int SignatureExists = b & optAon;
			//			int AppendOnly      = b & optBon;
			
			if (SignatureExists) {
				
				IsDigitalSigned = TRUE;
				if(CGPDFDictionaryGetArray(cont, FIELDS, &SigArray)) {
					
					SignatureCount = CGPDFArrayGetCount(SigArray);
				}
			}
		} 
	}
	
	return TRUE;	
}


-(NSString *) GetSignatureSubfilter:(NSInteger)SigIndex
{
	CGPDFDictionaryRef SigField, SigDict;
	CGPDFObjectRef     subFilterObj;
	
	if(CGPDFArrayGetDictionary(SigArray, SigIndex, &SigField)) {
	
		if(CGPDFDictionaryGetDictionary(SigField, V, &SigDict)) {
			
			if(CGPDFDictionaryGetObject(SigDict, SUBFILTER, &subFilterObj)) {
							
				char * name;
				CGPDFObjectGetValue( subFilterObj, kCGPDFObjectTypeName, &name );
				NSString *ret = [NSString stringWithUTF8String: name];
				
				return ret;
			}
			NSLog(@"subFilterObj non trovato");
		}
		NSLog(@"SigDict non trovato");
	}
	NSLog(@"SigField non trovato");
	
	return NULL;
}


-(NSData *) GetDetachedPKCS7:(NSInteger)SigIndex
{
	NSString *temp = @ADOBEPKCS7;
	
	if (![[self GetSignatureSubfilter:SigIndex] isEqualToString:temp]) {
		
		NSLog(@"Only %s signature verification is implemented", ADOBEPKCS7);
		return NULL;
	}
	
	CGPDFDictionaryRef SigField, SigDict;
	CGPDFStringRef     contentsStr;
	
	if(CGPDFArrayGetDictionary(SigArray, SigIndex, &SigField)) {
		
		if(CGPDFDictionaryGetDictionary(SigField, V, &SigDict)) {
			
			if(CGPDFDictionaryGetString(SigDict, PKCS7CONTENT, &contentsStr)) {
				
				NSData *dataret = [NSData dataWithBytes:CGPDFStringGetBytePtr(contentsStr) length:CGPDFStringGetLength(contentsStr)];
				
				return dataret;
			}
		}
	}
	
	return NULL;
}

-(CGPDFArrayRef) GetByteRange:(NSInteger)SigIndex
{
	CGPDFDictionaryRef SigField, SigDict;
	CGPDFArrayRef      byterange = NULL;
//	CGPDFInteger       temp_int;
	
	if(CGPDFArrayGetDictionary(SigArray, SigIndex, &SigField)) {
		
		if(CGPDFDictionaryGetDictionary(SigField, V, &SigDict)) {
			
			if(CGPDFDictionaryGetArray(SigDict, BYTERANGE, &byterange)) {
				
				return byterange;
			}			
			NSLog(@"ByteRange non trovato");
		}
		NSLog(@"SigDict non trovato");
	}
	NSLog(@"SigField non trovato");
	
	return NULL;
}


-(NSData*) GetPKCS7DetachedData:(NSInteger)SigIndex
{
	// Get the PDF byte range for the signature
	NSData *wholePDFBuffer  = [NSData dataWithContentsOfFile:self.thisPDFpath];
	NSMutableData *dataret  = [[NSMutableData alloc] initWithLength:0];
	CGPDFArrayRef byterange = [self GetByteRange:SigIndex];
	CGPDFInteger   byte1, byte2;
	
	for (int j=0; j<CGPDFArrayGetCount(byterange); j+=2) {
		
		CGPDFArrayGetInteger(byterange, j,   &byte1);
		CGPDFArrayGetInteger(byterange, j+1, &byte2);
		
		NSRange range = {byte1, byte2};		
		
		[dataret appendData:[wholePDFBuffer subdataWithRange: range]];
	}
	
	// Nooo altrimenti sempre il solito bad access
	// [wholePDFBuffer release];
	
	return (NSData *)dataret;
}


-(Boolean) Verify
{
	for (int i=0; i<SignatureCount; i++) 
	{
		if ([self VerifySignature:i] == FALSE) {
			return FALSE;
		}
	}
	
	return TRUE;
}

-(Boolean) VerifySignature:(NSInteger)SigIndex
{
	// First load the PKCS7 signature BLOB
	NSData *temp = [self GetDetachedPKCS7:SigIndex];
	unsigned long len = [temp length];
	unsigned char *p  = (unsigned char *)[temp bytes];
	
	// Se fai cosi, al solito... BAD ACCESS
	// [temp release];
	
	PKCS7 *p7 = d2i_PKCS7(NULL, &p, len);
	if (!p7) {
		NSLog(@"VerifySignature: Contents is not a valid pkcs7 object!");
		return FALSE;
	}
	
	// Load the data to be checked
	temp = [self GetPKCS7DetachedData:SigIndex];
	BIO *indata = BIO_new(BIO_s_mem());
	BIO_write(indata, (unsigned char *)[temp bytes], [temp length]);
	BIO_flush(indata);
	// [temp release];
	
	// Verify the signature
	Boolean ret = TRUE;
	if (PKCS7_verify(p7, NULL, CertStore, indata, NULL, 0) <= 0) {
		NSLog(@"VerifySignature: %s", ERR_error_string(ERR_get_error(), NULL));	
		ret = FALSE;
	}
	
	BIO_free(indata);
	PKCS7_free(p7);
	
	return ret;
}

+(NSData*) GenPKCS7:(NSData*)PDFBuffer WithCert:(NSString *)certPath UsingKey:(NSString *)privKeyPath withPIN:(NSString *)PIN;
{
	if (!certPath || !privKeyPath) {
		NSLog(@"GenDetachedPKCS7: Unable to load credential");
		return NULL;
	}
	
	// First load the X509 certificate
	const char *cpath = [certPath UTF8String];
	FILE *file = fopen(cpath, "rb");
    X509 *mycert = PEM_read_X509(file, NULL, NULL, NULL);
	fclose(file);
	
	// Check signature type
	SignAlgo signalg = Unknown_Signature;
	char alg[2048];
	BIO *bp = BIO_new(BIO_s_mem());
	X509_CINF *ci = mycert->cert_info;
	if (i2a_ASN1_OBJECT(bp, ci->signature->algorithm) > 0) {
		BIO_read(bp, alg, sizeof(alg));
	}
	BIO_free(bp);
	
	// Check for supported signature algorithm
	NSString *SignAlg = [NSString stringWithUTF8String:alg];
	if ([SignAlg rangeOfString:@"WithRSAEncryption"].location != NSNotFound) {
		signalg = RSA_Signature;
	}
	else if ([SignAlg rangeOfString:@"WithDSAEncryption"].location != NSNotFound){
		signalg = DSA_Signature;
	}
	else {
		signalg = Unknown_Signature;
		NSLog(@"GenDetachedPKCS7: Unsupported X509 signature algorithm for cert");
		return NULL;
	}
	
	// Now load the privatekey
	cpath = [privKeyPath UTF8String];
	file = fopen(cpath, "rb");
	unsigned char *ckey = NULL;
	if (PIN != nil) 
		ckey = (unsigned char *)[PIN UTF8String];
	
	// Setup the key
	EVP_PKEY *pkey = EVP_PKEY_new();
	switch (signalg) 
	{
		case RSA_Signature:
		{
			RSA *mykey = PEM_read_RSAPrivateKey(file, NULL, NULL, ckey);
			EVP_PKEY_set1_RSA(pkey, mykey);
			break;
		}
		case DSA_Signature:
		{
			DSA *mykey = PEM_read_DSAPrivateKey(file, NULL, NULL, ckey);
			EVP_PKEY_set1_DSA(pkey, mykey);
			break;
		}
		default:
		{
			// Never reacher i hope
			NSLog(@"GenDetachedPKCS7: key setup failure");
			return NULL;
			break;
		}
	}
	fclose(file);
	
	// Do the signature
	BIO *bio = BIO_new(BIO_s_mem());
	BIO_write(bio, [PDFBuffer bytes], [PDFBuffer length]);
	PKCS7 *pkcs7 = PKCS7_sign(mycert, pkey, NULL, bio, PKCS7_DETACHED | PKCS7_BINARY);
	if(!pkcs7) {
		NSLog(@"GenDetachedPKCS7: %s", ERR_error_string(ERR_get_error(), NULL));
		return NULL;
	}
	
  	// Free some memory
	BIO_free(bio);
	EVP_PKEY_free(pkey);
	X509_free(mycert);
	
	// Generate an adbe.pkcs7.detached compatible sgnature 
	// (...In this case, the value of Contents is a DER-encoded PKCS#7 binary data object containing the signature. 
	// No data is encapsulated in the PKCS#7 signed-data field...)
	unsigned char *buf, *p;
	int len;
	
	// Calculate the size
	len = i2d_PKCS7(pkcs7, NULL);
	buf = OPENSSL_malloc(len);
	if (!buf) {
		NSLog(@"GenDetachedPKCS7: FATAL OPENSSL_malloc returned NULL");
		return NULL;
	}
	
	// Do the conversion
	p = buf;
	i2d_PKCS7(pkcs7, &p);
	NSData *ret = [NSData dataWithBytes:buf length:len];
	OPENSSL_free(buf);

#ifdef DEBUG_ON
	//Save PEM PKCS7 on the disk for OpenSSL

	file = fopen("mypkcs7.pem", "w+");
	if (!file) {
		NSLog(@"parsePKCS7: Errore nella creazione del file\n");
		return NULL;
	}
	PEM_write_PKCS7(file, pkcs7);
	fclose(file);
#endif
	
	// Free PKCS7
	PKCS7_free(pkcs7);
	
	return ret;
}


-(Boolean*) Sign:(NSString *)certPath UsingKey:(NSString *)privKeyPath withPIN:(NSString *)PIN
{
	return TRUE;
}


-(CFURLRef) getPDFURL
{
	return thisPDFurl;
}


- (void)release 
{
    EVP_cleanup();
	[super release];
}

@end
