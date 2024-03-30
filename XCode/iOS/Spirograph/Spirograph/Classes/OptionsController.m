//
//  OptionsController.m
//  Spirograph
//
// $Header: http://maccattaneo:8081/svn/Cattaneo/XCode/iOS/Spirograph/Spirograph/Classes/OptionsController.m 159 2011-08-31 18:02:00Z cattaneo $
// $Id: OptionsController.m 159 2011-08-31 18:02:00Z cattaneo $
//
//  Created by Giuseppe Cattaneo on 17/08/11.
//  Copyright 2011 Dip. Informatica - Università di Salerno. All rights reserved.
//

#import "OptionsController.h"


@implementation OptionsController

@synthesize canvas=_canvas;
@synthesize colorPicker;
@synthesize historyMgr;

@synthesize btChooseColor;
@synthesize btAddDraw;
@synthesize btHistory;
@synthesize btClearView;

@synthesize lblRaggioInterno;
@synthesize lblInPenDistance;
@synthesize lblColor;
@synthesize lblValueInCircle;
@synthesize lblValuePenPosition;

@synthesize viewSelectedColor;
@synthesize viewHistory;

@synthesize sliderInternalRadius;
@synthesize sliderPenRadius;


- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        self.title = @"SpirogrApp";
//        self.navigationItem.rightBarButtonItem = [[[UIBarButtonItem alloc] initWithTitle:@"Drawing"
//                   style:UIBarButtonItemStyleBordered target:self action:@selector(drawAction:)] autorelease];
//        [self.navigationItem setHidesBackButton:YES];
        
        [self setCanvas: [[CanvasController alloc] initWithNibName:@"CanvasController" bundle:nil]]; 

        [self setColorPicker: [[ColorPickerViewController alloc] initWithNibName:@"ColorPickerViewController" bundle:nil]];
        [colorPicker setDelegate: self];

        [self setHistoryMgr: [[HistoryManagerController alloc] initWithNibName:@"HistoryManagerController" bundle:nil]]; 
        [[self canvas] setHistoryMgr:[self historyMgr]];
        [[self historyMgr] setParentView: self];
    }
    return self;
}

- (void)dealloc
{
    [_canvas release];
    [colorPicker release];
    [historyMgr release];
    
    [lblRaggioInterno release];
    [lblInPenDistance release];
    [lblColor release];
    [btChooseColor release];
    [btAddDraw release];
    [btHistory release];
    [btClearView release];
    [sliderInternalRadius release];
    [sliderPenRadius release];
    [lblValueInCircle release];
    [lblValuePenPosition release];
    [viewSelectedColor release];
    [viewHistory release];
    [super dealloc];
}

- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
    // set slider ranges and default value (non in viewWillAppear altrimenti si resattano ad ogni display)
    CGFloat v = [[self canvas] maxInCircleSize];
    [[self sliderInternalRadius] setMaximumValue: v];    
    [[self sliderInternalRadius] setMinimumValue: [[self canvas] minInCircleSize]];
    v = v / 2.;
    [[self sliderInternalRadius] setValue: v];
    [[self canvas] setInCircleSize: v];
    [lblValueInCircle setText: [NSString stringWithFormat: @"%.1f", v]];
    
    [[self sliderPenRadius] setMaximumValue: v * 0.98];
    [[self sliderPenRadius] setMinimumValue: [[self canvas] minInCircleSize] * 0.95];
    v = v / 2.;
    [[self sliderPenRadius] setValue: v];
    [[self canvas] setPenPositionInCircle: v];
    [lblValuePenPosition setText: [NSString stringWithFormat: @"%.1f", v]];
    [viewSelectedColor setBackgroundColor: [[self canvas] penColor]];
    // Do any additional setup after loading the view from its nib.
}


