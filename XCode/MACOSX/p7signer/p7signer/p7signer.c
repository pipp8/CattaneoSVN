//
//  main.c
//  p7signer
//
//  Created by Giuseppe Cattaneo on 07/12/12.
//  Copyright (c) 2012 Giuseppe Cattaneo. All rights reserved.
//
// $Id: p7signer.c 653 2014-02-07 18:24:20Z cattaneo $
//
 
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <sys/stat.h>

#include <openssl/ssl.h>
#include <openssl/evp.h>

#include <openssl/cms.h>
#include <openssl/err.h>

#include <openssl/ts.h>

#include <assert.h>

#define MAX_BUF_LEN 100000

unsigned char *getdata(char *filename, int *length);

int DoSign(const char * filename, const char * certPath, const char * privKeyPath, const char * passphrase);
int DoVerify(const char * filename);
int DoExtract(const char * filename, char * outfile);
int DoEncrypt(const char * filename, const char * receiptCertPath);
int DoDecrypt(const char * filename, const char * receiptPrivKeyPath, const char * receiptCertPath, const char * passphrase);
int DoVerifyTS(const char * filename, const char * TSAResponsePath, const char * TSACertPath);

int pass_cb(char *buf, int size, int rwflag, void *u);

void initOpenSSL();
void Usage( const char * prg);


const char * revision = {"$Id: p7signer.c 653 2014-02-07 18:24:20Z cattaneo $"};
const char * passphrase = NULL;

int main(int argc, const char * argv[])
{    
    char * outFile = NULL;
    const char * pf = NULL;
	const char * operation = NULL;
    
    if (argc < 2) {
        Usage(argv[0]);
    }
    
    initOpenSSL();

	operation = argv[1];
    
    if (strcmp( operation, "sign") == 0) {
        if (argc < 5 || argc > 6)
            Usage(argv[0]);

        if (argc > 5 )
            passphrase = argv[5];
        
        DoSign( argv[2], argv[3], argv[4], passphrase);
    }
    else if (strcmp(operation, "verify") == 0) {
        if (argc != 3)
            Usage(argv[0]);
        
        DoVerify( argv[2]);
    }
    else if (strcmp(operation, "extract") == 0) {
        if (argc < 3)
            Usage(argv[0]);
        else if (argc > 3)
            outFile = argv[3];
        
        DoExtract( argv[2], outFile);
    }
    else if (strcmp(operation, "encrypt") == 0) {
        if (argc != 4)
            Usage(argv[0]);
        
        DoEncrypt( argv[2], argv[3]);
    }
    else if (strcmp(operation, "decrypt") == 0) {
        if (argc < 4 || argc > 5)
            Usage(argv[0]);
        
        if (argc == 5 )
            passphrase = argv[4];
        
        DoDecrypt( argv[2], argv[3], NULL, passphrase);
    }
    else if (strcmp(operation, "verifyTS") == 0) {
        if (argc != 5)
            Usage(argv[0]);
        
        DoVerifyTS( argv[2], argv[3], argv[4]);
    }
    else {
        Usage(argv[0]);
    }
    ERR_free_strings();
}


void initOpenSSL() {
    OpenSSL_add_all_algorithms();
    OpenSSL_add_all_ciphers();
    OpenSSL_add_all_digests();
    ERR_load_crypto_strings();
}


/***
 * Get the data from file and return a malloced buffer and size.
 * This code does not need to be digested it simply returns the whole contents if data
 * from reading a file.
 **/
unsigned char *getdata(char *filename, int *length)
{
	unsigned char *b = NULL;
    FILE *fp = fopen(filename, "rb");
    long avail;
    
    *length = 0;
    if (fp == NULL){
        fprintf( stderr, "Get Data File %s error %d\n", filename, errno);
        return NULL;
    }
    
    fseek(fp, 0L, SEEK_END);
    avail = ftell(fp);
    fseek(fp, 0L, SEEK_SET);
    b = (unsigned char *) malloc(avail);
    if (fread ( b, 1, avail, fp) != avail){
        printf("INPUT Data File read error %d\n", errno);
        return NULL;
    }
    *length = (int) avail;
    fclose(fp);
    return b;
}



