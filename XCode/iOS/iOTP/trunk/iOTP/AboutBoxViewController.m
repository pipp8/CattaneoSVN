//
//  AboutBoxViewController.m
//  iOTP
//
//  Created by Giuseppe Cattaneo on 15/02/15.
//  Copyright (c) 2015 Giuseppe Cattaneo. All rights reserved.
//
// $Id: AboutBoxViewController.m 826 2016-02-17 14:18:30Z cattaneo $
//


#import "AboutBoxViewController.h"

#import "iOTPMisc.h"



@interface AboutBoxViewController ()

@property NSString * Revision;

@property NSString * Date;

@property (weak, nonatomic) IBOutlet UILabel *lblLongDesc;

@property (weak, nonatomic) IBOutlet UIBarButtonItem *btBack;
@end



@implementation AboutBoxViewController



// per utilizzare la doppia unwind segue
- (IBAction)btBack_clicked:(id)sender {

    // [self dismissViewControllerAnimated:YES completion:nil]; // non funziona
    [self.navigationController popViewControllerAnimated: YES];
}



- (void)viewDidLoad {
    [super viewDidLoad];
    
    // Do any additional setup after loading the view.
    
    // set view background color
    self.view.backgroundColor = GrayBackgroundColor;
    self.lblLongDesc.backgroundColor = GrayBackgroundColor;
    
//    //set bar color
//    [self.navigationController.navigationBar setBarTintColor: BlueColor];
//    //optional, i don't want my bar to be translucent
//    [self.navigationController.navigationBar setTranslucent:NO];
//    //set title color
//    [self.navigationController.navigationBar setTitleTextAttributes:[NSDictionary dictionaryWithObject:[UIColor whiteColor] forKey:UITextAttributeTextColor]];
//    
    //set title and title color
    //    // [self.navigationItem setTitle:@"Title"];
    
    UIImage *logo = [UIImage imageNamed:@"ID-BarIcon.png"];
    [self.navigationItem setTitleView: [[UIImageView alloc]
                                        initWithImage:[logo scaleImageToHeight: self.navigationController.navigationBar.frame.size.height]]];
    
//    //set back button color
//    [[UIBarButtonItem appearanceWhenContainedIn:[UINavigationBar class], nil]
//     setTitleTextAttributes:[NSDictionary dictionaryWithObjectsAndKeys:[UIColor whiteColor], UITextAttributeTextColor,nil]
//     forState:UIControlStateNormal];
//
//    [[UIBarButtonItem appearanceWhenContainedIn:[UINavigationBar class], nil]
//     setTitleTextAttributes:[NSDictionary dictionaryWithObjectsAndKeys:[UIColor grayColor], UITextAttributeTextColor,nil]
//     forState:UIControlStateDisabled];
//    
//    
//    //set back button arrow color
//    [self.navigationController.navigationBar setTintColor:[UIColor whiteColor]];
   
//    //Cambio il delegato della navar solo! per animare l'uscita
//    UIBarButtonItem *buttonBack = [[UIBarButtonItem alloc] initWithTitle:@"Home" style:UIBarButtonItemStyleBordered target:self action:@selector(animateBack:)];
//    
//    self.navigationItem.leftBarButtonItem = buttonBack;
//
    // Set this in every view controller so that the back button displays back instead of the root view controller name
    self.navigationItem.backBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:@" " style:UIBarButtonItemStylePlain target:nil action:nil];
//
//    NSString * SVNRev = @"$Rev: 826 $";
//    NSString * SVNDate = @"$Date: 2016-02-17 15:18:30 +0100(Mer, 17 Feb 2016) $";
    
    NSString *build = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleVersion"];
    NSString *versionShort = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleShortVersionString"];

    NSString * SVNRev = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"SVNRevisionRelease"];
    NSString * SVNDate = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"DataRelease"];
    

    NSArray * words = [SVNRev componentsSeparatedByString:@" "];
    self.Revision = words[1];
    
    words = [SVNDate componentsSeparatedByString:@" "];
    self.Date = [NSString stringWithFormat: @"%@ %@", words[1], words[2]];
    
    [self.lblLongDesc setText:[ NSString stringWithFormat: NSLocalizedString(@"VERSION-MSG", nil),
                               [self.lblLongDesc text], versionShort, build, self.Revision, self.Date]];

}




- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}



//-(void)animateBack:(id)sender {
//    
//    if(self.animateBackDelegate != nil && [self.animateBackDelegate respondsToSelector:@selector(animateBack:)]) {
//        [self.animateBackDelegate performSelectorOnMainThread:@selector(animateBack:) withObject:nil waitUntilDone:NO];
//    }
//}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
