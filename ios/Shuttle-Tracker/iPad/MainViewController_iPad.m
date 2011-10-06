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
@synthesize splitViewController;
@synthesize mapViewController;
@synthesize etasViewController;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        etasViewController = [[EtasViewController alloc] init];
        etasViewController.dataManager = dataManager;
        
        //	Note that this class (MainViewController_iPad) gets a reference to timeDisplayFormatter
        //	via the init method of its superclass, MainViewController.
        etasViewController.timeDisplayFormatter = timeDisplayFormatter;
        etasViewController.title = @"ETAs";
        
        UINavigationController *etasTableNavController = [[UINavigationController alloc] init];
        etasTableNavController.viewControllers = [NSArray arrayWithObjects:etasViewController, nil];
        [etasViewController release];
        
        mapViewController = [[MapViewController alloc] init];
        mapViewController.dataManager = dataManager;
        
        splitViewController = [[UISplitViewController alloc] init];
        splitViewController.viewControllers = [NSArray arrayWithObjects:etasTableNavController, mapViewController, nil];
        [etasTableNavController release];
        [mapViewController release];
        
        self.view = splitViewController.view;
//        [self.view addSubview:splitViewController.view];
    }
    return self;
}

- (void)dealloc
{
    [splitViewController release];

    [super dealloc];
}

- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - View lifecycle

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    [super shouldAutorotateToInterfaceOrientation:interfaceOrientation];
    
    // Return YES for supported orientations
//	return UIInterfaceOrientationIsLandscape(interfaceOrientation);
    return YES;
}

@end
