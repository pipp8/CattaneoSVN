//
//  CanvasController.m
//  Spirograph
//
// $Header: http://maccattaneo:8081/svn/Cattaneo/XCode/iOS/Spirograph/Spirograph/Classes/CanvasController.m 159 2011-08-31 18:02:00Z cattaneo $
// $Id: CanvasController.m 159 2011-08-31 18:02:00Z cattaneo $
//
//  Created by Giuseppe Cattaneo on 17/08/11.
//  Copyright 2011 Dip. Informatica - Università di Salerno. All rights reserved.
//

#import "CanvasController.h"

#import "HistoryManagerController.h"

@implementation CanvasController


@synthesize paintView;

@synthesize historyMgr;

@synthesize inCircleSize, minInCircleSize,maxInCircleSize;
@synthesize penPositionInCircle;
@synthesize penColor;
@synthesize activityIndicator;
@synthesize scrollView;
@synthesize externalRadius;

@synthesize genericRGBSpace;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        CGRect info = [[self view ] frame];
        NSLog( @"-initWithNibName View size: %f x %f", info.size.width, info.size.height);

        externalRadius = MIN(info.size.width, info.size.height) / 2.;
        maxInCircleSize = externalRadius * 0.9;
        minInCircleSize = 10;
        penColor = [UIColor redColor];
        [activityIndicator setActivityIndicatorViewStyle:UIActivityIndicatorViewStyleGray];
     }
    return self;
}

- (void)dealloc
{
    [scrollView release];
    [paintView release];
    [penColor release];
    [activityIndicator release];
    [super dealloc];
}

- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - View lifecycle

- (PaintView *)paintView
{
	if (paintView == nil)
	{
		paintView = [[PaintView alloc] initWithFrame:self.scrollView.frame painter:self];
	}
    assert(paintView != nil);
	return paintView;
}


- (void)viewDidLoad
{
    [super viewDidLoad];
    [scrollView addSubview: [self paintView]];
}