int DoSign(const char * filename, const char * certPath, const char * privKeyPath, const char * passphrase) {

    EVP_MD_CTX mdctx;
    const EVP_MD *md;
    unsigned char md_value[EVP_MAX_MD_SIZE];
    unsigned int md_len, i, length, len;
    char buf[MAX_BUF_LEN];
	struct stat stbuf;

	BIO *bio = NULL;
    FILE *file;
    char errbuf[200];

	X509 *cert = NULL;
    RSA *privKey = RSA_new();

    CMS_ContentInfo *cms;

	EVP_PKEY *pkey = NULL;

	BIO *outBio = NULL;

    int flags = 0;
    char outPath[1024];
    int  ret;

    if (stat( filename, &stbuf) < 0) {
        fprintf(stderr, "cannot stat input file: %s\n", filename);
        exit(-1);
    }
    
    if ((length = stbuf.st_size) <= 0) {
        fprintf(stderr, "zero size input file: %s\n", filename);
        exit(-1);
    }
    printf("INPUT file %s length %d \n", filename, length);
    
#if 0
    BIO *bio = BIO_new(BIO_s_file());

    if (BIO_read_filename(bio, filename) == 0) {
        fprintf(stderr, "Failed initialing BIO input data file %s\n", filename);
        return -1;
    }
#endif
    bio = BIO_new_file(filename, "r");
    if (bio == NULL) {
        fprintf(stderr, "Failed initialing BIO input data file %s\n", filename);
        return -1;
    }

    EVP_add_digest(EVP_sha256());        //load our algorithm
    md = EVP_get_digestbyname("SHA256");
    printf("DIGEST is SHA256\n");
    
    EVP_MD_CTX_init( &mdctx);
    EVP_DigestInit_ex( &mdctx, md, NULL);
    /* Read file in MAX_BUF_LEN chunks   and pass it to update functions */
    while ((len = BIO_read( bio, buf, MAX_BUF_LEN)) != 0)
        EVP_DigestUpdate( &mdctx, buf, len);   //use our file contents
    
    EVP_DigestFinal_ex( &mdctx, md_value, &md_len);
    EVP_MD_CTX_cleanup(&mdctx);
    
    //Okay now we have a Message Digest from the file
    printf("Digest1 is: ");
    for(i = 0; i < md_len; i++) printf("%02x", md_value[i]);
    printf("\n");
    
    
	
	file = fopen( certPath, "rb");
    if (file == NULL || fstat( fileno( file), &stbuf) < 0) {
        fprintf(stderr, "cannot stat certificate file: %s\n", certPath);
        exit(-1);
    }
    if (stbuf.st_size < 1024) {
        fprintf(stderr, "Empty certificate file (size = %ld)\n", (long) stbuf.st_size);
        exit(-1);
    }
    
	cert =  PEM_read_X509(file, NULL, pass_cb, NULL);
    fclose(file);

    if (cert == NULL) {
        ERR_error_string(ERR_get_error(), errbuf);
        fprintf(stderr, "Impossibile caricare il certificato specificato:\n%s\n", errbuf);
        return -1;
    }    
    fprintf(stderr, "File will be signed by: %s\n", X509_NAME_oneline(X509_get_subject_name(cert), 0, 0));

    file = fopen( privKeyPath, "rb");
    if (file == NULL || fstat( fileno( file), &stbuf) < 0) {
        fprintf(stderr, "cannot stat private key file: %s\n", privKeyPath);
        exit(-1);
    }
    if (stbuf.st_size < 1024) {
        fprintf(stderr, "Empty private key file (size = %ld)\n", (long) stbuf.st_size);
        exit(-1);
    }
    
    privKey = PEM_read_RSAPrivateKey(file, NULL, pass_cb, passphrase);
    fclose(file);

	if (privKey == NULL) {
        ERR_error_string(ERR_get_error(), errbuf);
        fprintf(stderr, "Impossibile caricare la chiave specificata\n%s\n", errbuf);
        return -1;
    }
        
	pkey = EVP_PKEY_new();
    EVP_PKEY_set1_RSA( pkey, privKey);
	flags = CMS_BINARY | CMS_PARTIAL;
    
    BIO_seek(bio, 0);   // si riposiziona all'inizio cf. digest

    
    cms = CMS_sign(NULL, NULL, NULL, bio, flags);
    
    if (cms == NULL) {
        ERR_error_string(ERR_get_error(), errbuf);
        fprintf(stderr, "cms_sign failure: %s\n", errbuf);
    }
    
    if (CMS_add1_signer(cms, cert, pkey, md, CMS_BINARY) == NULL) {
        ERR_error_string(ERR_get_error(), errbuf);
        fprintf(stderr, "cms_add_signer failure: %s\n", errbuf);
    }
    
    CMS_final(cms, bio, NULL, CMS_BINARY);
    
    BIO_free(bio);
    EVP_PKEY_free(pkey);

    sprintf( outPath, "%s.p7m", filename);

#ifdef PEM_ENCODING
    
    file = fopen( outPath, "wb");

    if (PEM_write_PKCS7(file, cms) > 0) {
        fprintf( stderr, "CMS Saved, outfile: %s\n", outPath);
        ret = 0;
    }
    else {
        fprintf( stderr, "Cannot save the pkcs#7 structure\n");
        ret = -1;
    }

    fclose(file);
    
#else

    outBio = BIO_new_file(outPath, "wb");
    if (outBio == NULL) {
        fprintf(stderr, "Failed initialing BIO for output PKCS#7 data file %s\n", filename);
        return -1;
    }

    if (i2d_CMS_bio_stream( outBio, cms, NULL, 0) > 0) {
        fprintf( stderr, "CMS Saved, outfile: %s\n", outPath);
    }
    else {
        fprintf( stderr, "Cannot save the cms DER format structure\n");
    }

    BIO_free( outBio);
#endif
    
    
    if (DoVerify( outPath) != 0)
        fprintf(stderr, "Verifica fallita\n");
    
    
    if (privKey != NULL) {
		privKey->n = NULL;
		privKey->e = NULL;
		privKey->d = NULL;
		privKey->p = NULL;
		privKey->q = NULL;
		RSA_free(privKey);
    }

    X509_free(cert);
    
    return ret;
}




