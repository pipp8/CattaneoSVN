//
//  OPENSSLTSP.m
//  MyPhotoMap
//
//  Created by Antonio De Marco on 13/07/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "OpenSSLTSP.h"
#import <cms.h>

@implementation OpenSSLTSP
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

-(NSData *)CreateRequestWithHash:(NSData *)hash HashName:(NSString *)hashname Policy:(NSString *)policy nonce:(long int)nonce CertRequired:(Boolean)certRequired {
  
	int cert = 0;
	
	if (certRequired)
		cert = 1;
	
	TS_REQ *ts_req = NULL;
	unsigned char *request = NULL;
	int request_len = 0;

	if ((ts_req = TS_REQ_new()) == NULL) {
	    return nil;
	}
	
	
	if (!TS_REQ_set_version(ts_req, 1)) {
		TS_REQ_free(ts_req);
		ts_req = NULL;
		return nil;
	}
	
	
	TS_MSG_IMPRINT *msg_imprint = NULL;
	if ((msg_imprint = TS_MSG_IMPRINT_new()) == NULL) {
		TS_REQ_free(ts_req);
		ts_req = NULL;
		return nil;
	}
	
	
	X509_ALGOR *algo = NULL;
	if ((algo = X509_ALGOR_new()) == NULL) {
		TS_REQ_free(ts_req);
		ts_req = NULL;
		TS_MSG_IMPRINT_free(msg_imprint);
		msg_imprint = NULL;
		return nil;
	}
	
	if (!(algo->algorithm = OBJ_txt2obj([hashname UTF8String] , 0))) {
		TS_REQ_free(ts_req);
		ts_req = NULL;
		TS_MSG_IMPRINT_free(msg_imprint);
		msg_imprint = NULL;
		X509_ALGOR_free(algo);
		algo = NULL;
		return nil;
	}
	
	if (!(algo->parameter = ASN1_TYPE_new())) {
		TS_REQ_free(ts_req);
		ts_req = NULL;
		TS_MSG_IMPRINT_free(msg_imprint);
		msg_imprint = NULL;
		X509_ALGOR_free(algo);
		algo = NULL;
		return nil;
	}
	
	algo->parameter->type = V_ASN1_NULL;
	
	if (!TS_MSG_IMPRINT_set_algo(msg_imprint, algo)) {
		TS_REQ_free(ts_req);
		ts_req = NULL;
		TS_MSG_IMPRINT_free(msg_imprint);
		msg_imprint = NULL;
		X509_ALGOR_free(algo);
		algo = NULL;
		return nil;
	}
	
	
	if (!TS_MSG_IMPRINT_set_msg(msg_imprint, (unsigned char*)[hash bytes], [hash length])) {
		TS_REQ_free(ts_req);
		ts_req = NULL;
		TS_MSG_IMPRINT_free(msg_imprint);
		msg_imprint = NULL;
		X509_ALGOR_free(algo);
		algo = NULL;
		return nil;
	}
	
	
	if (!TS_REQ_set_msg_imprint(ts_req, msg_imprint)) {
		TS_REQ_free(ts_req);
		ts_req = NULL;
		TS_MSG_IMPRINT_free(msg_imprint);
		msg_imprint = NULL;
		X509_ALGOR_free(algo);
		algo = NULL;
		return nil;
	}
	
	
	ASN1_OBJECT *policy_obj = NULL;
	if (!([policy length]==0)) {
		
		if ((policy_obj = OBJ_txt2obj([policy UTF8String], 0)) == NULL) {
			TS_REQ_free(ts_req);
			ts_req = NULL;
			TS_MSG_IMPRINT_free(msg_imprint);
			msg_imprint = NULL;
			X509_ALGOR_free(algo);
			algo = NULL;
			ASN1_OBJECT_free(policy_obj);
			policy_obj = NULL;
			return nil;
		}
		
		if (!TS_REQ_set_policy_id(ts_req, policy_obj)) {
			TS_REQ_free(ts_req);
			ts_req = NULL;
			TS_MSG_IMPRINT_free(msg_imprint);
			msg_imprint = NULL;
			X509_ALGOR_free(algo);
			algo = NULL;
			ASN1_OBJECT_free(policy_obj);
			policy_obj = NULL;
			return nil;
		}
		
	}
	

	if (nonce != 0) {
		
		ASN1_INTEGER *asn_nonce = NULL;
		if ((asn_nonce = ASN1_INTEGER_new()) == NULL) {
			TS_REQ_free(ts_req);
			ts_req = NULL;
			TS_MSG_IMPRINT_free(msg_imprint);
			msg_imprint = NULL;
			X509_ALGOR_free(algo);
			algo = NULL;
			ASN1_OBJECT_free(policy_obj);
			policy_obj = NULL;
			return nil;
		}
		
		if (!ASN1_INTEGER_set(asn_nonce, nonce)) {
			TS_REQ_free(ts_req);
			ts_req = NULL;
			TS_MSG_IMPRINT_free(msg_imprint);
			msg_imprint = NULL;
			X509_ALGOR_free(algo);
			algo = NULL;
			ASN1_OBJECT_free(policy_obj);
			policy_obj = NULL;
			return nil;
		}
		
		if (!TS_REQ_set_nonce(ts_req, asn_nonce)) {
			TS_REQ_free(ts_req);
			ts_req = NULL;
			TS_MSG_IMPRINT_free(msg_imprint);
			msg_imprint = NULL;
			X509_ALGOR_free(algo);
			algo = NULL;
			ASN1_OBJECT_free(policy_obj);
			policy_obj = NULL;
			ASN1_INTEGER_free(asn_nonce);
			asn_nonce = NULL;
			return(0);
		}
		
		ASN1_INTEGER_free(asn_nonce);
		asn_nonce = NULL;
		
	}
	
	
	if (!TS_REQ_set_cert_req(ts_req, cert)) {
		TS_REQ_free(ts_req);
		ts_req = NULL;
		TS_MSG_IMPRINT_free(msg_imprint);
		msg_imprint = NULL;
		X509_ALGOR_free(algo);
		algo = NULL;
		ASN1_OBJECT_free(policy_obj);
		policy_obj = NULL;
		return nil;
	}
	
	
	BIO *bio = NULL;
	if ((bio = BIO_new(BIO_s_mem())) == NULL) {
		TS_REQ_free(ts_req);
		ts_req = NULL;
		TS_MSG_IMPRINT_free(msg_imprint);
		msg_imprint = NULL;
		X509_ALGOR_free(algo);
		algo = NULL;
		ASN1_OBJECT_free(policy_obj);
		policy_obj = NULL;
		return nil;
	}
	
	if (!i2d_TS_REQ_bio(bio, ts_req)) {
		TS_REQ_free(ts_req);
		ts_req = NULL;
		TS_MSG_IMPRINT_free(msg_imprint);
		msg_imprint = NULL;
		X509_ALGOR_free(algo);
		algo = NULL;
		ASN1_OBJECT_free(policy_obj);
		policy_obj = NULL;
		BIO_free(bio);
		return nil;
	}
	
	
	BUF_MEM *bptr = NULL;
	BIO_get_mem_ptr(bio, &bptr);
	BIO_set_close(bio, BIO_NOCLOSE);
	BIO_free(bio);
	
	request = (unsigned char *) malloc(bptr->length);
	if (request == NULL) {
		return nil;
	}
	memcpy(request, bptr->data, bptr->length);
	request_len = bptr->length;
	
	TS_REQ_free(ts_req);
	ts_req = NULL;
	TS_MSG_IMPRINT_free(msg_imprint);
	msg_imprint = NULL;
	X509_ALGOR_free(algo);
	algo = NULL;
	ASN1_OBJECT_free(policy_obj);
	policy_obj = NULL;

	
	NSData *datarequest= [NSData dataWithBytes:request length:request_len];
	free(request);
	return datarequest;
}

