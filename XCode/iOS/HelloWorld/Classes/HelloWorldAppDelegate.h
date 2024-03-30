//
//  HelloWorldAppDelegate.h
//  HelloWorld
//
//  Created by Giuseppe Cattaneo on 19/12/10.
//  Copyright 2010 Dip. Informatica ed Applicazioni - Università di Salerno. All rights reserved.
//

#import <UIKit/UIKit.h>

@class MyViewController;

@interface HelloWorldAppDelegate : NSObject <UIApplicationDelegate> {
    UIWindow *window;

	MyViewController *myViewController;
}

@property (nonatomic, retain) IBOutlet UIWindow *window;

@property (nonatomic, retain) MyViewController *myViewController;

@end

