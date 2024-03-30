//
//  HistoryManagerController.m
//  Spirograph
//
// $Header: http://maccattaneo:8081/svn/Cattaneo/XCode/iOS/Spirograph/Spirograph/Classes/HistoryManagerController.m 159 2011-08-31 18:02:00Z cattaneo $
// $Id: HistoryManagerController.m 159 2011-08-31 18:02:00Z cattaneo $
//
//  Created by Giuseppe Cattaneo on 25/08/11.
//  Copyright 2011 Dip. Informatica - Università di Salerno. All rights reserved.
//

#import "HistoryManagerController.h"


#define LEFT_COLUMN_OFFSET 10.0
#define LEFT_COLUMN_WIDTH 160.0

#define MIDDLE_COLUMN_OFFSET 170.0
#define MIDDLE_COLUMN_WIDTH 90.0

#define RIGHT_COLUMN_OFFSET 280.0

#define MAIN_FONT_SIZE 18.0
#define LABEL_HEIGHT 26.0

#define IMAGE_SIDE 20.0
#define ROW_HEIGHT 50


@implementation HistoryManagerController


@synthesize parentView;

@synthesize drawnParameters;


- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        drawnParameters = [[NSMutableArray alloc] init];
    }
    return self;
}

- (id)initWithStyle:(UITableViewStyle)style
{
    self = [super initWithStyle:style];
    if (self) {
        drawnParameters = [[NSMutableArray alloc] init];
    }
    return self;
}


- (void)dealloc
{
    [parentView release];
    [drawnParameters release];
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

    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
 
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    self.title = @"History"; 
    self.navigationItem.leftBarButtonItem = [[[UIBarButtonItem alloc] initWithTitle:@"Options"
                                                                              style:UIBarButtonItemStyleBordered target:self action:@selector(backAction:)] autorelease];
    self.navigationItem.rightBarButtonItem = self.editButtonItem;
   	// self.tableView.style = UITableViewStylePlain;
    self.tableView.rowHeight = ROW_HEIGHT;
}



- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
}

- (void)viewDidDisappear:(BOOL)animated
{
    [super viewDidDisappear:animated];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    // Return the number of sections.
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    // Return the number of rows in the section.
    return [drawnParameters count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *CellIdentifier = @"SpirographHistoryCell";
    
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        cell = [[[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier] autorelease];
    }
    	
	// Set up the cell.
	drawingParameters * step = [drawnParameters objectAtIndex:indexPath.row];    

	UILabel *label;
	CGRect rect;
	
	// Create a label for the time zone name.
	rect = CGRectMake(LEFT_COLUMN_OFFSET, (ROW_HEIGHT - LABEL_HEIGHT) / 2.0, LEFT_COLUMN_WIDTH, LABEL_HEIGHT);
	label = [[UILabel alloc] initWithFrame:rect];
	label.font = [UIFont boldSystemFontOfSize:MAIN_FONT_SIZE];
	label.adjustsFontSizeToFitWidth = YES;
	label.highlightedTextColor = [UIColor whiteColor];
    label.text = [NSString stringWithFormat: @"%6.1f -%6.1f -%6 .1f",
                       step.extRadius, step.intRadius, step.penPosition];

	[cell.contentView addSubview:label];
    [label release];

    // Create an image view for the color image.
	rect = CGRectMake(RIGHT_COLUMN_OFFSET, (ROW_HEIGHT - IMAGE_SIDE) / 2.0, IMAGE_SIDE, IMAGE_SIDE);
    
	UIImageView *imageView = [[UIImageView alloc] initWithFrame:rect];
    imageView.backgroundColor = step.penColor;

	[cell.contentView addSubview:imageView];
	[imageView release];	
/*
    cell.textLabel.text = [NSString stringWithFormat: @"%6.1f -%6.1f -%6 .1f",
                  step.extRadius, step.intRadius, step.penPosition];
    
    cell.imageView.backgroundColor = step.penColor;
    
    cell.imageView.image = [[UIImage imageNamed:@"Icon.png"] retain];
 */   
	return cell;
}

/*
// Override to support conditional editing of the table view.
- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the specified item to be editable.
    return YES;
}
*/


// Override to support editing the table view.
- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        // Delete the row from the data source
        [drawnParameters removeObjectAtIndex:indexPath.row];

        [tableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationFade];
        [parentView modelDidChanged: self];
    }   
    else if (editingStyle == UITableViewCellEditingStyleInsert) {
        // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
    }   
}


/*
// Override to support rearranging the table view.
- (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath
{
}
*/

/*
// Override to support conditional rearranging of the table view.
- (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the item to be re-orderable.
    return YES;
}
*/

#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Navigation logic may go here. Create and push another view controller.
    /*
     <#DetailViewController#> *detailViewController = [[<#DetailViewController#> alloc] initWithNibName:@"<#Nib name#>" bundle:nil];
     // ...
     // Pass the selected object to the new view controller.
     [self.navigationController pushViewController:detailViewController animated:YES];
     [detailViewController release];
     */
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


#pragma local methods
- (void) addNewStage: (CGFloat) extR intRadius: (CGFloat) inR
          penPosition: (CGFloat) penPos penColor: (UIColor *) penCol
{
    drawingParameters * par = [[drawingParameters alloc]
                               initWithValues: extR intRadius: inR
                               penPosition: penPos penColor: penCol];

    [drawnParameters addObject:par];
    [[self view] setNeedsDisplay];
}
@end
