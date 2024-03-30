//
//  AppInitedViewController.m
//  iOTP
//
//  Created by Giuseppe Cattaneo on 26/04/15.
//  Copyright (c) 2015 Giuseppe Cattaneo. All rights reserved.
//

#import "AppInitedViewController.h"

#import "iOTPMisc.h"

#import "MainViewController.h"


@interface AppInitedViewController ()

@property (weak, nonatomic) IBOutlet UIButton *btStartApp;

@end


@implementation AppInitedViewController


- (void) viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    // You code here to update the view.
}


- (void)viewDidLoad {
    [super viewDidLoad];
    
    // set view background color
    self.view.backgroundColor = GrayBackgroundColor;
    
    [self.btStartApp setBackgroundColor: BlueColor];
    
}



- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}



- (IBAction)btStartApp_Clicked:(id)sender {

    // cambia view automaticamente
//    self.delegate.tabBarController.selectedIndex = mainViewIndex; // il tabItem con la main page
//    [self presentViewController:self.delegate animated:YES completion:nil];
    
    [self performSegueWithIdentifier:@"unwindToRestart" sender:self];

}

@end
