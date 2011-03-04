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
	
	timeDisplayFormatter = [[NSDateFormatter alloc] init];
	
	NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
	BOOL use24Time = [[defaults objectForKey:@"Use24Time"] boolValue];
	
	if (use24Time) {
		[timeDisplayFormatter setDateFormat:@"HH:mm"];
	} else {
		[timeDisplayFormatter setDateFormat:@"hh:mm a"];
	}
	
	dataManager.timeDisplayFormatter = timeDisplayFormatter;
	
    UITabBarController *tabBarController = [[UITabBarController alloc] init];
    
    MapViewController *mapViewController = [[MapViewController alloc] init];
    mapViewController.tabBarItem = [[UITabBarItem alloc] initWithTitle:@"Map" image:nil tag:0];
    mapViewController.dataManager = dataManager;
    
    etasViewController = [[EtasViewController alloc] init];
    etasViewController.tabBarItem = [[UITabBarItem alloc] initWithTitle:@"Times" image:nil tag:1];
    etasViewController.dataManager = dataManager;
	etasViewController.timeDisplayFormatter = timeDisplayFormatter;
    
    etasTableUpdateTimer = [NSTimer scheduledTimerWithTimeInterval:3.0f target:etasViewController.tableView selector:@selector(reloadData) userInfo:nil repeats:YES];
    
    tabBarController.viewControllers = [NSArray arrayWithObjects:mapViewController, etasViewController, nil];
    [self.view addSubview:tabBarController.view];
    
}


// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad
{
    [super viewDidLoad];
    
	//	Schedule a timer to make the DataManager pull new data every 5 seconds
    dataUpdateTimer = [NSTimer scheduledTimerWithTimeInterval:5.0f target:dataManager selector:@selector(updateData) userInfo:nil repeats:YES];
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
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

@end