- (void)viewDidUnload
{
    [self setBtAddDraw:nil];
    [self setLblRaggioInterno:nil];
    [self setLblInPenDistance:nil];
    [self setLblColor:nil];
    [self setBtChooseColor:nil];
    [self setBtClearView:nil];
    [self setSliderInternalRadius:nil];
    [self setSliderPenRadius:nil];
    [self setLblValueInCircle:nil];
    [self setLblValuePenPosition:nil];
    [self setViewSelectedColor:nil];
    [self setViewHistory:nil];
    [self setBtHistory:nil];
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

#pragma mark UIViewController delegate

- (void)viewWillAppear:(BOOL)animated
{
	// this UIViewController is about to appear
	// Set the navbar style to its default color for the list view.
	self.navigationController.navigationBar.barStyle = UIBarStyleDefault;
    self.navigationItem.leftBarButtonItem = [[[UIBarButtonItem alloc] initWithTitle:@"Exit"
                  style:UIBarButtonItemStyleBordered target:self action:@selector(exitAction:)] autorelease];
    
    self.navigationItem.rightBarButtonItem = [[[UIBarButtonItem alloc] initWithTitle:@"Drawing"
                  style:UIBarButtonItemStyleBordered target:self action:@selector(showAction:)] autorelease];
	// Set the status bar to its default color for the list view.
	[[UIApplication sharedApplication] setStatusBarStyle:UIStatusBarStyleDefault animated:YES];
}


#pragma mark -- local actions


- (IBAction) selectColor:(id)sender
{
    // default to red
    [colorPicker setDefaultsColor: [[self canvas] penColor]];

    [self presentModalViewController: colorPicker animated:YES];
}
    
- (void)colorPickerViewController:(ColorPickerViewController *)colorPickerController didSelectColor:(UIColor *) color {
    NSLog(@"Color: %d", color);
    
    // No storage & check, just assign back the color
    [[self canvas] setPenColor: color];
    [btChooseColor setBackgroundColor: color];
    [viewSelectedColor setBackgroundColor: color];
    [self.view setNeedsDisplay];
    [colorPickerController dismissModalViewControllerAnimated:YES];
}


- (IBAction)exitAction:(id)sender
{
    
}


- (IBAction)showAction:(id)sender
{
    [self.navigationController pushViewController:[self canvas] animated:YES];    
}


// Called when the user taps the Drawing button.  We just bring up the Canvas View
- (IBAction)drawAction:(id)sender
{
#pragma unused(sender)    
    if ([self canvas] == nil) {
        [self setCanvas: [[CanvasController alloc] initWithNibName:@"CanvasController" bundle:nil]];
    }
    assert([self canvas] != nil);
    
    [viewHistory setText:[[viewHistory text] stringByAppendingFormat: @"%3d,%6.1f,%6.1f\n",
                          (int) [[self canvas] externalRadius],
                          [[self canvas] inCircleSize],
                          [[self canvas] penPositionInCircle]]];
    
    // [[self canvas] presentModallyOn:self];
    [[self canvas] addNewSpirograph];
    // Displays the view we use as canvas
    [self.navigationController pushViewController:[self canvas] animated:YES];
}

- (IBAction)historyManagerAction:(id)sender
{
    [historyMgr.tableView reloadData];
    
    [self.navigationController pushViewController:[self historyMgr] animated:YES]; 
}

    
- (void) modelDidChanged:(HistoryManagerController *)mgr
{
    [[self canvas] clearView];
    [[self canvas] redrawAllStages];
}

- (IBAction)clearAction:(id)sender {
    // [self.navigationController pushViewController:[self canvas] animated:YES];
    [[self canvas] clearView];
 
    // reset history
    [[historyMgr drawnParameters] removeAllObjects];
}


- (void)didDraw:(CanvasController *)controller
// Called when the user taps back view.
{
#pragma unused(controller)
    assert(controller != nil);
    [self dismissModalViewControllerAnimated:YES];
}  

- (IBAction)inRadiusChanged:(id)sender
{    
    assert( sender == sliderInternalRadius);
    CGFloat value = [sliderInternalRadius value];
//    NSLog(@"Internal radius current value: %f", value);
    
    [[self canvas] setInCircleSize: value];         // aggiorna il valore del raggio nella classe
    [lblValueInCircle setText: [NSString stringWithFormat: @"%.1f", value]];

    if ([sliderPenRadius value] >= value)
    {
        value -= 5;
        [[self canvas] setPenPositionInCircle: value];
        [sliderPenRadius setValue: value];
        [lblValuePenPosition setText: [NSString stringWithFormat: @"%.1f", value]];
    }
    [sliderPenRadius setMaximumValue: value];   // in ogni caso aggiorna il massimo per la posizione della penna
}


- (IBAction)penPositionChanged:(id)sender
{
    assert(sender == sliderPenRadius);
    CGFloat value = [sliderPenRadius value];
//    NSLog(@"Pen Position current value: %f", value);
    [[self canvas] setPenPositionInCircle: value];
    [lblValuePenPosition setText: [NSString stringWithFormat: @"%.1f", value]];
}




@end