- (void)viewDidUnload
{
    [self setScrollView:nil];
    [self setPaintView: nil];
    [self setActivityIndicator:nil];
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

#pragma mark UIViewController delegate methods

// called before this controller's view has appeared
-(void)viewWillAppear:(BOOL)animated
{
/*
 // for aesthetic reasons make the nav bar an appropriate color (defaulting to black) for this page
	self.navigationController.navigationBar.barStyle = barStyle;
	// ditto for the status bar.
	[[UIApplication sharedApplication] setStatusBarStyle:statusStyle animated:animated];
*/
    self.title = @"Drawing"; 
    self.navigationItem.leftBarButtonItem = [[[UIBarButtonItem alloc] initWithTitle:@"Options"
                                                                              style:UIBarButtonItemStyleBordered target:self action:@selector(backAction:)] autorelease];
    self.navigationItem.rightBarButtonItem = [[[UIBarButtonItem alloc] initWithTitle:@"Save"
                                                                              style:UIBarButtonItemStyleBordered target:self action:@selector(saveAction:)] autorelease];
	
    // Reset the scroll view to 1.0 zoom
	[scrollView setZoomScale:1.0 animated:NO];
	[[self paintView] setFrame: scrollView.bounds];
    CGRect info = [[self paintView ] frame];
    NSLog( @"-viewWillAppear View size: %f x %f", info.size.width, info.size.height);
	scrollView.contentSize = scrollView.bounds.size;
    [[self paintView] setNeedsDisplay];
}

#pragma mark UIScrollView delegate methods

-(UIView *)viewForZoomingInScrollView:(UIScrollView *)scrollView
{
	return self.paintView;
}


#pragma mark painter methods

- (void) clearView
{
    // Get the CG context corresponding to the layer.
    CGContextRef context = CGLayerGetContext([paintView cachedLayer]);  // disegna nel cache layer
    
    if (context == nil)
        return;
    
    CGContextClearRect ( context, [paintView bounds]);
    [paintView setNeedsDisplay];
}


- (void) addNewSpirograph
{
    [historyMgr addNewStage:externalRadius intRadius:inCircleSize
                penPosition:penPositionInCircle penColor:penColor];
    // Get the CG context corresponding to the layer.
    CGContextRef context = CGLayerGetContext([paintView cachedLayer]);  // disegna nel cache layer
    
    if (context == nil)
    {
        [paintView setShouldDraw:YES];
        return;
    }    
	[self drawSpirograph: context center:[paintView center]];
    
    [paintView setNeedsDisplay];
}


- (void) redrawAllStages
{
    CGContextRef context = CGLayerGetContext([paintView cachedLayer]);  // disegna nel cache layer
    [self drawAllStagesWithContext: context center: [paintView center]];
}

    
- (void) drawAllStagesWithContext: (CGContextRef) context center: (CGPoint) center
{
    NSMutableArray * params = [historyMgr drawnParameters];
    NSUInteger i, count = [params count];
    for( i = 0; i < count; i++)
    {
        drawingParameters * p = [params objectAtIndex:i];
        [self setExternalRadius: [p extRadius]];
        [self setInCircleSize: [p intRadius]];
        [self setPenPositionInCircle: [p penPosition]];
        [self setPenColor: [p penColor]];
        
        [self drawSpirograph: context center: center];
    }
}


- (void) drawSpirograph: (CGContextRef) context center: (CGPoint) center
{ 
    //    CGContextSetRGBStrokeColor(context, 1.0, 0.11, 0.01, 1.0);
	CGContextSetStrokeColorWithColor( context, penColor.CGColor);
    
    CGContextSetLineWidth(context, 1.0);
    
    CGPoint startPoint = CGPointMake(0.,0.);
    CGPoint fromPoint = CGPointMake(0.,0.);
    CGPoint toPoint = CGPointMake(0.,0.);
    
    CGFloat intR1 = inCircleSize;
    CGFloat intR2 = penPositionInCircle;	// must be less than intR1
    CGFloat extR =  externalRadius;
    
    NSLog(@"ExtRadius: %.1f InRadius: %.1f, PenPosition: %.1f", extR, intR1, intR2);

    assert( intR1 > intR2);
            
    CGPoint inCenter = CGPointMake(0.,0.);  // coordinate del centro del cerchio interno
    CGFloat Dr = extR - intR1;
    int steps = 0;
    CGFloat extA = 0.0;
    CGFloat intA = 0.0;
    double stepA = M_PI / 500.0;
    double stepA1;
    CGFloat Ds;
    CGFloat minDistance = 1000.;
    
    // calcola l'angolo di rotazione del cerchio inscritto
    Ds = extR * stepA;	// arco di circonferenza corrispondente all'angolo StepA
    stepA1 = Ds / intR1;
    
    while( true) 
    {
        // coordinate cerchio esterno
        // extX = (int) (extR * Math.Sin( extA)) + c1x;
        // extY = (int) (extR * Math.Cos( extA)) + c1y;
        
        // calcola le coordinate del centro del cerchio inscritto
        inCenter.x = Dr * sin( extA);
        inCenter.y = Dr * cos( extA);
        
        // calcola le coordinate del nuovo punto da tracciare
        toPoint.x = (inCenter.x +  intR2 * sin( intA)) + center.x;
        toPoint.y = (inCenter.y -  intR2 * cos( intA)) + center.y;
        
        if (steps == 0)
        {
            startPoint = toPoint;
        }
        else
        {
            // disegna il segmento from - to
            CGContextMoveToPoint(context, fromPoint.x, fromPoint.y);
            CGContextAddLineToPoint(context, toPoint.x, toPoint.y);
            CGContextStrokePath(context);
            
            if (steps > 50000)
            {
                NSLog(@"Maximum number of iterations reached: %d", steps);
                break;
            }

            CGFloat distance = sqrt(pow((startPoint.x - toPoint.x), 2.) + pow((startPoint.y - toPoint.y), 2.));
            if (distance < 1.)
                NSLog(@"distance %.3f - step: %d continuing", distance, steps);

                
            if (distance < .65 && steps > 2) // due to CFFloat approximation we can't use CGPointEqualToPoint( startPoint, toPoint)
            {
                minDistance = distance;
                NSLog(@"stopping");
                break;
            }            
         }
         fromPoint = toPoint;   // il punto di partenza della prossima iterazione e' il vecchio punto di arrivo
        
         intA += stepA1;
         extA += stepA;
         steps++;
    }
}


- (CGColorSpaceRef) genericRGBSpace
{
    if ( genericRGBSpace == NULL ) {
//        genericRGBSpace = CGColorSpaceCreateWithName(kCGColorSpaceGenericRGB); // NA for iPhone
        genericRGBSpace = CGColorSpaceCreateDeviceRGB();
    }
    return genericRGBSpace;
}



- (CGImageRef) createRGBAImageFromQuartzDrawing: (CGFloat) dpi
{
    // For generating RGBA data from drawing. Use a Letter size page as the 
    // image dimensions. Typically this size would be the minimum necessary to 
    // capture the drawing of interest. We want 8 bits per component and for
    // RGBA data there are 4 components.
    size_t width = 8.5*dpi, height = 11*dpi, bitsPerComponent = 8, numComps = 4;
    // Compute the minimum number of bytes in a given scanline.
    size_t bytesPerRow = width* bitsPerComponent/8 * numComps;
    
    // This bitmapInfo value specifies that we want the format where alpha is
    // premultiplied and is the last of the components. We use this to produce
    // RGBA data.
    CGBitmapInfo bitmapInfo = kCGImageAlphaPremultipliedLast;
    
    // Round to nearest multiple of BEST_BYTE_ALIGNMENT for optimal performance.
    bytesPerRow = COMPUTE_BEST_BYTES_PER_ROW(bytesPerRow);
    
    // Allocate the data for the bitmap.
    char *data = malloc( bytesPerRow * height);
    
    NSLog(@"Allocated %ld bytes at 0x%x", bytesPerRow * height, (unsigned int) data);
    // Create the bitmap context. Characterize the bitmap data with the
    // Generic RGB color space.
    CGContextRef bitmapContext = CGBitmapContextCreate( data, width, height, bitsPerComponent, bytesPerRow,
                                                       [self genericRGBSpace], bitmapInfo);
    
    // Clear the destination bitmap so that it is completely transparent before
    // performing any drawing. This is appropriate for exporting PNG data or
    // other data formats that capture alpha data. If the destination output
    // format doesn't support alpha then a better choice would be to paint
    // to white.
    CGContextClearRect( bitmapContext, CGRectMake(0, 0, width, height) );
    
    int min = MIN(width, height);
    CGSize paintSize = paintView.bounds.size;
    int minCanvas = MIN(paintSize.width, paintSize.height);
    CGFloat factor = min / minCanvas; 
    // Scale the coordinate system so that can reuse the same parameters used to draw in the paintView.
    CGContextScaleCTM( bitmapContext, factor, factor);
    
    // Perform the requested drawing in the new context (replay all the requests)
    [self drawAllStagesWithContext: bitmapContext center: CGPointMake( width/(2*factor), height/(2*factor))];

     // Create a CGImage object from the drawing performed to the bitmapContext.
    CGImageRef image = CGBitmapContextCreateImage(bitmapContext);
    
    // Release the bitmap context object and free the associated raster memory.
    CGContextRelease(bitmapContext);
    free(data);
    
    // Return the CGImage object this code created from the drawing.
    return image;
}


- (void) image: (UIImage *) image didFinishSavingWithError: (NSError *) error contextInfo: (void *) contextInfo
{    
    NSString * message;
    
    if (error != nil)
    {
        message = [NSString stringWithFormat: @"Error Saving Image: %@", [error localizedDescription]];
    }
    else
    {
        message = [NSString stringWithFormat: @"Image Saved"];
    }
    UIAlertView* alertView = [[UIAlertView alloc] initWithTitle:@"Info" message: message delegate:nil
                                              cancelButtonTitle:@"OK" otherButtonTitles:nil];
    [alertView show];
    [alertView release];
    [activityIndicator stopAnimating];
    [activityIndicator setHidden:YES];
}


- (void) saveAction: (id) sender
{
#pragma unused(sender)
    float dpi = 300;
    [activityIndicator startAnimating];
    [activityIndicator setHidden:NO];

    // Create an RGBA image from the Quartz drawing that corresponds to drawingCommand.
    CGImageRef image = [self createRGBAImageFromQuartzDrawing: dpi];
    
    // Create a CGImageDestination object will write PNG data to URL.
    // We specify that this object will hold 1 image.
    // CGImageDestinationRef imageDestination = CGImageDestinationCreateWithURL(url, kUTTypePNG, 1, NULL);
    
    UIImage *myImage = [UIImage imageWithCGImage:image];
    
    UIImageWriteToSavedPhotosAlbum( myImage, self, @selector(image:didFinishSavingWithError:contextInfo:), nil);
    
    /*
     NSData *jpgData = UIImageJPEGRepresentation(myImage, 0.9f);
     
     NSError * error = [NSError alloc];
     
     if ([jpgData writeToFile: file options:NSDataWritingAtomic error: &error] != YES)
     {
     */ 
    // Release the CGImage object that createRGBAImageFromQuartzDrawing created.
    CGImageRelease(image);
    
    // [myImage release];
    // [jpgData release]; // cannot / must not be released !!!
}


- (void) backAction: (id) sender
{
#pragma unused(sender)
    /*
     if ( (self.delegate != nil) && [self.delegate respondsToSelector:@selector(didDraw:)] ) {
     [self.delegate didDraw:self];
     }
     */
    [self.navigationController popViewControllerAnimated: YES];
}

#pragma mark -- navigation methods
- (void)presentModallyOn:(UIViewController *)parent
{
    UINavigationController * nav;
    
    // Create a navigation controller with us as its root.
    
    nav = [[[UINavigationController alloc] initWithRootViewController:self] autorelease];
    assert(nav != nil);
    
    self.navigationItem.leftBarButtonItem = [[[UIBarButtonItem alloc] initWithTitle:@"Options"
                    style:UIBarButtonItemStyleBordered target:self action:@selector(backAction:)] autorelease];
 
    assert(self.navigationItem.leftBarButtonItem != nil);
    // self.navigationItem.rightBarButtonItem = [[[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemSave   target:self action:@selector(saveAction:)] autorelease];
    // assert(self.navigationItem.rightBarButtonItem != nil);
    
    // Present the navigation controller on the specified parent 
    // view controller.
    
    [parent presentModalViewController:nav animated:YES];
}


@end


@implementation PaintView

@synthesize painter;
@synthesize cachedLayer;
@synthesize shouldDraw;

-(id)initWithFrame:(CGRect)frame painter: (CanvasController *) p
{
	self = [super initWithFrame:frame];
	if(self != nil)
	{
		self.backgroundColor = [UIColor whiteColor];
		self.opaque = YES;
		self.clearsContextBeforeDrawing = YES;
        [self setCachedLayer: nil];
        shouldDraw = NO;
        [self setNeedsDisplay];
	}
    [self setPainter: p];
    
	return self;
}



-(void)drawRect:(CGRect)rect
{
	// Since we use the CGContextRef a lot, it is convienient for our demonstration classes to do the real work
	// inside of a method that passes the context as a parameter, rather than having to query the context
	// continuously, or setup that parameter for every subclass.

    CGContextRef context = UIGraphicsGetCurrentContext();    
    if (cachedLayer == nil)
    {
        // inizializa un layer da conservare per le successive operazioni, con la stessa taglia della view.
        cachedLayer = CGLayerCreateWithContext(context,  self.bounds.size, NULL);
        
        if( shouldDraw == YES)
        {
            CGContextRef cachedContext = CGLayerGetContext(cachedLayer);  // disegna nel cache layer
            [painter drawSpirograph: cachedContext center: [[painter paintView] center]];
            shouldDraw = NO;    // solo una volta
        }
    }

    CGContextDrawLayerAtPoint(context, CGPointMake(0, 0), cachedLayer);
}


- (void)dealloc
{
    CGLayerRelease(cachedLayer);
    [painter release];
    [super dealloc];
}

@end


@implementation drawingParameters

@synthesize extRadius, intRadius, penPosition;
@synthesize penColor;


- (id) initWithValues: (CGFloat) er intRadius: (CGFloat) ir
            penPosition: (CGFloat) pp penColor: (UIColor *) pc
{
    self = [super init];
    self.extRadius = er;
    self.intRadius = ir;
    self.penPosition = pp;
    self.penColor = pc;
    
    return self;
}


@end
