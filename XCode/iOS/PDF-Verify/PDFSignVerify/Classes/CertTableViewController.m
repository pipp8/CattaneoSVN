//
//  CertTableViewController.m
//  MyPhotoVerify
//
//  Created by Antonio De Marco on 01/08/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "CertTableViewController.h"
#import "OpenSSLX509.h"
#import "PDFSignVerifyAppDelegate.h"

@implementation CertTableViewController
@synthesize restClient;
@synthesize fileManager;
@synthesize directoryContent;
@synthesize timer;
@synthesize fileCount;
@synthesize DBCertPath;

#pragma mark -
#pragma mark View lifecycle



- (void)viewDidLoad {
    [super viewDidLoad];

	self.title = @"Certificati";
	fileManager = [[NSFileManager alloc] init ];
	
	
	UIBarButtonItem *editButton = [[UIBarButtonItem alloc] initWithTitle:@"Edit" style:UIBarButtonItemStyleBordered target:self action:@selector(toggleEdit:)];
	self.navigationItem.rightBarButtonItem = editButton; 
	[self.navigationItem.rightBarButtonItem setEnabled:FALSE];
	[editButton release];
	
	self.fileCount = 0;
	self.timer = [NSTimer scheduledTimerWithTimeInterval: 10.0  target: self	selector: @selector(onTimer:) userInfo: nil	 repeats: YES];
	
	NSString *docDir;
	NSString *certDir;
	NSString *DBRootPath;
	
	NSBundle* myBundle = [NSBundle mainBundle];
	
	// If we succeeded, look for our property.
	if ( myBundle != NULL ) {
		docDir = [myBundle objectForInfoDictionaryKey:@"DocumentsDir"];
		certDir = [myBundle objectForInfoDictionaryKey:@"CertificateDir"];
		DBRootPath = [myBundle objectForInfoDictionaryKey:@"DBRootPath"];
	}
	
	[self setDBCertPath:[NSString stringWithFormat:@"%@/%@", DBRootPath, certDir]];	
}

-(IBAction)toggleEdit:(id)sender {
	
	[self.tableView setEditing:!self.tableView.editing animated:YES];
	if (self.tableView.editing)
		[self.navigationItem.rightBarButtonItem setTitle:@"Done"];
	else
		[self.navigationItem.rightBarButtonItem setTitle:@"Edit"];
	
}

-(void)onTimer:(NSTimer *)timer {
	
	if (([[DBSession sharedSession] isLinked]) && fileCount == 0) {
		[self.restClient loadMetadata:DBCertPath withHash:nil];
	}
}



/*
- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
	[self onBtnDropBox];
}
*/

#pragma mark DBRestClientDelegate methods

- (void)restClient:(DBRestClient*)client loadedMetadata:(DBMetadata*)metadata {
	
	PDFSignVerifyAppDelegate *appDelegate = (PDFSignVerifyAppDelegate *) [[UIApplication sharedApplication] delegate];
	    	
    NSArray* validExtensions = [NSArray arrayWithObjects:@"crt", nil];
    NSMutableArray* newCertPaths = [NSMutableArray new];
	NSMutableArray* removableCertPaths = [NSMutableArray new];
    for (DBMetadata* child in metadata.contents) {
    	NSString* extension = [[child.path pathExtension] lowercaseString];

		
		if (!child.isDirectory && [validExtensions indexOfObject:extension] != NSNotFound) {
            NSString *fileName;
			NSString *fileNameAtPath;
			fileName = [child.path lastPathComponent] ;
			fileNameAtPath = [NSString stringWithFormat:@"%@/%@", [appDelegate certPath],fileName];
			
			
			
			if (![fileManager fileExistsAtPath:fileNameAtPath]) {
				[newCertPaths addObject:child.path]; 
			} else {
				[removableCertPaths addObject:child.path];
			}

			
        }
    }
   
	Boolean trovato = NO;
	Boolean go = YES;
	for (NSInteger i= 0; i< [directoryContent count]; i++) {
		NSString *file = [directoryContent objectAtIndex:i];
		file = [file lastPathComponent];
		for (NSInteger j=0; j< [removableCertPaths count]; j++) {
			NSString *file2 = [removableCertPaths objectAtIndex:j];
			file2 = [file2 lastPathComponent];
			if (![file isEqualToString:file2]) {
			   	trovato = NO;		
			} else {
				trovato = YES;
				break;
			}

			    		
			
		}
		
		if (!trovato) {
		    go = NO;
		 	 [fileManager removeItemAtPath:[NSString stringWithFormat:@"%@/%@",[appDelegate certPath],file] error:nil];
			[self.tableView reloadData];
			
		} 

	}
	
	if (go) {
		NSString *fileName;
		NSString *fileNameAtPath;
		for (NSInteger i=0; i< [newCertPaths count]; i++) {
			fileName = [[newCertPaths objectAtIndex:i] lastPathComponent] ;
			fileNameAtPath = [NSString stringWithFormat:@"%@/%@", [appDelegate certPath],fileName];
			fileCount++;
			[self.restClient loadFile:[newCertPaths objectAtIndex:i] intoPath:fileNameAtPath];
			break;
		}
	}
	

		
	
	[newCertPaths release];
	[removableCertPaths release];
}



- (void)restClient:(DBRestClient*)client metadataUnchangedAtPath:(NSString*)path {
	//   [self loadRandomPhoto];
}

