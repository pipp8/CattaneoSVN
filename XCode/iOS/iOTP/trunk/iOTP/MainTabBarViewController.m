//
//  MainTabBarViewController.m
//  iOTP
//
//  Created by Giuseppe Cattaneo on 24/02/15.
//  Copyright (c) 2015 Giuseppe Cattaneo. All rights reserved.
//

#import "MainTabBarViewController.h"

#import "iOTPMisc.h"

#import "MainViewController.h"



@interface MainTabBarViewController ()

@end

@implementation MainTabBarViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [[UITabBar appearance] setBackgroundColor: BlueColor];
    
    UINavigationController * navigationVC  = (UINavigationController *)[self.viewControllers objectAtIndex: mainViewIndex];
    
    MainViewController * mainVC = (MainViewController *) [navigationVC.viewControllers objectAtIndex:0];
    
    if ([mainVC isAppActivable]) {
        self.selectedIndex = mainViewIndex; // il tabItem con la main page
    }
    else {
        self.selectedIndex = seedSetupViewIndex; // il tabItem per l'enrollement
    }

}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