int DoVerify(const char * filename) {
    
    char errbuf[200];
	struct stat stbuf;
    FILE *file;
    CMS_ContentInfo *cms = NULL;
    STACK_OF(CMS_SignerInfo) *sis = NULL;
    STACK_OF(X509) *certs = NULL;
    CMS_SignerInfo *signer;
    X509 *cert = NULL;

	BIO *inBio = NULL;

    int  ret = 0;
    
    int flags =  CMS_BINARY | CMS_NOVERIFY;
    
    if ((file = fopen(filename, "rb")) == NULL) {
        fprintf( stderr, "Cannot open PKCS7 Data File %s (error: %d)\n", filename, errno);
        exit(-1);
    }
    
#ifdef PEM_ENCODING
    
    if (PEM_read_CMS(file, &cms, NULL, NULL) == NULL) {
        ERR_error_string(ERR_get_error(), errbuf);
        fprintf( stderr, "Failed reading PKCS7 PEM Encoded input file %s (error: %s)\n", filename, errbuf);
        exit(-1);
    }

#else
    
    inBio = BIO_new_fp(file, 0);
    if (inBio == NULL) {
        ERR_error_string(ERR_get_error(), errbuf);
        fprintf(stderr, "Failed initialing BIO to read PKCS7 DER encoded input file %s (error: %s)\n", filename, errbuf);
        exit(-1);
    }

    if ( d2i_CMS_bio(inBio, &cms) == NULL) {
        ERR_error_string(ERR_get_error(), errbuf);
        fprintf( stderr, "Failed reading PKCS7 DER Encoded input file %s (error: %s)\n", filename, errbuf);
        exit(-1);
    }

#endif
    
    assert (cms != NULL); // should report/handle/etc errors
    
    if (CMS_verify(cms, NULL, NULL, NULL, NULL, flags) == 1) {
        fprintf(stderr, "Signature successfully verified\n");
        ret = 0;
    }
    else {
        fprintf(stderr, "Verify failed\n");
        ret = 1;
    }

    // assert (CMS_type_is_signed(cms)); // ditto maybe
    
    sis = CMS_get0_SignerInfos(cms);
    assert (sis != NULL); // should never fail
    
    assert (sk_CMS_SignerInfo_num(sis) >= 0); // or if allow multi-signed
    signer = sk_CMS_SignerInfo_value(sis, 0); // loop through and choose
    assert (signer != NULL); // should never fail
    

    // puo' essere chiamata solo dopo la cms_verify()
    certs = CMS_get0_signers(cms);
    assert (sk_X509_num(certs) >= 1); // or if allow multi-signed

    cert = sk_X509_value(certs, 0);
    assert (cert != NULL); // should never fail

    // or cheat and use STACK_OF(X509)* p7->d.sign->cert
    // and require only one, or loop through and look
    // assert(cert == sk_X509_value(cms->d.sign->cert,0));
    
    fprintf(stderr, "File signed by: %s\n", X509_NAME_oneline(X509_get_subject_name(cert), 0, 0));

    if (cms)
        CMS_ContentInfo_free(cms);
    cert = NULL; // sarà liberato dallo stack X509_free(cert);
    certs = NULL; // è stato liberato da ContentInfo.
    if (cert)
        X509_free(cert);
    if (certs)
        sk_X509_pop_free(certs, X509_free);
    if (inBio)
        BIO_free(inBio);

    return ret;
}



