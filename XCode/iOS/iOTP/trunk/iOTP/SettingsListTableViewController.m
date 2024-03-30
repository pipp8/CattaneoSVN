//
//  SettingsListTableViewController.m
//  iOTP
//
//  Created by Giuseppe Cattaneo on 14/11/15.
//  Copyright © 2015 Giuseppe Cattaneo. All rights reserved.
//

#import "SettingsListTableViewController.h"

#import "AppSettings.h"

#import "SecureStoreManagement.h"

#import "RemoteSeedViewController.h"

#import  "AboutBoxViewController.h"

#import "InitSeedViewController.h"

#import "ScaledImage.h"
#import "SOLOptionsTransitionAnimator.h"
#import "SOLSlideTransitionAnimator.h"




// Segue Ids
static NSString * const kSegueAboutBoxPush   = @"AboutBoxSegueId";
static NSString * const kSegueRequestSeedPush= @"RemoteSeedSegueId";


@interface SettingsListTableViewController () <UIViewControllerTransitioningDelegate, UINavigationControllerDelegate>
@end



@implementation SettingsListTableViewController


- (void)viewDidLoad {
    [super viewDidLoad];
    
//    // set view background color
//    self.view.backgroundColor = GrayBackgroundColor;
//    // Initialize the toolbar sopra
//    self.navigationController.toolbarHidden = YES;
//    // self.navigationController.navigationBar.tintColor = BlueColor;
//    // self.navigationController.toolbar.tintColor = BlueColor;
//    
//    //set bar color
//    [self.navigationController.navigationBar setBarTintColor: BlueColor];
//    //optional, i don't want my bar to be translucent
//    [self.navigationController.navigationBar setTranslucent:NO];
    
    //set title and title color
//    // [self.navigationItem setTitle:@"Title"];
    
    UIImage *logo = [UIImage imageNamed:@"ID-BarIcon.png"];
    [self.navigationItem setTitleView: [[UIImageView alloc]
                                        initWithImage:[logo scaleImageToHeight: self.navigationController.navigationBar.frame.size.height]]];
    
    // Set this in every view controller so that the back button displays back instead of the root view controller name
    self.navigationItem.backBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:@" " style:UIBarButtonItemStylePlain target:nil action:nil];

//    [self.navigationController.navigationBar setTitleTextAttributes:[NSDictionary dictionaryWithObject:[UIColor whiteColor] forKey:UITextAttributeTextColor]];
//    //set back button color
//    [[UIBarButtonItem appearanceWhenContainedIn:[UINavigationBar class], nil] setTitleTextAttributes:[NSDictionary dictionaryWithObjectsAndKeys:[UIColor whiteColor], UITextAttributeTextColor,nil] forState:UIControlStateNormal];
//    //set back button arrow color
//    [self.navigationController.navigationBar setTintColor:[UIColor whiteColor]];
    
    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
    
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
    
    self.tableView.sectionHeaderHeight = 60;
    self.tableView.sectionFooterHeight = 0;

}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


-(void)doLoad {
//
}



-(void)viewWillDisappear:(BOOL)animated {
//    [self unshowPicker];
}

-(void)viewWillAppear:(BOOL)animated {
//    self.title = NSLocalizedString(@"SETTING-TITLE", nil);
    
    [self doLoad];
    
//    self.navigationItem.rightBarButtonItem = self.editButtonItem;
    
    [self.navigationController setToolbarHidden:YES animated:animated];
}



- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex{
    
    if (alertView.tag == 1) {
        
        if (buttonIndex == 0) {
            // abort resta nella view
        }
        else if (buttonIndex == 1){
            
            if ([AppSettings getUseSecureStore])
                [SecureStoreManagement deleteSeedFromSecureStore];
            else
                [AppSettings setEncryptedSeed: nil];
            
            [AppSettings setSeedAvailable:NO];
            [AppSettings setUsername: @""];
            [self.tableView reloadData];
        }
    }
}


- (void) removeSeed {

    UIAlertView *alert = [[UIAlertView alloc]
                          initWithTitle:NSLocalizedString(@"NOTIFY-TITLE", nil)
                          message:NSLocalizedString(@"ALREADYINITED-MSG", nil)
                          delegate:self cancelButtonTitle:NSLocalizedString(@"ABORT-BT", nil)
                          otherButtonTitles:NSLocalizedString(@"OK-BT", nil), nil];

    [alert setTag: 1];
    [alert show];
}


#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {

    return 3;
}


