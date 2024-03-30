//
//  ShowOTPViewController.m
//  iOTP
//
//  Created by Giuseppe Cattaneo on 26/04/15.
//  Copyright (c) 2015 Giuseppe Cattaneo. All rights reserved.
//

#import "ShowOTPViewController.h"

#import <AudioToolbox/AudioToolbox.h>

#import "iOTPMisc.h"



@interface ShowOTPViewController ()

@property SystemSoundID btClickedSound;
@property SystemSoundID doneSound;

@property float progressValue;

@property int remainingTime;


@property (weak, nonatomic) IBOutlet UILabel *lblOTP;

@property (weak, nonatomic) IBOutlet UIProgressView *progressView;

@property (weak, nonatomic) IBOutlet UIButton *btNuovoOTP;

@end

@implementation ShowOTPViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.

    // set view background color
    self.view.backgroundColor = GrayBackgroundColor;
    
    self.btClickedSound = 0;
    self.doneSound = 0;
    
    self.progressValue = 0.;
    
    [self.btNuovoOTP setBackgroundColor: BlueColor];
    
    self.lblOTP.font = [UIFont fontWithName:@"SCOREBOARD" size:75];
    
    //[self displayOTP: self.OTP];
    // [self performSelectorInBackground:@selector(displayOTP:) withObject: self.OTP]; // run in a separate thread
}


- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
/*
 - (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
    if ([[segue identifier] isEqualToString:@"showOTP"])
    {
    }
}
*/

- (IBAction)btNuovoOTP_Clicked:(id)sender {

    [self performSegueWithIdentifier:@"unwindToNewOTP" sender:self];

}

- (void) displayOTP: (NSString *) otp withTime: (long) usedTime {
    
    unichar cur;
    int val;
    
    if (self.btClickedSound == 0) {
        
        NSString *snd1Path = [[NSBundle mainBundle]
                              pathForResource:@"camera-video-button-press" ofType:@"wav"];
        NSURL *snd1URL = [NSURL fileURLWithPath:snd1Path];
        
        AudioServicesCreateSystemSoundID((__bridge CFURLRef)snd1URL, &_btClickedSound);
    }
    AudioServicesPlaySystemSound(self.btClickedSound);

    
    NSString * toPrint = [[NSString alloc] init];
    NSString * pre = @"";
    
    NSTimeInterval ti = 0.080;
    
    for(int i = 0; i < [otp length]; i++) {
        
        cur = [otp characterAtIndex: i];
        
        val = cur - '0';
        
        for(int j = 0; j <= val; j++) {
            
            toPrint = [NSString stringWithFormat:@"%@%c", pre, '0'+j ];
            
            [self.lblOTP setText: toPrint]; // tutte le componenti devono essere updated dal mainThread
            
            [self.view setNeedsDisplay];
            
            [[NSRunLoop currentRunLoop] runMode: NSDefaultRunLoopMode beforeDate: [NSDate date]];
            
            [NSThread sleepForTimeInterval: ti];
        }
        [NSThread sleepForTimeInterval: 0.250];
        pre = toPrint;
    }
    if (self.doneSound == 0) {
        
        NSString *snd2Path = [[NSBundle mainBundle]
                              pathForResource:@"bicycle-bell-2" ofType:@"wav"];
        NSURL *snd2URL = [NSURL fileURLWithPath:snd2Path];
        
        AudioServicesCreateSystemSoundID((__bridge CFURLRef)snd2URL, &_doneSound);
    }
    AudioServicesPlaySystemSound(self.doneSound);
    
    self.remainingTime = (60 - (usedTime % 60)) + 3 * 60; // windows 3 min + 3 min
    
    [self increaseProgressValue];
}


-(void)increaseProgressValue
{
    
    if (self.progressView.progress < 1)
    {
        self.progressValue = self.progressValue + 0.2 / self.remainingTime;
        
        self.progressView.progress = self.progressValue;
        
        [self performSelector:@selector(increaseProgressValue) withObject:self afterDelay:0.2];
        
    }
}

@end
