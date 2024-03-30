//
//  RootViewController.m
//  MyPhotoVerify
//
//  Created by Antonio De Marco on 28/07/10.
//  Copyright __MyCompanyName__ 2010. All rights reserved.
//

#import "MenuViewController.h"
#import "ServicesViewController.h"
#import "TableViewController.h"
#import "CertTableViewController.h"
@implementation MenuViewController
@synthesize menuContent;

#pragma mark -
#pragma mark View lifecycle


- (void)viewDidLoad {
    [super viewDidLoad];

    NSArray *array = [[NSArray alloc] initWithObjects: @"Documenti", @"Certificati", @"Servizi",   nil];
    
	self.menuContent = array;
	self.title = @"Menu";
	[array release];
}


/*
- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
}
*/
/*
- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
}
*/
/*
- (void)viewWillDisappear:(BOOL)animated {
	[super viewWillDisappear:animated];
}
*/
/*
- (void)viewDidDisappear:(BOOL)animated {
	[super viewDidDisappear:animated];
}
*/

/*
 // Override to allow orientations other than the default portrait orientation.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
	// Return YES for supported orientations.
	return (interfaceOrientation == UIInterfaceOrientationPortrait);
}
 */


#pragma mark -
#pragma mark Table view data source

// Customize the number of sections in the table view.
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}


// Customize the number of rows in the table view.
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [menuContent count];
}


// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    static NSString *CellIdentifier = @"Cell";
    
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        cell = [[[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier] autorelease];
    }
    
	// Configure the cell.
	cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
    	
    cell.textLabel.text = [menuContent objectAtIndex:[indexPath row]];
    	
	if ([indexPath row] == 0) {
		cell.imageView.image = [UIImage imageNamed:@"photo.png"];
	}
	
	if ([indexPath row] == 1) {
		cell.imageView.image = [UIImage imageNamed:@"certificato.jpg"];
	}
	
	if ([indexPath row] == 2) {
		cell.imageView.image = [UIImage imageNamed:@"tools.png"];
	}
	
	
	return cell;
}


/*
// Override to support conditional editing of the table view.
- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath {
    // Return NO if you do not want the specified item to be editable.
    return YES;
}
*/


/*
// Override to support editing the table view.
- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
    
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        // Delete the row from the data source.
        [tableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationFade];
    }   
    else if (editingStyle == UITableViewCellEditingStyleInsert) {
        // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view.
    }   
}
*/


/*
// Override to support rearranging the table view.
- (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath {
}
*/


/*
// Override to support conditional rearranging of the table view.
- (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath {
    // Return NO if you do not want the item to be re-orderable.
    return YES;
}
*/


#pragma mark -
#pragma mark Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    
	
	if ([indexPath row] == 0) {
		TableViewController *photoTableViewController= [[TableViewController alloc] initWithNibName:@"PhotoTableViewController" bundle:nil];
		[self.navigationController pushViewController:photoTableViewController animated:YES];
		[photoTableViewController release];
	}
	
	if ([indexPath row] == 1) {
		CertTableViewController *certTableViewController= [[CertTableViewController alloc] initWithNibName:@"CertTableViewController" bundle:nil];
		[self.navigationController pushViewController:certTableViewController animated:YES];
		[certTableViewController release];
	}
	
	if ([indexPath row] == 2) {
		ServicesViewController *servicesViewController= [[ServicesViewController alloc] initWithNibName:@"ServicesViewController" bundle:nil];
		[self.navigationController pushViewController:servicesViewController animated:YES];
		[servicesViewController release];
	}
	
}


#pragma mark -
#pragma mark Memory management

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Relinquish ownership any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload {
    // Relinquish ownership of anything that can be recreated in viewDidLoad or on demand.
    // For example: self.myOutlet = nil;
	self.menuContent = nil;
}


- (void)dealloc {
	[menuContent release];
    [super dealloc];
}


@end