// Override to support conditional editing of the table view.
- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath {
    // Return NO if you do not want the specified item to be editable.
    if(indexPath.section == 0) {
        return YES;
    }
    
    return NO; // "Add Account" button
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section {
    switch(section) {
        case 0:
            return NSLocalizedString(@"SEEDMGT-MNU", nil);
        case 1:
            return NSLocalizedString(@"PINMGT-MNU", nil);
        case 2:
            return NSLocalizedString(@"HELP-MNU", nil);
        default:
            return @"";
    }
}

// Customize the number of rows in the table view.
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    switch(section) {
        case 0:
            return 1;
        case 1:
            return 1;
        case 2:
            return 1;
        default:
            return 0;
    }
}


// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    static NSString *CellIdentifier = @"Cell";
    
    UITableViewCell *cell;
    
    cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        // cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:CellIdentifier];
    }

    
    if(indexPath.section == 0) {

        cell.showsReorderControl = NO;
        CGSize correctSize = CGSizeMake(32,32);
        UIImage * ico;
        if (indexPath.row == 0) {
            
            if ([AppSettings getSeedAvailable]) {
                
                cell.textLabel.text = NSLocalizedString(@"REMOVESEED-MNU", nil);
                cell.detailTextLabel.text = [AppSettings getUsername];
                ico = [UIImage imageNamed:@"Stop-48.png"];
            }
            else {
                [AppSettings setHMACLength:8];  // manca tra i setting !!! lo faccio qui
                cell.textLabel.text = NSLocalizedString(@"CREATESEED-MNU", nil);
                cell.detailTextLabel.text = @"";
                ico = [UIImage imageNamed:@"Go-48.png"];
            }

            cell.imageView.image = [ico scaleImageToSize:correctSize];
        }
//        else if (indexPath.row == 1) {
//            
//            if ([AppSettings getSeedAvailable]) {
//                
//                cell.textLabel.text = NSLocalizedString(@"REMOVESEED-MNU", nil);
//                cell.detailTextLabel.text =[AppSettings getUsername];
//                ico = [UIImage imageNamed:@"Stop-48.png"];
//            }
//            else {
//                
//                cell.textLabel.text = NSLocalizedString(@"Inizializza l'App", nil);
//                cell.detailTextLabel.text = @"";
//                ico = [UIImage imageNamed:@"Go-48.png"];
//            }
//            
//            cell.imageView.image = [ico scaleImageToSize:correctSize];
//        }
        
    } else if(indexPath.section == 1) {
        
        if (indexPath.row == 0) {

            UISwitch *switchview = [[UISwitch alloc] initWithFrame:CGRectZero];
            if ([SecureStoreManagement isFingerPrintAvailable]) {
                
                [switchview setOn:[AppSettings getUseSecureStore]];
                [switchview setEnabled:YES];
            }
            else {
                // se non disponibile lo disabilita e basta
                [switchview setOn:NO];
                [switchview setEnabled:NO];
                [AppSettings setUseSecureStore:NO];
            }
            [switchview addTarget:self action:@selector(fingerprintSwitchChanged:) forControlEvents: UIControlEventValueChanged];
            cell.accessoryView = switchview;
            cell.textLabel.text = NSLocalizedString(@"TOUCHIDENABLE-MNU", nil);
            cell.imageView.image = nil;
            
            if ([SecureStoreManagement isFingerPrintAvailable]) {
                cell.selectionStyle = UITableViewCellSelectionStyleBlue;
                cell.userInteractionEnabled = YES;
            }
            else {
                cell.selectionStyle = UITableViewCellSelectionStyleNone;
                cell.userInteractionEnabled = NO;
                [switchview setEnabled: false];
            }
        }
//        else if (indexPath.row == 1) {
//            
//            UIStepper *stepper = [[UIStepper alloc]init];
//            [stepper addTarget:self action:@selector(incrementStepper:) forControlEvents:UIControlEventValueChanged];
//            [stepper setMinimumValue: 2.];
//            [stepper setMaximumValue: 8.];
//            [stepper setStepValue: 1.];
//            [stepper setValue: [AppSettings getOTPLength]];
//            
//            cell.accessoryView = stepper;
////            UILabel *label = [[UILabel alloc]init];
////            label.text = [NSString stringWithFormat:@"%.f", stepper.value];
//            cell.textLabel.text = [NSString stringWithFormat:@"%@%d", NSLocalizedString(@"OTPLEN-MNU", nil), (int) stepper.value];
//            cell.imageView.image = nil;
//            
//        }

    } else if(indexPath.section == 2){

        if(indexPath.row == 0) {
            cell.textLabel.text = NSLocalizedString(@"INFO-MNU", nil);
            cell.imageView.image = nil;
        }
    }

    return cell;
}



