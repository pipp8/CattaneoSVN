//
//  CommonSec.h
//  MyPhotoMap
//
//  Created by Antonio De Marco on 02/07/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface CommonSec : NSObject {

	
}

-(NSString *)base64EncodedStringWithData:(NSData *)data;
-(NSData *)dataByBase64DecodingString:(NSString *)decode;
-(NSString*)getHexStringWithData:(NSData *)data;
-(NSString *)replicateStr:(NSString *)strReplay number:(NSInteger)number;

- (void)add_all_algorithms;
- (void)cleanup;

@end