-(NSDate *)getDateWithResponse:(NSData *)response {
	TS_RESP *ts_resp = NULL;
	BIO *bio = NULL;
    
	
	if ((bio = BIO_new(BIO_s_mem())) == NULL) {
		return nil;
	}
	

	
	
	if (BIO_write(bio, [response bytes], [response length]) != [response length]) {
		BIO_free(bio);
		return nil;
	}
	
	ts_resp = d2i_TS_RESP_bio(bio, NULL);
	if (ts_resp == NULL) {
		BIO_free(bio);
		return nil;
	}
	
	TS_TST_INFO *tst_info = TS_RESP_get_tst_info(ts_resp);
	
	ASN1_GENERALIZEDTIME *timegen =ASN1_GENERALIZEDTIME_new();
	
	timegen = TS_TST_INFO_get_time(tst_info);
	
	

	if (timegen == NULL) {
		ASN1_GENERALIZEDTIME_free(timegen);
		TS_TST_INFO_free(tst_info);
		ts_resp = NULL;
		BIO_free(bio);
		TS_RESP_free(ts_resp);
		return nil;
	}	
	
	unsigned char *asn1time = (unsigned char *)malloc(timegen->length);
	asn1time = timegen->data;

	NSString *dataStr = [[NSString alloc] initWithUTF8String:asn1time];
	NSString *dataStrSub = [dataStr substringToIndex:timegen->length-1];
	
	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    NSTimeZone *timeZone = [NSTimeZone timeZoneWithName:@"UTC"];
    [dateFormatter setTimeZone:timeZone];
    [dateFormatter setDateFormat:@"yyyyMMddHHmmss"];
	NSDate *date = [dateFormatter dateFromString:dataStrSub];
	[dateFormatter release];
	
	TS_RESP_free(ts_resp);
	BIO_free(bio);
	[dataStr release];
	return date;

} 