-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    // deselect
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    
    if (indexPath.section == 0) {
        
        if (indexPath.row == 0) {
        
            if ([AppSettings getSeedAvailable]) {
                // cancella un seed già creato
                [self removeSeed];
            }
            else {
//          RemoteSeedViewController * vc = [self.storyboard instantiateViewControllerWithIdentifier:@"RemoteInitNavSBId"];
//          [self.navigationController presentViewController:vc animated:YES completion:nil];
//          [self.navigationController pushViewController:vc animated:YES];
            
                [self performSegueWithIdentifier: kSegueRequestSeedPush sender: self];
            }
        }
//        else if (indexPath.row == 1) {
//                
//            if ([AppSettings getSeedAvailable]) {
//                // cancella un seed già creato
//                [self removeSeed];
//            }
//            else {
//
//                [self performSegueWithIdentifier: @"LocalSeedSegueId" sender: self];
//            }
//        }
    }
    else if(indexPath.section == 1) {

    }
    else if (indexPath.section == 2) {
        
//        RemoteSeedViewController * vc = [self.storyboard instantiateViewControllerWithIdentifier:@"AboutBoxNavSBId"];
//        [self.navigationController presentViewController:vc animated:YES completion:nil];

        [self performSegueWithIdentifier: kSegueAboutBoxPush sender: self];
    }
}



// Override to support editing the table view.
- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
    
//    //prendo l'indice effettivo dell'account
//    int accIndex = [[self.accountIndices objectAtIndex:indexPath.row] intValue];
//    // stop showing edit button if there are no more accounts
//    if([self.accountIndices count] == 0) {
//        self.navigationItem.rightBarButtonItem = nil;
//    }
}



- (void) fingerprintSwitchChanged:(UISwitch *)secureStoreSwitch {
    
    if ([AppSettings getUseSecureStore] && ![secureStoreSwitch isOn]) {
        
        // sto passando da secure store a store normale con PIN
        if ([AppSettings getSeedAvailable]) {
            if (![self.mainVC moveToPIN])
                NSLog(@"Errore nel cambio store da standard a secure");
        }
    }
    else if (![AppSettings getUseSecureStore] && [secureStoreSwitch isOn]) {
        
        // sto passando da store normale con PIN a secure store
        if ([AppSettings getSeedAvailable]) {
            if (![self.mainVC moveToSecureStore])
                NSLog(@"Errore nel cambio store da secure a standard");
        }
    }
    //Setto cmq il nuovo valore lo stato potrebbe essere cambiato prima di generare il seed
    [AppSettings setUseSecureStore: [secureStoreSwitch isOn]];
}


- (void) incrementStepper:(UIStepper *)sender {
    // al momento ce n'e' uno solo
    NSIndexPath* path = [NSIndexPath indexPathForRow:1 inSection:1];
    NSArray<NSIndexPath *>  * paths = [NSArray arrayWithObject: path];

    [AppSettings setOTPLength: (int) sender.value];
    
    [self.tableView reloadRowsAtIndexPaths: paths withRowAnimation:UITableViewRowAnimationNone];
}

/*
// Override to support editing the table view.
- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        // Delete the row from the data source
        [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
    } else if (editingStyle == UITableViewCellEditingStyleInsert) {
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


#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    
    // AboutBo - push
    if ([segue.identifier isEqualToString:kSegueAboutBoxPush]) {

        self.navigationController.delegate = self;
        UIViewController *toVC = segue.destinationViewController;
        toVC.transitioningDelegate = self;
    }
    // RemoteSeed Enrollment - push
    else if ([segue.identifier isEqualToString:kSegueRequestSeedPush]) {
        
        self.navigationController.delegate = self;
        UIViewController *toVC = segue.destinationViewController;
        toVC.transitioningDelegate = self;
    }

    //    else if ([segue.identifier isEqualToString:kSegueSlideModal]) {
    //        UIViewController *toVC = segue.destinationViewController;
    //        toVC.transitioningDelegate = self;
    //    }
    
    [super prepareForSegue:segue sender:sender];
}




#pragma mark - UIViewControllerTransitioningDelegate

/*
 Called when presenting a view controller that has a transitioningDelegate
 */
- (id<UIViewControllerAnimatedTransitioning>)animationControllerForPresentedController:(UIViewController *)presented
                                                                  presentingController:(UIViewController *)presenting
                                                                      sourceController:(UIViewController *)source
{
    id<UIViewControllerAnimatedTransitioning> animationController;
    
    // Options modal
    //    if ([presented isKindOfClass:[UINavigationController class]] &&
    //        [((UINavigationController *)presented).topViewController isKindOfClass:[SettingsListTableViewController class]]) {
    //
    //        SOLOptionsTransitionAnimator *animator = [[SOLOptionsTransitionAnimator alloc] init];
    //        animator.appearing = YES;
    //        animator.duration = 0.35;
    //        animationController = animator;
    //    }
    // AboutBox Push
    if ([presented isKindOfClass:[AboutBoxViewController class]]) {
        SOLSlideTransitionAnimator *animator = [[SOLSlideTransitionAnimator alloc] init];
        animator.appearing = YES;
        animator.duration = kDuration;
        animator.edge = SOLEdgeRight;
        animationController = animator;
    }
    // RemoteSeed Enrollment
    else if ([presented isKindOfClass:[RemoteSeedViewController class]]) {
        SOLSlideTransitionAnimator *animator = [[SOLSlideTransitionAnimator alloc] init];
        animator.appearing = YES;
        animator.duration = kDuration;
        animator.edge = SOLEdgeRight;
        animationController = animator;
    }
    
    return animationController;
}