int DoExtract(const char * filename, char * outFile) {
    
    char errbuf[200];
    FILE *file;
    CMS_ContentInfo *cms = NULL;
    X509 *cert = NULL;
    STACK_OF(X509) *st = NULL;

	BIO *inBio = NULL;
	BIO *outBio = NULL;

    int ret;
    int flags =  CMS_BINARY | CMS_NOVERIFY;
    
    if ((file = fopen(filename, "rb")) == NULL) {
        fprintf( stderr, "Cannot get Data File %s error %d\n", filename, errno);
        exit(-1);
    }

#ifdef PEM_ENCODING
    
    if (PEM_read_CMS(file, &cms, NULL, NULL) == NULL) {
        ERR_error_string(ERR_get_error(), errbuf);
        fprintf( stderr, "Failed reading PKCS7 PEM Encoded input file %s (error: %s)\n", filename, errbuf);
        exit(-1);
    }
    
#else
    
    inBio = BIO_new_fp(file, 0);
    if (inBio == NULL) {
        ERR_error_string(ERR_get_error(), errbuf);
        fprintf(stderr, "Failed initialing BIO to read PKCS7 DER encoded input file %s (error: %s)\n", filename, errbuf);
        exit(-1);
    }
    
    if ( d2i_CMS_bio(inBio, &cms) == NULL) {
        ERR_error_string(ERR_get_error(), errbuf);
        fprintf( stderr, "Failed reading CMS DER Encoded input file %s (error: %s)\n", filename, errbuf);
        exit(-1);
    }

    BIO_free(inBio);
#endif
    
    fclose( file);
    
    assert (cms != NULL); // should report/handle/etc errors
        
    if (outFile == NULL) {
        outFile = malloc(strlen(filename));
        strcpy(outFile, filename);
        outFile[ strlen(filename) - 4] = '\0';
    }
    outBio = BIO_new_file(outFile, "wb");
    if (outBio == NULL) {
        fprintf(stderr, "Failed initialing BIO for output data file %s\n", outFile);
        return -1;
    }
    
    
    if (CMS_verify(cms, st, NULL, NULL, outBio, flags) == 1) {
        fprintf(stderr, "Signed Data successfully extracted\n");
        ret = 0;
    }
    else {
        X509_free(cert);
        fprintf(stderr, "Verify failed\n");
        ret = 1;
    }
    
    BIO_free(outBio);
    return ret;
}