-(NSData *)getMessageImprintWithResponse:(NSData *)response {
	TS_RESP *ts_resp = NULL;
	BIO *bio = NULL;
	BIO *bio2 = NULL;
	
	if ((bio = BIO_new(BIO_s_mem())) == NULL) {
		return nil;
	}
	

	
	if (BIO_write(bio, [response bytes], [response length]) != [response length]) {
		BIO_free(bio);
		return nil;
	}
	
	ts_resp = d2i_TS_RESP_bio(bio, NULL);
	if (ts_resp == NULL) {
		BIO_free(bio);
		return nil;
	}
	
	TS_TST_INFO *tst_info = TS_RESP_get_tst_info(ts_resp);
	
	TS_MSG_IMPRINT *msg_imprint = TS_TST_INFO_get_msg_imprint(tst_info);
	
	if (msg_imprint == NULL) {
		TS_RESP_free(ts_resp);
		TS_MSG_IMPRINT_free(msg_imprint);
		TS_TST_INFO_free(tst_info);
		ts_resp = NULL;
		BIO_free(bio);
		return nil;
	}
	
	
	if ((bio2 = BIO_new(BIO_s_mem())) == NULL) {
		TS_RESP_free(ts_resp);
		TS_MSG_IMPRINT_free(msg_imprint);
		TS_TST_INFO_free(tst_info);
		ts_resp = NULL;
		BIO_free(bio);
		return nil;
	}
	
	i2d_TS_MSG_IMPRINT_bio(bio2, msg_imprint);
	
	unsigned char *biodata = (unsigned char *) malloc(256);
	
	if (!BIO_read(bio2, biodata, 256)) {
		TS_RESP_free(ts_resp);
		TS_MSG_IMPRINT_free(msg_imprint);
		TS_TST_INFO_free(tst_info);
		ts_resp = NULL;
		BIO_free(bio);
		BIO_free(bio2);
		return nil;   
	
	}
		
    NSData *dataresponse= [NSData dataWithBytes:biodata length:256];
		
	TS_TST_INFO_free(tst_info);
	ts_resp = NULL;
	free(biodata);
	BIO_free(bio);
	BIO_free(bio2);
	TS_RESP_free(ts_resp);
	return dataresponse;
		
} 


