//
//  CanvasController.h
//  Spirograph
//
// $Header: http://maccattaneo:8081/svn/Cattaneo/XCode/iOS/Spirograph/Spirograph/Classes/CanvasController.h 159 2011-08-31 18:02:00Z cattaneo $
// $Id: CanvasController.h 159 2011-08-31 18:02:00Z cattaneo $
//
//  Created by Giuseppe Cattaneo on 17/08/11.
//  Copyright 2011 Dip. Informatica - Università di Salerno. All rights reserved.
//

#import <UIKit/UIKit.h>


/* Defines */
#define kOurImageFile		CFSTR("ptlobos.jpeg")

// For best performance make bytesPerRow a multiple of 16 bytes.
#define BEST_BYTE_ALIGNMENT 16
#define COMPUTE_BEST_BYTES_PER_ROW(bpr)		( ( (bpr) + (BEST_BYTE_ALIGNMENT-1) ) & ~(BEST_BYTE_ALIGNMENT-1) )


@class PaintView;
@class HistoryManagerController;

@interface CanvasController : UIViewController {

    PaintView * paintView;
    CGColorSpaceRef genericRGBSpace;
    
    CGFloat externalRadius;
    CGFloat inCircleSize, minInCircleSize, maxInCircleSize;
    CGFloat penPositionInCircle;    // must be less than inCircleSize
    UIColor *penColor;
    
    UIScrollView *scrollView;
    
    HistoryManagerController * historyMgr;
    UIActivityIndicatorView *activityIndicator;
}

@property (nonatomic, retain) IBOutlet UIActivityIndicatorView *activityIndicator;

@property (nonatomic, retain) IBOutlet UIScrollView *scrollView;

@property (nonatomic, retain) PaintView * paintView;

@property (nonatomic, retain) HistoryManagerController *historyMgr;

@property (nonatomic, readwrite) CGFloat externalRadius;
@property (nonatomic, readwrite) CGFloat inCircleSize;
@property (nonatomic, readwrite) CGFloat minInCircleSize;
@property (nonatomic, readwrite) CGFloat maxInCircleSize;
@property (nonatomic, readwrite) CGFloat penPositionInCircle;
@property (nonatomic, readwrite, retain) UIColor *penColor;

@property (nonatomic, readwrite) CGColorSpaceRef genericRGBSpace;

- (void) clearView;
- (void) addNewSpirograph;
- (void) drawSpirograph: (CGContextRef) context center: (CGPoint) center;
- (void) redrawAllStages;
- (void) drawAllStagesWithContext: (CGContextRef) context center: (CGPoint) center;


- (CGColorSpaceRef) genericRGBSpace;
- (CGImageRef) createRGBAImageFromQuartzDrawing: (CGFloat) dpi;
- (void) image: (UIImage *) image didFinishSavingWithError: (NSError *) error contextInfo: (void *) contextInfo;


- (void) presentModallyOn: (UIViewController *)parent;
// This wraps the controller is a UINavigationController and presents 
// that modally on the specified parent controller.

- (void) backAction: (id) sender;
- (void) saveAction: (id) sender;

@end



@interface PaintView : UIView
{
    CanvasController * painter;
    CGLayerRef cachedLayer;
    BOOL shouldDraw;
}

@property (nonatomic, retain) CanvasController * painter;
@property (nonatomic) CGLayerRef cachedLayer;
@property (nonatomic, readwrite) BOOL shouldDraw;

-(id)initWithFrame:(CGRect)frame painter: (CanvasController *) p;

@end