/*
 Called when dismissing a view controller that has a transitioningDelegate
 */
- (id<UIViewControllerAnimatedTransitioning>)animationControllerForDismissedController:(UIViewController *)dismissed
{
    id<UIViewControllerAnimatedTransitioning> animationController;
    
    // Options modal
    //    if ([dismissed isKindOfClass:[UINavigationController class]] &&
    //        [((UINavigationController *)dismissed).topViewController isKindOfClass:[SettingsListTableViewController class]]) {
    //
    //        SOLOptionsTransitionAnimator *animator = [[SOLOptionsTransitionAnimator alloc] init];
    //        animator.appearing = NO;
    //        animator.duration = 0.35;
    //        animationController = animator;
    //    }
    // Options push
    if ([dismissed isKindOfClass:[AboutBoxViewController class]]){
        SOLSlideTransitionAnimator *animator = [[SOLSlideTransitionAnimator alloc] init];
        animator.appearing = NO;
        animator.duration = kDuration;
        animator.edge = SOLEdgeRight;
        animationController = animator;
    }
    // Slide
    else if ([dismissed isKindOfClass:[RemoteSeedViewController class]]) {
        SOLSlideTransitionAnimator *animator = [[SOLSlideTransitionAnimator alloc] init];
        animator.appearing = NO;
        animator.duration = kDuration;
        animator.edge = SOLEdgeRight;
        animationController = animator;
    }
    
    return animationController;
}



#pragma mark - UINavigationControllerDelegate

/*
 Called when pushing/popping a view controller on a navigation controller that has a delegate
 */
- (id<UIViewControllerAnimatedTransitioning>)navigationController:(UINavigationController *)navigationController
                                  animationControllerForOperation:(UINavigationControllerOperation)operation
                                               fromViewController:(UIViewController *)fromVC
                                                 toViewController:(UIViewController *)toVC
{
    id<UIViewControllerAnimatedTransitioning> animationController;
    
    
    // AboutBox - Push
    if ([toVC isKindOfClass:[AboutBoxViewController class]] && operation == UINavigationControllerOperationPush) {
        SOLSlideTransitionAnimator *animator = [[SOLSlideTransitionAnimator alloc] init];
        animator.appearing = YES;
        animator.duration = kDuration;
        animator.edge = SOLEdgeRight;
        animationController = animator;
    }
    // AboutBox - Pop
    else if ([fromVC isKindOfClass:[AboutBoxViewController class]] && operation == UINavigationControllerOperationPop) {
        SOLSlideTransitionAnimator *animator = [[SOLSlideTransitionAnimator alloc] init];
        animator.appearing = NO;
        animator.duration = kDuration;
        animator.edge = SOLEdgeRight;
        animationController = animator;
    }
    // RemoteSeedEnrollment - Push
    if ([toVC isKindOfClass:[RemoteSeedViewController class]] && operation == UINavigationControllerOperationPush) {
        SOLSlideTransitionAnimator *animator = [[SOLSlideTransitionAnimator alloc] init];
        animator.appearing = YES;
        animator.duration = kDuration;
        animator.edge = SOLEdgeRight;
        animationController = animator;
    }
    // RemoteSeedEnrollment - Pop
    else if ([fromVC isKindOfClass:[RemoteSeedViewController class]] && operation == UINavigationControllerOperationPop) {
        SOLSlideTransitionAnimator *animator = [[SOLSlideTransitionAnimator alloc] init];
        animator.appearing = NO;
        animator.duration = kDuration;
        animator.edge = SOLEdgeLeft;
        animationController = animator;
    }
    
    return animationController;
}



#pragma mark - Storyboard unwinding

/*
 Unwind segue action called to dismiss the Options and Drop view controllers and
 when the Slide, Bounce and Fold view controllers are dismissed with a single tap.
 
 Normally an unwind segue will pop/dismiss the view controller but this doesn't happen
 for custom modal transitions so we have to manually call dismiss.
 */

// solo per le modali
//- (IBAction)unwindToMainVC:(UIStoryboardSegue *)sender
//{
//    if ([sender.identifier isEqualToString:kSegueOptionsDismiss]) {
//        [self dismissViewControllerAnimated:YES completion:nil];
//    }
//}


- (IBAction)unwindToSetting:(UIStoryboardSegue *)segue {
    
    NSLog(@"Unwinding from %@", [segue identifier]);
}

@end
