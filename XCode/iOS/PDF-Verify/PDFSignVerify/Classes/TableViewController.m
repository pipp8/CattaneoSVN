//
//  PtotoTableViewController.m
//  MyPhotoVerify
//
//  Created by Antonio De Marco on 31/07/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "TableViewController.h"
#import "PDFViewController.h"
#import "PDFSignVerifyAppDelegate.h"

@implementation TableViewController
@synthesize restClient;
@synthesize fileManager;
@synthesize directoryContent;
@synthesize timer;
@synthesize fileCount;
@synthesize lblProgress;
@synthesize DBDocPath;


#pragma mark -
#pragma mark View lifecycle

- (void)viewDidLoad {
    [super viewDidLoad];
	
	self.title = @"Documenti PDF";
	fileManager = [[NSFileManager alloc] init ];

	UIBarButtonItem *editButton = [[UIBarButtonItem alloc] initWithTitle:@"Edit" style:UIBarButtonItemStyleBordered target:self action:@selector(toggleEdit:)];
	self.navigationItem.rightBarButtonItem = editButton; 
	[self.navigationItem.rightBarButtonItem setEnabled:FALSE];
	[editButton release];

	self.fileCount = 0;
	self.timer = [NSTimer scheduledTimerWithTimeInterval: 10.0  target: self	selector: @selector(onTimer:) userInfo: nil	 repeats: YES];
    CGRectMake(lblProgress.frame.origin.x,lblProgress.frame.origin.y,lblProgress.frame.size.width,0);
	lblProgress.frame = CGRectMake(lblProgress.frame.origin.x,lblProgress.frame.origin.y,lblProgress.frame.size.width,0);

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
	
	[self setDBDocPath:[NSString stringWithFormat:@"%@/%@", DBRootPath, docDir]];
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
				
		[self.restClient loadMetadata:DBDocPath withHash:nil];
	}
}




-(UIImage *)generatePhotoThumbnail:(UIImage *)image {
	// Create a thumbnail version of the image for the event object.
	CGSize size = image.size;
	CGSize croppedSize;
	CGFloat ratio = 64.0;
	CGFloat offsetX = 0.0;
	CGFloat offsetY = 0.0;
	
	// check the size of the image, we want to make it
	// a square with sides the size of the smallest dimension
	if (size.width > size.height) {
		offsetX = (size.height - size.width) / 2;
		croppedSize = CGSizeMake(size.height, size.height);
	} else {
		offsetY = (size.width - size.height) / 2;
		croppedSize = CGSizeMake(size.width, size.width);
	}
	
	// Crop the image before resize
	CGRect clippedRect = CGRectMake(offsetX * -1, offsetY * -1, croppedSize.width, croppedSize.height);
	CGImageRef imageRef = CGImageCreateWithImageInRect([image CGImage], clippedRect);
	// Done cropping
	
	// Resize the image
	CGRect rect = CGRectMake(0.0, 0.0, ratio, ratio);
	
	UIGraphicsBeginImageContext(rect.size);
	[[UIImage imageWithCGImage:imageRef] drawInRect:rect];
	UIImage *thumbnail = UIGraphicsGetImageFromCurrentImageContext();
	UIGraphicsEndImageContext();
	// Done Resizing
	
	return thumbnail;
}



#pragma mark DBRestClientDelegate methods

- (void)restClient:(DBRestClient*)client loadedMetadata:(DBMetadata*)metadata {
    PDFSignVerifyAppDelegate *appDelegate = (PDFSignVerifyAppDelegate *) [[UIApplication sharedApplication] delegate];
	
    NSArray* validExtensions = [NSArray arrayWithObjects:@"pdf", nil];
    NSMutableArray* newImagePaths = [NSMutableArray new];
	NSMutableArray* removableImagePaths = [NSMutableArray new];
    for (DBMetadata* child in metadata.contents) {
    	NSString* extension = [[child.path pathExtension] lowercaseString];
        
		
		if (!child.isDirectory && [validExtensions indexOfObject:extension] != NSNotFound) {
            NSString *fileName;
			NSString *fileNameAtPath;
			fileName = [child.path lastPathComponent] ;
			fileNameAtPath = [NSString stringWithFormat:@"%@/%@", [appDelegate docPath],fileName];
		
			if (![fileManager fileExistsAtPath:fileNameAtPath]) {
				[newImagePaths addObject:child.path]; 
			} else {
				[removableImagePaths addObject:child.path];
			}
			
			
        }
    }
	
	Boolean trovato = NO;
	Boolean go = YES;
	for (NSInteger i= 0; i< [directoryContent count]; i++) {
		NSString *file = [directoryContent objectAtIndex:i];
		file = [file lastPathComponent];
		for (NSInteger j=0; j< [removableImagePaths count]; j++) {
			NSString *file2 = [removableImagePaths objectAtIndex:j];
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
		    [fileManager removeItemAtPath:[NSString stringWithFormat:@"%@/%@",[appDelegate docPath],file] error:nil];
			[self.tableView reloadData];
			
		} 
		
	}
	
	if (go) {
		NSString *fileName;
		NSString *fileNameAtPath;
		for (NSInteger i=0; i< [newImagePaths count]; i++) {
			fileName = [[newImagePaths objectAtIndex:i] lastPathComponent] ;
			fileNameAtPath = [NSString stringWithFormat:@"%@/%@", appDelegate.docPath,fileName];
			fileCount++;
			[self.restClient loadFile:[newImagePaths objectAtIndex:i] intoPath:fileNameAtPath];
			break;
			
		}
	}
		
	[newImagePaths release];
	[removableImagePaths release];
}

