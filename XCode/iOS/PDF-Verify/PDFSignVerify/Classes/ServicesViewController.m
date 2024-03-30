//
//  ServicesViewController.m
//  MyPhotoVerify
//
//  Created by Antonio De Marco on 28/07/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "ServicesViewController.h"
#import "DBSession.h"
#import "PDFSignVerifyAppDelegate.h"



@implementation ServicesViewController
@synthesize servicesList;
@synthesize webServer;

#pragma mark -
#pragma mark View lifecycle


- (void)viewDidLoad {
    [super viewDidLoad];
	
	self.title = @"Servizi"; 
    NSArray *array = [[NSArray alloc] initWithObjects: @"Web server", @"DropBox",   nil];
    
    self.servicesList = array;
	[array release];
	MongooseWrapper *mongoose = [[MongooseWrapper alloc] init ];
	self.webServer = mongoose;
	[mongoose release];
}

- (NSString *)imagePath {
	
	NSString *imgPath;
	imgPath = [NSString stringWithFormat:@"%@/%@", @"Documents",@"Images"];
	imgPath = [NSHomeDirectory() stringByAppendingPathComponent:imgPath];
	return imgPath;
}

#pragma mark DBLoginControllerDelegate methods

- (void)loginControllerDidLogin:(DBLoginController*)controller {
   

}

- (void)loginControllerDidCancel:(DBLoginController*)controller {
	[self.tableView reloadData];
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
    // Return YES for supported orientations
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}
*/


#pragma mark -
#pragma mark Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    // Return the number of sections.
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    // Return the number of rows in the section.
    return [servicesList count];
}

// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    static NSString *CellIdentifier = @"Cell";
    
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        cell = [[[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier] autorelease];
    }
    
    // Configure the cell...
    
	UISwitch *switchView = [[UISwitch alloc] init]; 
	
	[switchView setOn:NO ]; 
	
	
	if ([indexPath row] == 0) {
		[switchView addTarget:self action:@selector(switchWebServer:) forControlEvents:UIControlEventValueChanged]; 
	}
	
	
	if ([indexPath row] == 1) {
		[switchView addTarget:self action:@selector(switchDropBoxService:) forControlEvents:UIControlEventValueChanged]; 
	    [switchView setOn:[[DBSession sharedSession] isLinked]];
	}
	
	
	[cell setAccessoryView: switchView]; 
	[switchView release];
	switchView = nil; 
    cell.textLabel.text = [servicesList objectAtIndex:[indexPath row]];
	
    return cell;
}

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex {
	
	[webServer stop];
	[self.tableView reloadData];
}


-(void) switchWebServer:(id)sender {

		
		[webServer start:@"8080" documentRoot:[self imagePath] RecURI:NO];
		
		UIAlertView* alertView = [[UIAlertView alloc] initWithTitle:@"Web server" message:[NSString stringWithFormat:@"%@%@:%@", @"Premere close per chiudere la connessione:\n http://",[webServer getIPAddress],@"8080"] delegate:self cancelButtonTitle:@"Close" otherButtonTitles:nil];
		[alertView show];
		[alertView release];
}


-(void) switchDropBoxService:(id)sender {
	//MyPhotoVerifyAppDelegate *appDelegate = (MyPhotoVerifyAppDelegate *) [[UIApplication sharedApplication] delegate];

	
	if (![[DBSession sharedSession] isLinked]) {
	
		DBLoginController* controller = [[DBLoginController new] autorelease];
        controller.delegate = self;
        [controller presentFromController:self];
    } else {
	
		[[DBSession sharedSession] unlink];
        [[[[UIAlertView alloc] 
           initWithTitle:@"Account Scollegato!" message:@"Il tuo dropbox account è stato scollegato" 
           delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil]
          autorelease]
         show];
	}
	
	
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
        // Delete the row from the data source
        [tableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:YES];
    }   
    else if (editingStyle == UITableViewCellEditingStyleInsert) {
        // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
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
    // Navigation logic may go here. Create and push another view controller.
	/*
	 <#DetailViewController#> *detailViewController = [[<#DetailViewController#> alloc] initWithNibName:@"<#Nib name#>" bundle:nil];
     // ...
     // Pass the selected object to the new view controller.
	 [self.navigationController pushViewController:detailViewController animated:YES];
	 [detailViewController release];
	 */
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
	self.servicesList = nil;
	self.webServer = nil;
}


- (void)dealloc {
	[servicesList release];
	[webServer release];
    [super dealloc];
}


@end

