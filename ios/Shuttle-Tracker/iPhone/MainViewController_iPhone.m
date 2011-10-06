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

@synthesize tabBarController;


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
    self.tabBarController = [[[UITabBarController alloc] init] autorelease];
    tabBarController.delegate = self;
	
    MapViewController *mapViewController = [[MapViewController alloc] init];
    mapViewController.tabBarItem = [[[UITabBarItem alloc] initWithTitle:@"Map" image:[UIImage imageNamed:@"glyphish_map"] tag:0] autorelease];
    mapViewController.dataManager = dataManager;
    
    etasViewController = [[EtasViewController alloc] init];
    etasViewController.tabBarItem = [[[UITabBarItem alloc] initWithTitle:@"ETAs" image:[UIImage imageNamed:@"glyphish_clock"] tag:1] autorelease];
    etasViewController.dataManager = dataManager;
	
	UINavigationController *tableNavController = [[UINavigationController alloc] init];
	tableNavController.viewControllers = [NSArray arrayWithObjects:etasViewController, nil];
	[etasViewController release];
	
	//	Note that this class (MainViewController_iPhone) gets a reference to timeDisplayFormatter
	//	via the init method of its superclass, MainViewController.
	etasViewController.timeDisplayFormatter = timeDisplayFormatter;
    
	IASKAppSettingsViewController *settingsViewController = [[IASKAppSettingsViewController alloc] initWithNibName:@"IASKAppSettingsView" bundle:nil];
	settingsViewController.title = @"Settings";
	settingsViewController.delegate = dataManager;
	
	UINavigationController *settingsNavController = [[UINavigationController alloc] initWithRootViewController:settingsViewController];
	[settingsViewController release];
	settingsNavController.tabBarItem = [[[UITabBarItem alloc] initWithTitle:@"Settings" image:[UIImage imageNamed:@"glyphish_gear"] tag:2] autorelease];
	
    tabBarController.viewControllers = [NSArray arrayWithObjects:mapViewController, tableNavController, settingsNavController, nil];
	[mapViewController release];
	[tableNavController release];
	[settingsNavController release];
	
    self.view = tabBarController.view;
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


#pragma - UITabBarControllerDelegate


- (void)tabBarController:(UITabBarController *)tabBarController didSelectViewController:(UIViewController *)viewController {
	NSNumber *tabNumber = [NSNumber numberWithInt:viewController.tabBarItem.tag];
	
	//	Set the default tab to the currently selected tab, if the current
	//	one is not the settings tab
	if ([tabNumber intValue] == 2) {
		return;
	} else {
		NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
		[defaults setValue:tabNumber forKey:@"defaultTab"];
		[defaults synchronize];
	}
}


@end
