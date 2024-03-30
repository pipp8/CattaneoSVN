//
// $Header: http://maccattaneo:8081/svn/Cattaneo/XCode/iOS/Spirograph/Spirograph/Classes/SplashScreenController.h 159 2011-08-31 18:02:00Z cattaneo $
// $Id: SplashScreenController.h 159 2011-08-31 18:02:00Z cattaneo $
//
//  SplashScreenController.h
//  Spirograph
//
//  Created by Giuseppe Cattaneo on 25/08/11.
//  Copyright 2011 Dip. Informatica - Università di Salerno. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface SplashScreenController : UIViewController {
    
    UIButton *btStart;
    UILabel *lblVersion;
}
@property (nonatomic, retain) IBOutlet UILabel *lblVersion;

@property (nonatomic, retain) IBOutlet UIButton *btStart;

@end