- (void)restClient:(DBRestClient*)client loadMetadataFailedWithError:(NSError*)error {
    //NSLog(@"restClient:loadMetadataFailedWithError: %@", [error localizedDescription]);
	//Probabilemente il path non esiste e provo a crearlo
	[self.restClient createFolder:DBCertPath];
	fileCount = 0;
	
}

- (void)restClient:(DBRestClient *)client deletePathFailedWithError:(NSError *)error {

	[[[[UIAlertView alloc] 
       initWithTitle:@"Errore" message:@"Impossibile cancellare il certificato."
       delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil]
      autorelease]
     show];
	fileCount--;
	
	if (fileCount <= 0)
		fileCount = 0;
	[self toggleEdit:nil];

}

- (void)restClient:(DBRestClient *)client deletedPath:(NSString *)path {
	PDFSignVerifyAppDelegate *appDelegate = (PDFSignVerifyAppDelegate *) [[UIApplication sharedApplication] delegate];
	NSString *fileName = [path lastPathComponent];
	
	[fileManager removeItemAtPath:[NSString stringWithFormat:@"%@/%@",[appDelegate certPath],fileName] error:nil];
     
	fileCount--;
	
	if (fileCount <= 0)
		fileCount = 0;
	[self toggleEdit:nil];
	[self.tableView reloadData];
	
}
	
	
- (void)restClient:(DBRestClient *)client loadedFile:(NSString *)destPath {

		
	fileCount--;
	
	if (fileCount <= 0)
		fileCount = 0;
	
	
	[self.tableView reloadData];
}


- (void)restClient:(DBRestClient*)client loadFiledWithError:(NSError*)error {
	
	[[[[UIAlertView alloc] 
       initWithTitle:@"Errore" message:@"Impossibile scaricare il certificato."
       delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil]
      autorelease]
     show];
	fileCount--;
	
	if (fileCount <= 0)
		fileCount = 0;
	
}




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
   PDFSignVerifyAppDelegate *appDelegate = (PDFSignVerifyAppDelegate *) [[UIApplication sharedApplication] delegate];
	
	NSString * certPath = [appDelegate certPath];
	
	// Return the number of rows in the section.
	NSMutableArray *array = [[NSMutableArray alloc] initWithArray:[[fileManager contentsOfDirectoryAtPath:[appDelegate certPath]
					error:nil] filteredArrayUsingPredicate:[NSPredicate predicateWithFormat:@"self ENDSWITH '.crt'"] ]]; 
	
	self.directoryContent =   array  ;
	[array release];
	
	
	[self setEnabledEditButton];
	return [directoryContent count];
	
}


// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
	PDFSignVerifyAppDelegate *appDelegate = (PDFSignVerifyAppDelegate *) [[UIApplication sharedApplication] delegate];
    static NSString *CellIdentifier = @"Cell";
    
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        cell = [[[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:CellIdentifier] autorelease];
    }
    
    // Configure the cell...
    
	OpenSSLX509 *x509Sec = [[OpenSSLX509 alloc] init ];
	
	X509 *cert = [x509Sec loadCertificateWithPath:[NSString stringWithFormat:@"%@/%@",[appDelegate certPath],[directoryContent objectAtIndex:[indexPath row]]]];
	
	if (cert != nil) {
								
		cell.textLabel.text  = [x509Sec getCertSubject:cert Parameter:@"CN"];
		
		NSDateFormatter *dateFormat = [[NSDateFormatter alloc] init];
		
		[dateFormat setDateFormat:@"dd/MM/yyyy"];
		cell.detailTextLabel.text = [NSString stringWithFormat:@"Validità Da:%@ A:%@",[dateFormat stringFromDate:[x509Sec getCertStartValidity:cert]],[dateFormat stringFromDate:[x509Sec getCertEndValidity:cert]]];
	    [dateFormat release];
		[x509Sec FreeX509Struct:cert];
		
				
	} else {
	
	   cell.textLabel.text = [[directoryContent objectAtIndex:[indexPath row]] stringByDeletingPathExtension  ];
	
	}			  
	
	//cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
		
	cell.imageView.image = [UIImage imageNamed:@"certificato.jpg"];
	
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
    self.fileManager = nil;
	[timer invalidate];

	

}

- (DBRestClient*)restClient {
    if (restClient == nil) {
    	restClient = [[DBRestClient alloc] initWithSession:[DBSession sharedSession]];
    	restClient.delegate = self;
    }
    return restClient;
}

- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
   	
		
	 if (editingStyle == UITableViewCellEditingStyleDelete) {
		 fileCount++;
		 [self.restClient deletePath:[NSString stringWithFormat:@"%@/%@",DBCertPath , [directoryContent objectAtIndex:[indexPath row]]]];
				 
		// [self.tableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationFade];
		 
	}
	
	
	//[self setEnabledEditButton];
	//[self.tableView reloadData];


}

-(void)setEnabledEditButton {

	Boolean isEditEnabled=NO;
	isEditEnabled = ([directoryContent count] > 0);
	if (isEditEnabled) {
		[self.navigationItem.rightBarButtonItem setEnabled:TRUE];
		
	} else {
	    [self.tableView setEditing:FALSE animated:YES];
		[self.navigationItem.rightBarButtonItem setTitle:@"Edit"];
		[self.navigationItem.rightBarButtonItem setEnabled:FALSE];
	}
	
}

- (void)dealloc {
	[timer invalidate];
	[timer release];
	timer = nil;
    [directoryContent release];
	[restClient release];
	[fileManager release];
	
	
    [super dealloc];
}



@end