int DoEncrypt(const char * filename, const char * receiptCertPath) {
    
    unsigned int i, length, len;
    char buf[MAX_BUF_LEN];
    
	struct stat stbuf;
    
    FILE *file;
	BIO *inBio = NULL;
	X509 *cert = NULL;
	STACK_OF(X509) *certs =  NULL;
    CMS_ContentInfo *cms;
	BIO *outBio = NULL;

    char errbuf[200];
	char outPath[1024];
    int  ret = 0;
	int flags = 0;
    
    if (stat( filename, &stbuf) < 0) {
        fprintf(stderr, "cannot stat input file: %s\n", filename);
        exit(-1);
    }
    
    if ((length = stbuf.st_size) <= 0) {
        fprintf(stderr, "zero size input file: %s\n", filename);
        exit(-1);
    }
    printf("INPUT file %s length %d \n", filename, length);
    
    inBio = BIO_new_file(filename, "rb");
    if (inBio == NULL) {
        fprintf(stderr, "Failed initialing BIO input data file %s\n", filename);
        return -1;
    }
    
	file = fopen( receiptCertPath, "rb");
    if (file == NULL || fstat( fileno( file), &stbuf) < 0) {
        fprintf(stderr, "cannot stat certificate file: %s\n", receiptCertPath);
        exit(-1);
    }
    if (stbuf.st_size < 1024) {
        fprintf(stderr, "Empty certificate file (size = %ld)\n", (long) stbuf.st_size);
        exit(-1);
    }
    
	cert =  PEM_read_X509(file, NULL, pass_cb, NULL);
    fclose(file);
    
    if (cert == NULL) {
        ERR_error_string(ERR_get_error(), errbuf);
        fprintf(stderr, "Impossibile caricare il certificato specificato:\n%s\n", errbuf);
        return -1;
    }
    fprintf(stderr, "File will be encrypted with public key of: %s\n", X509_NAME_oneline(X509_get_subject_name(cert), 0, 0));
    
           
    flags = CMS_BINARY;

    certs = sk_X509_new_null();
    sk_X509_push(certs, cert);

    cms = CMS_encrypt(certs, inBio, EVP_des_ede3_cbc(), flags);
    
    if (cms == NULL) {
        ERR_error_string(ERR_get_error(), errbuf);
        fprintf(stderr, "cms_encrypt failure: %s\n", errbuf);
    }
    
    CMS_final(cms, inBio, NULL, flags);
        
    sprintf( outPath, "%s.enc", filename);
    
#if PEM_ENCODING
    
    file = fopen( outPath, "wb");
    
    if (PEM_write_CMS(file, cms) > 0) {
        fprintf( stderr, "CMS Saved, outfile: %s\n", outPath);
        ret = 0;
    }
    else {
        fprintf( stderr, "Cannot save the CMS structure\n");
        ret = -1;
    }
    
    fclose(file);

#else
    
    outBio = BIO_new_file(outPath, "wb");
    if (outBio == NULL) {
        fprintf(stderr, "Failed initialing BIO for output PKCS#7 data file %s\n", filename);
        return -1;
    }
    
    if (i2d_CMS_bio_stream( outBio, cms, NULL, 0) > 0) {

        fprintf( stderr, "CMS Saved, outfile: %s\n", outPath);
    }
    else {
        ERR_error_string(ERR_get_error(), errbuf);
        fprintf( stderr, "Cannot save the cms DER format structure: %s\n", errbuf);
    }
    
    BIO_free( outBio);
#endif
    
    BIO_free(inBio);
    
    X509_free(cert);
    
    return ret;
}

