//
//  AboutBoxViewController.m
//  iOTP
//
//  Created by Giuseppe Cattaneo on 15/02/15.
//  Copyright (c) 2015 Giuseppe Cattaneo. All rights reserved.
//
// $Id: AboutBoxViewController.m 761 2015-09-20 15:50:26Z cattaneo $
//


#import "AboutBoxViewController.h"

#import "iOTPMisc.h"



@interface AboutBoxViewController ()

@property NSString * Revision;

@property NSString * Date;
@property (weak, nonatomic) IBOutlet UITextView *txtLongDesc;

@end



@implementation AboutBoxViewController


- (void)viewDidLoad {
    [super viewDidLoad];
    
    // Do any additional setup after loading the view.
    
    // set view background color
    self.view.backgroundColor = GrayBackgroundColor;
    self.txtLongDesc.backgroundColor = GrayBackgroundColor;
    
    NSString * SVNRev = @"$Rev: 761 $";
    NSString * SVNDate = @"$Date: 2015-09-20 17:50:26 +0200(Dom, 20 Set 2015) $";
    
    NSString *build = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleVersion"];
    NSString *versionShort = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleShortVersionString"];
    
    NSArray * words = [SVNRev componentsSeparatedByString:@" "];
    self.Revision = words[1];
    
    words = [SVNDate componentsSeparatedByString:@" "];
    self.Date = [NSString stringWithFormat: @"%@ %@", words[1], words[2]];
    
    [self.txtLongDesc setText:[ NSString stringWithFormat: @"%@\nVersione: %@ (build %@)\nRevisione: %@ del %@",
                               [self.txtLongDesc text], versionShort, build, self.Revision, self.Date]];

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
