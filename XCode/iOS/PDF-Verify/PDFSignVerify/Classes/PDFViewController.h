//
//  ImageViewController.h
//  MyPhotoVerify
//
//  Created by Antonio De Marco on 01/08/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PdfSigUtils.h"
#import "PDFSignVerifyAppDelegate.h"

@class DBRestClient;

@interface PDFViewController : UIViewController {
	IBOutlet UIImageView *image; 
	IBOutlet UIWebView *webView;
	NSString *imageFile;
	NSString *pdfFile;
	CFURLRef pdfURL;
	Boolean isView; 
	UIBarButtonItem *VerificaConLib;
	
	DBRestClient* restClient;
	NSFileManager *fileManager;
	NSMutableArray *directoryContent;
	
	
	
@private
	CGPDFDocumentRef pdf;
	
	PdfSigUtils *pdf2;

}
@property (nonatomic,retain) IBOutlet UIImageView *image;
@property (nonatomic,retain) IBOutlet UIWebView *webView;
@property (nonatomic,retain) NSString *imageFile;
@property (nonatomic,retain) NSString *pdfFile;
@property (nonatomic,retain) PdfSigUtils *pdf2;
@property (nonatomic,retain) NSMutableArray *directoryContent;
@property (nonatomic, retain) NSFileManager *fileManager;

-(IBAction)VerificaConLib:(id) sender;
-(IBAction)FirmaCADESConLib:(id) sender;

@end