int DoDecrypt(const char * filename, const char * receiptPrivKeyPath, const char * receiptCertPath, const char * pass) {
        
    char errbuf[200];
    char outPath[1024];
    int  ret = 0;
    int flags = CMS_BINARY;
    BIO * tbio = NULL;
	BIO *inBio = NULL;
	BIO *outBio = NULL;
    X509 * rcert = NULL;
    EVP_PKEY *rPrivKey = NULL;
    
    CMS_ContentInfo * cms = NULL;
    
    if (receiptCertPath != NULL) {
        tbio = BIO_new_file( receiptCertPath, "rb");
        rcert = PEM_read_bio_X509( tbio, NULL, 0, NULL);
        BIO_free( tbio);
    }
    
    tbio = BIO_new_file( receiptPrivKeyPath, "rb");
    rPrivKey = PEM_read_bio_PrivateKey( tbio, NULL, pass_cb, passphrase);
    
	if (rPrivKey == NULL) {
        ERR_error_string(ERR_get_error(), errbuf);
        fprintf(stderr, "Impossibile caricare la chiave specificata\n%s\n", errbuf);
        return -1;
    }
    
#if PEM_ENCODING
    
    FILE *fileIn;
    
    if ((fileIn = fopen(filename, "rb")) == NULL) {
        fprintf( stderr, "Cannot get Data File %s error %d\n", filename, errno);
        exit(-1);
    }

    if (PEM_read_CMS(fileIn, &cms, NULL, NULL) == NULL) {
        ERR_error_string(ERR_get_error(), errbuf);
        fprintf( stderr, "Failed reading CMS PEM Encoded input file %s (error: %s)\n", filename, errbuf);
        exit(-1);
    }
 
    fclose(fileIn);
    
#else
    
    inBio = BIO_new_file(filename, "r");
    if (inBio == NULL) {
        ERR_error_string(ERR_get_error(), errbuf);
        fprintf(stderr, "Failed initialing BIO to read CMS DER encoded input file %s (error: %s)\n", filename, errbuf);
        exit(-1);
    }
    
    if ( d2i_CMS_bio(inBio, &cms) == NULL) {
        ERR_error_string(ERR_get_error(), errbuf);
        fprintf( stderr, "Failed reading CMS DER Encoded input file %s (error: %s)\n", filename, errbuf);
        exit(-1);
    }
        
#endif
    
    assert(cms != NULL);
    
    sprintf( outPath, "%s.dec", filename);
    
    outBio = BIO_new_file(outPath, "wb");
    if (outBio == NULL) {
        fprintf(stderr, "Failed initialing BIO for output data file %s\n", filename);
        return -1;
    }
    
    if (!CMS_decrypt_set1_pkey(cms, rPrivKey, rcert)) {
        fprintf(stderr, "Error decrypting CMS using private key\n");
        return -1;
    }
    
    if (!CMS_decrypt(cms, NULL, NULL, NULL, outBio, flags)) {
        ERR_error_string(ERR_get_error(), errbuf);
        fprintf(stderr, "Error decrypting CMS structure: %s\n", errbuf);
        return -1;
    }

//    if (!CMS_data(cms, outBio, flags)) {
//        ERR_error_string(ERR_get_error(), errbuf);
//        fprintf(stderr, "Error extracting decrypted data from CMS structure: %s\n", errbuf);
//        ret =  -1;
//    }

    if (cms)
        CMS_ContentInfo_free(cms);

    BIO_free(inBio);

    BIO_free( outBio);

    if (rcert)
        X509_free(rcert);
    
    return ret;
}


// inizio verifica TS

static X509_STORE *create_cert_store(char *ca_path, char *ca_file)
{
	X509_STORE *cert_ctx = NULL;
	X509_LOOKUP *lookup = NULL;
	int i;
    
	/* Creating the X509_STORE object. */
	cert_ctx = X509_STORE_new();
    
	/* Setting the callback for certificate chain verification. */
	// X509_STORE_set_verify_cb(cert_ctx, verify_cb);
    
	/* Adding a trusted certificate directory source. */
	if (ca_path)
    {
		lookup = X509_STORE_add_lookup(cert_ctx, X509_LOOKUP_hash_dir());
		if (lookup == NULL)
        {
			fprintf(stderr, "memory allocation failure\n");
			goto err;
        }
		i = X509_LOOKUP_add_dir(lookup, ca_path, X509_FILETYPE_PEM);
		if (!i)
        {
			fprintf(stderr, "Error loading directory %s\n", ca_path);
			goto err;
        }
    }
    
	/* Adding a trusted certificate file source. */
	if (ca_file)
    {
		lookup = X509_STORE_add_lookup(cert_ctx, X509_LOOKUP_file());
		if (lookup == NULL)
        {
			fprintf( stderr, "memory allocation failure\n");
			goto err;
        }
		i = X509_LOOKUP_load_file(lookup, ca_file, X509_FILETYPE_PEM);
		if (!i)
        {
			fprintf(stderr, "Error loading file %s\n", ca_file);
			goto err;
        }
    }
    
	return cert_ctx;
err:
	X509_STORE_free(cert_ctx);
	return NULL;
}


