
#import "MongooseWrapper.h"
#include <ifaddrs.h> 
#include <arpa/inet.h>


@implementation MongooseWrapper

NSString *documentRoot;

/*
 * This callback is attached to the URI "/post"
 * It uploads file from a client to the server. This is the demostration
 * of how to use POST method to send lots of data from the client.
 * The uploaded file is saved into "uploaded.txt".
 * This function is called many times during single request. To keep the
 * state (how many bytes we have received, opened file etc), we allocate
 * a "struct state" structure for every new connection.
 */



- (void)start:(NSString *)ports documentRoot:(NSString *)docRoot RecURI:(Boolean)recUri;
{
	documentRoot = docRoot;
	ctx = mg_start(); 
	mg_set_option(ctx, "root", [docRoot UTF8String]); 
	mg_set_option(ctx, "ports", [ports UTF8String]); 
	
	//if (recUri)
	  
	

}

- (void)stop
{
	mg_stop(ctx);
}

- (NSString *)getIPAddress { 
	NSString *address = @"error"; 
	struct ifaddrs *interfaces = NULL; 
	struct ifaddrs *temp_addr = NULL; 
	int success = 0; // retrieve the current interfaces - returns 0 on success  success =
	getifaddrs(&interfaces); 
	if (success == 0)  { 
		// Loop through linked list of interfaces 
		temp_addr = interfaces; 
		while(temp_addr != NULL)  { 
			if(temp_addr->ifa_addr->sa_family == AF_INET)  { 
				// Check if interface is en0 which is the wifi connection on the iPhone  
				if([[NSString stringWithUTF8String:temp_addr->ifa_name] isEqualToString:@"en0"])  {
					// Get NSString from C String  
					address = [NSString stringWithUTF8String:inet_ntoa(((struct sockaddr_in *)temp_addr->ifa_addr)->sin_addr)]; }
			} temp_addr = temp_addr->ifa_next; } 
	} 
	// Free memory  
	freeifaddrs(interfaces); 
	return address; 
} 

@end

