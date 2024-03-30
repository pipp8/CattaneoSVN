#import "mongoose.h"

@interface MongooseWrapper : NSObject {
	
}



struct mg_context *ctx;

- (void)start:(NSString *)ports documentRoot:(NSString *)docRoot RecURI:(Boolean)recUri;
- (void)stop;
- (NSString *)getIPAddress;

@end