//
//  HashSec.h
//  MyPhotoMap
//
//  Created by Antonio De Marco on 02/07/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <openssl/pkcs7.h>

@interface OpenSSLHash : NSObject {

}
-(NSData *) SHA1:(NSData *)data;
@end
