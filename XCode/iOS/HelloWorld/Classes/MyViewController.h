//
//  MyViewController.h
//  HelloWorld
//
//  Created by Giuseppe Cattaneo on 19/12/10.
//  Copyright 2010 Dip. Informatica ed Applicazioni - Università di Salerno. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface MyViewController : UIViewController <UITextFieldDelegate> {
	
	UITextField *textField;
	
	UILabel *label;
	
	NSString *string;

}

@property (nonatomic, retain) IBOutlet UITextField *textField;

@property (nonatomic, retain) IBOutlet UILabel *label;

@property (nonatomic, copy) NSString *string;

- (IBAction)changeGreeting:(id)sender;

@end
