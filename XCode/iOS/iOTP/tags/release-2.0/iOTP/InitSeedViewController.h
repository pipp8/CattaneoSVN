//
//  InitSeedViewController.h
//  iOTP
//
//  Created by Giuseppe Cattaneo on 08/02/15.
//  Copyright (c) 2015 Giuseppe Cattaneo. All rights reserved.
//
// $Id: InitSeedViewController.h 720 2015-03-08 18:29:14Z cattaneo $
//

#import <UIKit/UIKit.h>

@interface InitSeedViewController : UIViewController <UITextFieldDelegate>


- (void)viewDidLoad;

- (void)didReceiveMemoryWarning;

- (IBAction)btSaveClicked:(id)sender;

- (IBAction)btCancelClicked:(id)sender ;
- (IBAction)txtKeyEditingBegin:(id)sender;
- (IBAction)txtKeyEndOnExit:(id)sender;

- (void) generateSeed;

- (IBAction)txtKeyTouchOutside:(id)sender;

- (IBAction)txtKeyValueChanged:(id)sender;

- (BOOL)textFieldShouldReturn:(UITextField *)textField ;

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex ;

@end
