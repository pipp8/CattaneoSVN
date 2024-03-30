//
//  HistoryManagerController.h
//  Spirograph
//
// $Header: http://maccattaneo:8081/svn/Cattaneo/XCode/iOS/Spirograph/Spirograph/Classes/HistoryManagerController.h 159 2011-08-31 18:02:00Z cattaneo $
// $Id: HistoryManagerController.h 159 2011-08-31 18:02:00Z cattaneo $
//
//  Created by Giuseppe Cattaneo on 25/08/11.
//  Copyright 2011 Dip. Informatica - Università di Salerno. All rights reserved.
//

#import <UIKit/UIKit.h>

@class OptionsController;

@protocol modelChangedDelegate;



@interface HistoryManagerController : UITableViewController {
  
    id<modelChangedDelegate>    parentView;
    
    NSMutableArray *            drawnParameters;
}

@property (nonatomic, assign, readwrite) id<modelChangedDelegate> parentView;

@property (nonatomic, retain) NSMutableArray * drawnParameters;



- (void) addNewStage: (CGFloat) extR intRadius: (CGFloat) inR
         penPosition: (CGFloat) penPos penColor: (UIColor *) penCol;

@end


@protocol modelChangedDelegate <NSObject>

@required

- (void) modelDidChanged: (HistoryManagerController *) mgr;

@end


@interface drawingParameters : NSObject
{
    CGFloat extRadius;
    CGFloat intRadius;
    CGFloat penPosition;
    UIColor * penColor;
}

@property (nonatomic, readwrite) CGFloat  extRadius;
@property (nonatomic, readwrite) CGFloat  intRadius;
@property (nonatomic, readwrite) CGFloat  penPosition;
@property (nonatomic, readwrite, retain) UIColor * penColor;

- (id) initWithValues: (CGFloat) externalRadius intRadius: (CGFloat) inCircleSize
          penPosition: (CGFloat) penPositionInCircle penColor: (UIColor *) penColor;


@end
