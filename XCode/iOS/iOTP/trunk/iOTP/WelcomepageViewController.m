//
//  WelcomepageViewController.m
//  iOTP
//
//  Created by Giuseppe Cattaneo on 19/11/15.
//  Copyright © 2015 Giuseppe Cattaneo. All rights reserved.
//

#import "WelcomepageViewController.h"

#import "iOTPMisc.h"




@interface WelcomepageViewController ()

@property (weak, nonatomic) IBOutlet UILabel *lblMessage;

@property (weak, nonatomic) IBOutlet UIBarButtonItem *btNext;

@property (weak, nonatomic) IBOutlet UIButton *btAttiva;
@end

@implementation WelcomepageViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    // set view background color
    self.view.backgroundColor = GrayBackgroundColor;

//    // Initialize the toolbar sopra
//    self.navigationController.toolbarHidden = YES;
//    // self.navigationController.navigationBar.tintColor = BlueColor;
//    // self.navigationController.toolbar.tintColor = BlueColor;
//    
//    //set bar color
//    [self.navigationController.navigationBar setBarTintColor: BlueColor];
//    //optional, i don't want my bar to be translucent
//    [self.navigationController.navigationBar setTranslucent:NO];
//    //set title and title color
//    // [self.navigationItem setTitle:@"Title"];
//    
//    UIImage *logo = [UIImage imageNamed:@"logoapp.png"];
//    [self.navigationItem setTitleView: [[UIImageView alloc]
//                                        initWithImage:[logo scaleImageToHeight: self.navigationController.navigationBar.frame.size.height]]];
//
//    [self.navigationController.navigationBar setTitleTextAttributes:[NSDictionary dictionaryWithObject:[UIColor whiteColor]
//                                            forKey:UITextAttributeTextColor]];
//    //set back button color
//    [[UIBarButtonItem appearanceWhenContainedIn:[UINavigationBar class], nil] setTitleTextAttributes:[NSDictionary dictionaryWithObjectsAndKeys:[UIColor whiteColor], UITextAttributeTextColor,nil] forState:UIControlStateNormal];
//    //set back button arrow color
//    [self.navigationController.navigationBar setTintColor:[UIColor whiteColor]];
    
    _btAttiva.backgroundColor = BlueColor;
    
    _lblMessage.text = NSLocalizedString(@"INIT1-MSG", nil);
    
 }


-(void)viewWillAppear:(BOOL)animated
{
}



- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


- (IBAction)btNextClicked:(id)sender {

    [self performSegueWithIdentifier:@"RemoteSeedGenerationSegueId" sender:self];
}


- (IBAction)btAttivaClicked:(id)sender {
    
    [self performSegueWithIdentifier:@"RemoteSeedGenerationSegueId" sender:self];
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