-(TSPResponseStatus) verifyStatusWithResponse:(NSData *)response {
	TS_RESP *ts_resp = NULL;
	BIO *bio = NULL;
	
	if ((bio = BIO_new(BIO_s_mem())) == NULL) {
		return(-1);
	}
	
	if (BIO_write(bio, [response bytes], [response length]) != [response length]) {
		BIO_free(bio);
		return(-1);
	}
	
	ts_resp = d2i_TS_RESP_bio(bio, NULL);
	if (ts_resp == NULL) {
		TS_RESP_free(ts_resp);
		ts_resp = NULL;
		BIO_free(bio);
		return tspResponseNotParsable;
	}
	
	TS_STATUS_INFO *status_info = NULL;
	status_info = TS_RESP_get_status_info(ts_resp);
	
	int rv = 0;
	
	long int status = ASN1_INTEGER_get(status_info->status);

	switch (status) {
		case 0:
			// Stato "Granted"
			rv = tspGranted;
			break;
		case 1:
			// Stato "Granted with modifications"
			rv = tspGrantedWithModification;
			break;
		case 2:
			rv = tspRejected;
			break;
		case 3:
			rv = tspWaiting;
			break;
		case 4:
			rv = tspRevocationWarning;
			break;
		case 5:
			rv = tspRevoked;
			break;
		default:
			rv = tspWrongStatusInfo;
	}
	
	TS_RESP_free(ts_resp);
	ts_resp = NULL;
	BIO_free(bio);
	
	return rv;
	
}

-(PKCS7 *)getTokenWithResponse:(NSData *)response {
	TS_RESP *ts_resp = NULL;
	BIO *bio = NULL;
	
	
	if ((bio = BIO_new(BIO_s_mem())) == NULL) {
		return nil;
	}
	
	if (BIO_write(bio, [response bytes], [response length]) != [response length]) {
		BIO_free(bio);
		return nil;
	}
	
	ts_resp = d2i_TS_RESP_bio(bio, NULL);
	if (ts_resp == NULL) {
		BIO_free(bio);
		return nil;
	}
		
	
    PKCS7 *pkcs7 = TS_RESP_get_token(ts_resp);
    	
	TS_RESP_free(ts_resp);
	ts_resp = NULL;
	BIO_free(bio);
	return pkcs7;
}

-(Boolean)verifySignatureWithResponse:(NSData *)responseData Request:(NSData *)requestData Cert:(X509 *)cert CACert:(X509 *)cacert {
	
	TS_RESP *ts_resp = NULL;
	TS_REQ *ts_req = NULL;
	BIO *bio = NULL;
	BIO *bio2 = NULL;
	TS_VERIFY_CTX *verify_ctx = NULL;
	X509_STORE *cert_ctx = NULL;
	PKCS7 *token = NULL;
	
	if ((bio = BIO_new(BIO_s_mem())) == NULL) {
		return NO;
	}
	
	if ((bio2 = BIO_new(BIO_s_mem())) == NULL) {
		BIO_free(bio);
		return NO;
	}
	
	if (BIO_write(bio, [responseData bytes], [responseData length]) != [responseData length]) {
		BIO_free(bio);
		BIO_free(bio2);
		return NO;
	}
	
	if (BIO_write(bio2, [requestData bytes], [requestData length]) != [requestData length]) {
		BIO_free(bio);
		BIO_free(bio2);
		return NO;
	}	
	
	ts_resp = d2i_TS_RESP_bio(bio, NULL);
	
	
	if (ts_resp == NULL) {
		BIO_free(bio);
		BIO_free(bio2);
		return NO;
	}
	
	ts_req = d2i_TS_REQ_bio(bio2, NULL);
	
	if (ts_req== NULL) {
		BIO_free(bio);
		BIO_free(bio2);
		TS_RESP_free(ts_resp);
		return NO;
	}
	
	
	verify_ctx = TS_REQ_to_TS_VERIFY_CTX(ts_req, NULL);
	
	
	cert_ctx = X509_STORE_new();
	
	verify_ctx->flags |= TS_VFY_SIGNATURE;
	
	if (cacert != nil) 
		X509_STORE_add_cert(cert_ctx, cacert);
	
	
	if (cert != nil) 
		X509_STORE_add_cert(cert_ctx, cert);
	
	verify_ctx->store = cert_ctx;
	
	token =  TS_RESP_get_token(ts_resp); 
	int ver1 = TS_RESP_verify_token(verify_ctx, token);
	
	int ver2 = TS_RESP_verify_response(verify_ctx, ts_resp);
    
    int ver3 = PKCS7_verify(token, NULL, cert_ctx, NULL, NULL, CMS_NO_SIGNER_CERT_VERIFY);

    //Verifica la Marca Temporale
    if (ver3 <= 0) {
        NSLog(@"VerifySignature error"); //: %s", ERR_error_string(ERR_get_error(), NULL));
    }

	
	TS_RESP_free(ts_resp);
	TS_REQ_free(ts_req);
	ts_resp = NULL;
	BIO_free(bio);
	BIO_free(bio2);
	X509_STORE_free(cert_ctx);
	
	
    // if ((ver1 <= 0) || (ver2 <=0) || ver3 <= 0)
    if (ver3 <= 0)
		return NO;
	
	
	return YES;
}


