//
//  RemoteCommunication.h
//  iOTP
//
//  Created by Giuseppe Cattaneo on 21/04/15.
//  Copyright (c) 2015 Giuseppe Cattaneo. All rights reserved.
//

#ifndef iOTP_RemoteCommunication_h
#define iOTP_RemoteCommunication_h

#import <UIKit/UIKit.h>

#import "iOTPMisc.h"


typedef NS_OPTIONS(NSUInteger, RequestStatus) {
    ReqUndefined            = 0,
    LoginReq                = 1,
    OTPSMSRequest           = 2,
    OTPSMSRequested         = 3,
    OTPVerifyRequest        = 4,
    SeedRequest             = 5,
    SendConfirm             = 6,
    CloseRequest            = 7,
    Terminated              = 8,
    WrongOTP                = 9
};


@interface RemoteCommunication : NSObject

@property RequestStatus progressStatus;

@property BOOL serverFailure;

@property (nonatomic, strong) NSString *provisioningSrv;

@property (nonatomic, strong) NSString *OTPPrefix;

// protocollo NSURLConnection
- (void)connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)response;
- (void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data;

- (void)connection:(NSURLConnection *)connection didFailWithError:(NSError *)error;
- (void)connectionDidFinishLoading:(NSURLConnection *)connection;

// protocol methods
- (id) init;

- (void) test1: (id<RestartViewControllerDelegate>) callBackObj;
- (void) test2: (id<RestartViewControllerDelegate>) callBackObj;

- (void) openSession: (NSString *) user withPassword: (NSString *) password target: (id<RestartViewControllerDelegate>) callBack;

- (void) requestSMS ;

- (void) sendSMS: (NSString *) smsOTP target: (id<RestartViewControllerDelegate>) callBack;

- (void) getSeed;

// misc
- (void) resetCommunication;
- (NSDictionary *) parseCockies: (NSString *) line;
- (void) initErrorMessages;

@end

#endif
