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

@property int remainingTimeSec;
@property int remainingDecimi;


@property (weak, nonatomic) IBOutlet UILabel *lblOTP;

@property (weak, nonatomic) IBOutlet UIProgressView *progressView;

@property (weak, nonatomic) IBOutlet UIButton *btNuovoOTP;

@property (weak, nonatomic) IBOutlet UILabel *lblRemainingTime;
@end

@implementation ShowOTPViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.

    UIImage *logo = [UIImage imageNamed:@"ID-BarIcon.png"];
    [self.navigationItem setTitleView: [[UIImageView alloc]
                                        initWithImage:[logo scaleImageToHeight: self.navigationController.navigationBar.frame.size.height]]];
    
    [self.navigationItem setHidesBackButton:YES animated:YES];
    
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
    
    //[self dismissViewControllerAnimated:YES completion:nil];
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
    
//    self.remainingTimeSec = (60 - (usedTime % 60)) + 3 * 60; // windows 3 min + 3 min
    int  deltaTime = (int) ([[NSDate date] timeIntervalSince1970] - usedTime);
    self.remainingTimeSec = clockRound + (clockRound - (usedTime % clockRound) - deltaTime); // una finestra intera + la parte rimanente della finestra corrente - il tempo trascorso dall'inizio del calcolo per la visualizzazione
    self.remainingDecimi = self.remainingTimeSec * 10;
    
    self.progressValue = self.remainingTimeSec / (2. * clockRound);    // la barra intera rappresenta 2 windows (60 sec),
    self.progressView.progress = self.progressValue;                   // ma parte dalla posizione non fruita della prima finestra
    [self increaseProgressValue];
    
}



-(void)increaseProgressValue
{
    float interval = 0.3;
    
    if (self.progressView.progress > 0)
    {
        self.progressValue = self.progressValue - interval / (2. * clockRound);
        
        self.remainingDecimi = self.remainingDecimi - 3;
        
        self.progressView.progress = self.progressValue;

        
        self.lblRemainingTime.text = [NSString stringWithFormat: @"%@ %d sec.",
                                      NSLocalizedString(@"Tempo rimanente:", nil), (int) self.remainingDecimi / 10];

//         [self performSelectorOnMainThread:@selector(updateLabelText) withObject:nil waitUntilDone:NO];

        [self performSelector:@selector(increaseProgressValue) withObject:self afterDelay: interval];
    }
}



- (void)updateLabelText
{
    self.lblRemainingTime.text = [NSString stringWithFormat: @"%@ %d sec.",
                                  NSLocalizedString(@"Tempo rimanente:", nil), self.remainingDecimi / 10];
}

@end