static TS_VERIFY_CTX *create_verify_ctx(char *data, char *ca_path, char *ca_file,
                                        char *untrusted)
{
	TS_VERIFY_CTX *ctx = NULL;
	BIO *input = NULL;
	TS_REQ *request = NULL;
	int ret = 0;
    
	if (data != NULL)
    {
		if (!(ctx = TS_VERIFY_CTX_new())) goto err;
		ctx->flags = TS_VFY_VERSION | TS_VFY_SIGNER;
		if (data != NULL)
        {
			ctx->flags |= TS_VFY_DATA;
			if (!(ctx->data = BIO_new_file(data, "rb"))) goto err;
        }
    }
	else
		return NULL;
    
	/* Add the signature verification flag and arguments. */
	ctx->flags |= TS_VFY_SIGNATURE;
    
	/* Initialising the X509_STORE object. */
	if (!(ctx->store = create_cert_store(ca_path, ca_file))) goto err;
    
	/* Loading untrusted certificates. */
	if (untrusted && !(ctx->certs = TS_CONF_load_certs(untrusted)))
		goto err;
    
	ret = 1;
err:
	if (!ret)
    {
		TS_VERIFY_CTX_free(ctx);
		ctx = NULL;
    }
	BIO_free_all(input);
	TS_REQ_free(request);
	return ctx;
}



int DoVerifyTS(const char * filename, const char * TSAResponsePath, const char * TSACertPath)
{
    BIO *in_bio = NULL;
	TS_RESP *response = NULL;
	TS_VERIFY_CTX *verify_ctx = NULL;
    TS_TST_INFO *tst_info = NULL;
	PKCS7 *token = NULL;
    
	int ret = 0;
    
    BIO *out = NULL;
    
    if ((out = BIO_new_fp(stdout, 0)) == NULL)
        goto end;
    
	/* Decode the token (PKCS7) or response (TS_RESP) files. */
	if (!(in_bio = BIO_new_file(TSAResponsePath, "rb"))) goto end;

    //if (!(token = d2i_PKCS7_bio(in_bio, NULL))) goto end;

    if (!(response = d2i_TS_RESP_bio(in_bio, NULL))) goto end;
    
    // token = TS_RESP_get_token(response);
	tst_info = TS_RESP_get_tst_info(response);

    // if (!TS_check_status_info(response)) {
    //    printf("Bad Status Info\n");
    //    goto end;
    //}
    TS_TST_INFO_print_bio(out, tst_info);
    
    if ((verify_ctx = create_verify_ctx(filename, NULL, TSACertPath, NULL)) == NULL)
        goto end;
	
    // ret = TS_RESP_verify_token(verify_ctx, token) :
    ret = TS_RESP_verify_response(verify_ctx, response);
    
end:
	printf("Verification: ");
	if (ret)
		printf("OK\n");
	else
    {
		printf("FAILED\n");
		/* Print errors, if there are any. */
		// ERR_print_errors(bio_err);
    }
	
	/* Clean up. */
	BIO_free_all(in_bio);
	// PKCS7_free(token);
	TS_RESP_free(response);
	TS_VERIFY_CTX_free(verify_ctx);
	return ret;

}


int pass_cb(char *buf, int size, int rwflag, void *u)
{
    int len;
    if (passphrase != NULL) {
        len = strlen(passphrase);
        /* if too long, truncate */
        if (len > size)
            len = size;
        
        memcpy(buf, passphrase, len+1);
        return len;
    }
    else
        return 0;
}

void Usage( const char * prg)
{
    fprintf(stderr, "Revision: %s\n", revision);
    fprintf(stderr, "\nUsage:\n\t%s sign datafile signerCert signerPrivateKey [passphrase] |\n\
            \t%s verify pkcs7-file\n\
            \t%s extract pkcs7-file [content-filename] |\n\
            \t%s encrypt pkcs7-file receiptCert[...]\n\
            \t%s decrypt pkcs7-file receiptPrivKey [passphrase] |\n\
            \t%s verifyTS pkcs7-file TSAResponse TSACert\n", prg, prg, prg, prg, prg, prg);
    exit(-1);
}