+(int) dmTSVerifyDigest:(NSData *)digest tsRespons:(NSData *)derResponse certStore:(X509_STORE *)store
{
    BIO *in_bio = NULL;
    PKCS7 *token = NULL;
    TS_RESP *response = NULL;
    TS_VERIFY_CTX *ctx = NULL;
    int ret = 0;
    
    // [CommonSec initOpenSSL];
    
    in_bio = BIO_new(BIO_s_mem());
    BIO_write(in_bio, [derResponse bytes], [derResponse length]);
    
    
    if (false) {
        if (!(token = d2i_PKCS7_bio(in_bio, NULL))) goto end;
    }
    else {
        if (!(response = d2i_TS_RESP_bio(in_bio, NULL))) goto end;
    }
    
    if (!(ctx = TS_VERIFY_CTX_new()))
        goto end;
    
    ctx->flags = TS_VFY_VERSION | TS_VFY_SIGNER;
    ctx->flags |= TS_VFY_IMPRINT;
    ctx->imprint = (unsigned char*)[digest bytes];
    ctx->imprint_len = [digest length];
    
    /* Add the signature verification flag and arguments. */
    ctx->flags |= TS_VFY_SIGNATURE;
    ctx->store = store;
    
    /* Checking the token or response against the request. */
    ret = false ?
    TS_RESP_verify_token(ctx, token) :
    TS_RESP_verify_response(ctx, response);
    
end:
    NSLog(@"Verification: ");
    if (ret) {
        NSLog(@"OK\n");
    }
    else {
        NSLog(@"FAILED\n");
    }
    
    /* Clean up. */
    if (token) {
        PKCS7_free(token);
    }
    if (response) {
        TS_RESP_free(response);
        response = NULL;
    }
    BIO_free_all(in_bio);
    //TS_VERIFY_CTX_free(ctx);
    
    // [CommonSec initOpenSSL];
    
    return ret;
}

