//
//  MainViewController_iPad.m
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 3/21/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import "MainViewController_iPad.h"
#import "MapViewController.h"
#import "EtasViewController.h"


@implementation MainViewController_iPad

@synthesize toolbar;
@synthesize mapViewController;
@synthesize etaPopover;

/*
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}
*/

- (void)dealloc
{
    if (toolbar) {
        [toolbar release];
    }
	if (mapViewController) {
		[mapViewController release];
	}

    [super dealloc];
}

- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - View lifecycle


// Implement loadView to create a view hierarchy programmatically, without using a nib.
- (void)loadView
{
    self.view = [[[UIView alloc] initWithFrame:[[UIScreen mainScreen] applicationFrame]] autorelease];
    
    UIToolbar *topBar = [[UIToolbar alloc] init];
	topBar.barStyle = UIBarStyleDefault;
	
	[topBar sizeToFit];
	topBar.autoresizingMask = UIViewAutoresizingFlexibleWidth;
    
    //	Set the toolbar's frame to be positioned at the top of the screen with the correct size
	CGRect rect = [[UIScreen mainScreen] bounds];
	
	CGRect toolbarFrame = [topBar frame];
	toolbarFrame.size.width = rect.size.width;
	topBar.frame = toolbarFrame;

    //  Title label
    UIBarButtonItem *barLabel = [[UIBarButtonItem alloc] initWithTitle:@"RPI Shuttles" style:UIBarButtonItemStylePlain target:nil action:nil];

	//  Flexible spacer so that the entire toolbar is occupied
	UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
    
    EtasViewController *etasVC = [[EtasViewController alloc] init];
    etasVC.dataManager = dataManager;
    
    //  Show ETAs in a popover on pressing this button
    UIBarButtonItem *etaButton = [[UIBarButtonItem alloc] initWithTitle:@"ETAs" style:UIBarButtonItemStyleBordered target:self action:@selector(showEtas:)];
    
    //  Add a fixed space to keep the bar label centered
    UIBarButtonItem *fixedSpace = [[[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFixedSpace target:nil action:NULL] autorelease];
    fixedSpace.width = 56;
    
    UINavigationController *etasTableNavController = [[UINavigationController alloc] init];
	etasTableNavController.viewControllers = [NSArray arrayWithObjects:etasVC, nil];
	[etasVC release];
	
	//	Note that this class (MainViewController_iPad) gets a reference to timeDisplayFormatter
	//	via the init method of its superclass, MainViewController.
	etasVC.timeDisplayFormatter = timeDisplayFormatter;
    
    self.etaPopover = [[[UIPopoverController alloc] initWithContentViewController:etasTableNavController] autorelease];
    [etasTableNavController release];
    
    [topBar setItems:[NSArray arrayWithObjects:fixedSpace, flexibleSpace, barLabel, flexibleSpace, etaButton, nil]];
    [flexibleSpace release];
    [barLabel release];
    [etaButton release];
    
    self.toolbar = topBar;
    [topBar release];
    
    self.mapViewController = [[[MapViewController alloc] init] autorelease];
    
    mapViewController.dataManager = dataManager;
	
	[self.view addSubview:self.mapViewController.view];
    [self.view addSubview:self.toolbar];
}

- (void)showEtas:(id)sender
{
	if (![etaPopover isPopoverVisible]) {
		[etaPopover presentPopoverFromBarButtonItem:sender permittedArrowDirections:UIPopoverArrowDirectionUp animated:YES];
	}
	else {
		[etaPopover dismissPopoverAnimated:YES];
	}
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
	return YES;
}

@end
