//
//  main.m
//  HelloWorld
//
//  Created by Giuseppe Cattaneo on 19/12/10.
//  Copyright 2010 Dip. Informatica ed Applicazioni - Università di Salerno. All rights reserved.
//

#import <UIKit/UIKit.h>

int main(int argc, char *argv[]) {
    
    NSAutoreleasePool * pool = [[NSAutoreleasePool alloc] init];
    int retVal = UIApplicationMain(argc, argv, nil, nil);
    [pool release];
    return retVal;
}