//
//
//
//+(NSDictionary *) getTimeStampTokenInfo:(NSString *)pkcs7Filename isTSDFile:(Boolean)isTsd
//{
//    NSDictionary *ret = NULL;
//    
//    //Inizializza OPESSL
//    [CommonSec initOpenSSL];
//    
//    // Carico preventivamente lo store dei certificati per le eventuali verifiche
//    X509_STORE *store = [[dmDataModel sharedModel] getCertStore];
//    STACK_OF(X509) *other = NULL;
//    
//    NSMutableDictionary *tempDict = [NSMutableDictionary dictionaryWithCapacity:15];
//    
//    //Timestamp
//    PKCS7 *timeToken = NULL;
//    
//    BIO *b=BIO_new(BIO_s_file());
//    if (BIO_read_filename(b, [pkcs7Filename UTF8String]) <= 0) {
//        DMLog(@"BIO_read_filename failed");
//        goto end;
//    }
//    
//    timeToken = d2i_PKCS7_bio(b, NULL);
//    if (!timeToken) {
//        DMLog(@"BIO_read_filename failed");
//        goto end;
//    }
//    
//    //Provo a prendre il certificato della TSA
//    X509 *tsaCert = [OpenSSLCMS getSignerCertXBin:timeToken index:0];
//    if (tsaCert) {
//        //Fabio
//        //Non fare la freee del certificato qui non hai allocato nulla!
//        //Salvo il certificato in B64
//        [tempDict setObject:[OpenSSLX509 getb64String:tsaCert] forKey:DM_TS_INFO_B64_CERT];
//    }
//    
//    //Vediamo se la struttura è corretta e prendiamo la data
//    //la PKCS7_to_TS_TST_INFO fa sto tipo di controlli
//    NSDate* timeStamp = [OpenSSLTSP getDateWithToken:timeToken];
//    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
//    [formatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
//    [formatter setDateFormat: @"dd/MM/yyyy HH:mm:ss zzz"];
//    NSString *stringFromDate = [formatter stringFromDate:timeStamp];
//    [formatter release];
//    if (stringFromDate)
//        [tempDict setObject:stringFromDate forKey:DM_TS_INFO_TIMESTAMP];
//    else {
//        [tempDict setObject:@"-" forKey:DM_TS_INFO_TIMESTAMP];
//    }
//    
//    //Prendiamo tutte le info dal token
//    NSString *TimeToken_INFO = [OpenSSLTSP getInfoWithToken:timeToken];
//    NSArray  *temp_array = [TimeToken_INFO componentsSeparatedByString:@"**dikemobile**"];
//    
//    NSString *PolicyOID = [[temp_array objectAtIndex:1] stringByTrimmingCharactersInSet:[NSCharacterSet newlineCharacterSet]];
//    [tempDict setObject:PolicyOID forKey:DM_TS_POLICY_OID];
//    
//    [tempDict setObject:[temp_array objectAtIndex:2] forKey:DM_TS_INFO_SERIALNO];
//    
//    NSString *Tsa = [temp_array objectAtIndex:3];
//    unsigned long y = [Tsa rangeOfString:@"/CN="].location;
//    if (y != NSNotFound) {
//        Tsa = [Tsa substringFromIndex:y+4];
//        [tempDict setObject:Tsa forKey:DM_TS_INFO_TSA];
//    }
//    else {
//        y = [Tsa rangeOfString:@"URI:"].location;
//        if (y != NSNotFound) {
//            Tsa = [Tsa substringFromIndex:y+4];
//            [tempDict setObject:Tsa forKey:DM_TS_INFO_TSA];
//        }
//        else {
//            [tempDict setObject:Tsa forKey:DM_TS_INFO_TSA];
//        }
//    }
//    
//    //Verifica la Marca Temporale
//    if (PKCS7_verify(timeToken, NULL, store, NULL, NULL, CMS_NO_SIGNER_CERT_VERIFY) <= 0) {
//        DMLog(@"VerifySignature: %s", ERR_error_string(ERR_get_error(), NULL));
//        [tempDict setObject:DIKEMOBILE_STR_FAIL forKey:DM_TS_INFO_RESULT];
//        goto end;
//    }
//    
//    [tempDict setObject:DIKEMOBILE_STR_OK forKey:DM_TS_INFO_RESULT];
//    if (isTsd) {
//        [tempDict setObject:DM_TYPE_P7_TIMESTAMP forKey:DM_SIG_TYPE];
//    }
//    else {
//        [tempDict setObject:DM_TYPE_CADES_T_TS forKey:DM_SIG_TYPE];
//    }
//    
//    ret = [NSDictionary dictionaryWithDictionary:tempDict];
//    
//end:
//    if (timeToken) PKCS7_free(timeToken);
//    if (other) sk_X509_pop_free(other, X509_free);
//    if (b) BIO_free(b);
//    
//    //Clean OPESSL
//    [CommonSec cleanOpenSSL];
//    
//    return ret;
//}
//


/*
-(void)release {
  //  EVP_cleanup();
	[super release];

}
*/
@end
