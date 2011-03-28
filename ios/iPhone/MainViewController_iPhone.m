//
//  MainViewController_iPhone.m
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 2/11/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import "MainViewController_iPhone.h"
#import "MapViewController.h"
#import "EtasViewController.h"
#import "IASKAppSettingsViewController.h"


@implementation MainViewController_iPhone


/*
 Use the init method from the MainViewController superclass
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
	
    tabBarController = [[UITabBarController alloc] init];
    
    MapViewController *mapViewController = [[MapViewController alloc] init];
    mapViewController.tabBarItem = [[UITabBarItem alloc] initWithTitle:@"Map" image:[UIImage imageNamed:@"glyphish_map"] tag:0];
    mapViewController.dataManager = dataManager;
    
    etasViewController = [[EtasViewController alloc] init];
    etasViewController.tabBarItem = [[UITabBarItem alloc] initWithTitle:@"Times" image:[UIImage imageNamed:@"glyphish_clock"] tag:1];
    etasViewController.dataManager = dataManager;
	
	//	Note that this class (MainViewController_iPhone) gets a reference to timeDisplayFormatter
	//	via the init method of its superclass, MainViewController.
	etasViewController.timeDisplayFormatter = timeDisplayFormatter;
    
    //etasTableUpdateTimer = [NSTimer scheduledTimerWithTimeInterval:3.0f target:etasViewController.tableView selector:@selector(reloadData) userInfo:nil repeats:YES];
    
	IASKAppSettingsViewController *settingsViewController = [[IASKAppSettingsViewController alloc] initWithNibName:@"IASKAppSettingsView" bundle:nil];
	settingsViewController.title = @"Settings";
	settingsViewController.delegate = dataManager;
	
	UINavigationController *settingsNavController = [[UINavigationController alloc] initWithRootViewController:settingsViewController];
	[settingsViewController release];
	settingsNavController.tabBarItem = [[UITabBarItem alloc] initWithTitle:@"Settings" image:[UIImage imageNamed:@"glyphish_gear"] tag:2];
	
    tabBarController.viewControllers = [NSArray arrayWithObjects:mapViewController, etasViewController, settingsNavController, nil];
	[mapViewController release];
	[etasViewController release];
	[settingsNavController release];
	
    [self.view addSubview:tabBarController.view];
}


- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
	[tabBarController release];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

@end
