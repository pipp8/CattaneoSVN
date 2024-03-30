//
//  OptionsController.h
//  Spirograph
//
// $Header: http://maccattaneo:8081/svn/Cattaneo/XCode/iOS/Spirograph/Spirograph/Classes/OptionsController.h 159 2011-08-31 18:02:00Z cattaneo $
// $Id: OptionsController.h 159 2011-08-31 18:02:00Z cattaneo $
//
//  Created by Giuseppe Cattaneo on 17/08/11.
//  Copyright 2011 Dip. Informatica - Università di Salerno. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "CanvasController.h"

#import "HistoryManagerController.h"

#import "ColorPickerViewController.h"
#import <UIKit/UIColor.h>

@interface OptionsController : UIViewController <ColorPickerViewControllerDelegate, modelChangedDelegate> {
    
    CanvasController *canvas;
    ColorPickerViewController *colorPicker;
    HistoryManagerController * historyMgr;
        
    UIButton *btChooseColor;
    UIButton *btAddDraw;
    UIButton *btHistory;
    UIButton *btClearView;
    
    UILabel *lblRaggioInterno;
    UILabel *lblInPenDistance;
    UILabel *lblColor;
    UILabel *lblValueInCircle;
    UILabel *lblValuePenPosition;
    UIView *viewSelectedColor;
    UITextView *viewHistory;
    
    UISlider *sliderInternalRadius;
    UISlider *sliderPenRadius;
}

@property (nonatomic, retain) CanvasController *canvas;
@property (nonatomic, retain) ColorPickerViewController *colorPicker;
@property (nonatomic, retain) HistoryManagerController * historyMgr;

@property (nonatomic, retain) IBOutlet UIButton *btChooseColor;
@property (nonatomic, retain) IBOutlet UIButton *btAddDraw;
@property (nonatomic, retain) IBOutlet UIButton *btHistory;
@property (nonatomic, retain) IBOutlet UIButton *btClearView;

@property (nonatomic, retain) IBOutlet UILabel *lblRaggioInterno;
@property (nonatomic, retain) IBOutlet UILabel *lblInPenDistance;
@property (nonatomic, retain) IBOutlet UILabel *lblColor;
@property (nonatomic, retain) IBOutlet UILabel *lblValueInCircle;
@property (nonatomic, retain) IBOutlet UILabel *lblValuePenPosition;
@property (nonatomic, retain) IBOutlet UIView *viewSelectedColor;
@property (nonatomic, retain) IBOutlet UITextView *viewHistory;


@property (nonatomic, retain) IBOutlet UISlider *sliderInternalRadius;
@property (nonatomic, retain) IBOutlet UISlider *sliderPenRadius;

- (IBAction)selectColor:(id)sender;
- (IBAction)drawAction:(id)sender;
- (IBAction)clearAction:(id)sender;


- (IBAction)inRadiusChanged:(id)sender;
- (IBAction)penPositionChanged:(id)sender;

- (void) modelDidChanged:(HistoryManagerController *)mgr;

- (void)didDraw:(CanvasController *)controller;

@end