- (void)restClient:(DBRestClient*)client metadataUnchangedAtPath:(NSString*)path {
 //   [self loadRandomPhoto];
}

- (void)restClient:(DBRestClient*)client loadMetadataFailedWithError:(NSError*)error {
    NSLog(@"restClient:loadMetadataFailedWithError: %@", [error localizedDescription]);
	//Probabilemente il path non esiste e provo a crearlo
	[self.restClient createFolder:DBDocPath];
	fileCount = 0;
}

- (void)restClient:(DBRestClient *)client deletePathFailedWithError:(NSError *)error {
	
	[[[[UIAlertView alloc] 
       initWithTitle:@"Errore" message:@"Impossibile cancellare il documento."
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
	
	[fileManager removeItemAtPath:[NSString stringWithFormat:@"%@/%@",appDelegate.docPath,fileName] error:nil];
    
	NSString *pngPath = [NSString stringWithFormat:@"%@/%@.png",appDelegate.docPath,[fileName stringByDeletingPathExtension]];
	
	[fileManager removeItemAtPath:pngPath error:nil];
	
	fileCount--;
	
	if (fileCount <= 0)
		fileCount = 0;
	[self toggleEdit:nil];
	[self.tableView reloadData];
	
}

- (void)restClient:(DBRestClient *)client loadProgress:(CGFloat)progress forFile:(NSString *)destPath {
	lblProgress.frame = CGRectMake(lblProgress.frame.origin.x,lblProgress.frame.origin.y,lblProgress.frame.size.width,30);
	
	NSString *value =  [NSString stringWithFormat:@"%.2f",progress];
	float newvalue = [value floatValue];
	int intvalue = newvalue*100;
	
	lblProgress.text = [NSString stringWithFormat:@"Sto scaricando %@: (%i%%)",[destPath lastPathComponent],intvalue];
	
	
}

- (void)restClient:(DBRestClient *)client loadedFile:(NSString *)destPath {

	
	UIImage *image = [[UIImage alloc] initWithContentsOfFile:destPath];
	
	UIImage *thumb = [self generatePhotoThumbnail:image];
	[image release];
	NSData *imageData = [NSData dataWithData:UIImagePNGRepresentation(thumb)];
	

	[imageData writeToFile:[NSString stringWithFormat:@"%@.png",[destPath stringByDeletingPathExtension]] atomically:YES];
			
	
	fileCount--;
	if (fileCount <= 0)
		fileCount = 0;
	lblProgress.frame = CGRectMake(lblProgress.frame.origin.x,lblProgress.frame.origin.y,lblProgress.frame.size.width,0);
	lblProgress.text = @"";
	[self.tableView reloadData];
}


- (void)restClient:(DBRestClient*)client loadFiledWithError:(NSError*)error {
  
	[[[[UIAlertView alloc] 
       initWithTitle:@"Errore" message:@"Impossibile trasferire il documento."
       delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil]
      autorelease]
     show];

	fileCount--;
	if (fileCount <= 0)
		fileCount = 0;
	
	lblProgress.frame = CGRectMake(lblProgress.frame.origin.x,lblProgress.frame.origin.y,lblProgress.frame.size.width,0);
	lblProgress.text = @"";
	
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
	// Return the number of rows in the section.
	NSString *docPath = [appDelegate docPath];   
	
	NSLog( @"%@", docPath);
	NSMutableArray *array = [[NSMutableArray alloc] initWithArray:[[fileManager contentsOfDirectoryAtPath:docPath error:nil]
							filteredArrayUsingPredicate:[NSPredicate predicateWithFormat:@"self ENDSWITH '.pdf'"] ]]; 
	
	self.directoryContent =   array;
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
        cell = [[[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier] autorelease];
    }
    
    // Configure the cell...
    
	cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
	cell.textLabel.text = [[directoryContent objectAtIndex:[indexPath row]] stringByDeletingPathExtension  ];
	
	cell.imageView.image = [UIImage imageNamed:@"photo.png"];
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
  

	PDFViewController *pdfViewController= [[PDFViewController alloc] initWithNibName:@"PDFViewController" bundle:nil];
	[pdfViewController setPdfFile:[directoryContent objectAtIndex:[indexPath row]]];
	[self.navigationController pushViewController:pdfViewController animated:YES];
	[pdfViewController release];

	
		
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
	self.lblProgress = nil;
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
		[self.restClient deletePath:[NSString stringWithFormat:@"%@/%@", DBDocPath,
									 [directoryContent objectAtIndex:[indexPath row]]]];
		
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
    [directoryContent release];
	[restClient release];
	[fileManager release];
	[lblProgress release];
    [super dealloc];
}


@end

