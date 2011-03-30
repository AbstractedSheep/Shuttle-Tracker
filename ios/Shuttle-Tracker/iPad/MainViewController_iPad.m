//
//  MainViewController_iPad.m
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 3/21/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import "MainViewController_iPad.h"
#import "MapViewController.h"


@implementation MainViewController_iPad

@synthesize mapViewController;

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
	self.view = [[UIView alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
	
	mapViewController = [[MapViewController alloc] init];
    mapViewController.dataManager = dataManager;
	
	[self.view addSubview:mapViewController.view];
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
