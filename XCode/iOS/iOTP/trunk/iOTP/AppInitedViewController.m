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

#import "SettingsListTableViewController.h"


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
    
    UIImage *logo = [UIImage imageNamed:@"ID-BarIcon.png"];
    [self.navigationItem setTitleView: [[UIImageView alloc]
                                        initWithImage:[logo scaleImageToHeight: self.navigationController.navigationBar.frame.size.height]]];

    // rimuove il bottone per la navigazione
    self.navigationItem.leftBarButtonItem = nil;
    self.navigationItem.hidesBackButton = YES;
    
    [self.btStartApp setBackgroundColor: BlueColor];
    
}



- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}



- (IBAction)btStartApp_Clicked:(id)sender {

    // cambia view automaticamente
    //    [self presentViewController:self.delegate animated:YES completion:nil];
    
    // supponiamo che la rootwindows sia proprio una istanza della mainViewController
    MainViewController *mvc = [self.navigationController.viewControllers objectAtIndex:0];
    
    if ([mvc isMemberOfClass:[MainViewController class]]) {
            // siamo arrivati qui con la generazione automatica
        [self performSegueWithIdentifier:@"SeedGeneratedUnwindSegueId" sender:self];
    }
    else {
        // potremmo essere arrivati qui partendo dai settings (modale)
        if ([mvc isMemberOfClass:[SettingsListTableViewController class]]) {
 
            [self performSegueWithIdentifier:@"SeedGeneratedToSettingsSegueId" sender:self];
        }
        else {
            NSLog(@"Wrong navigation stack Seed not saved");
            return;
        }
    }
}

@end
