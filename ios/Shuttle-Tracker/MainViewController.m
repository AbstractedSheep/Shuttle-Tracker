//
//  MainViewController.m
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 10/29/11.
//  Copyright (c) 2011 Naga Softworks, LLC. All rights reserved.
//

#import "MainViewController.h"

#import "DataManager.h"
#import "EtasViewController.h"
#import "IASKAppSettingsViewController.h"
#import "MapViewController.h"

@implementation MainViewController

@synthesize dataManager = _dataManager;
@synthesize tabBarController = _tabBarController;
@synthesize timeDisplayFormatter = _timeDisplayFormatter;
@synthesize splitViewController = _splitViewController;
@synthesize fetchedResultsController = __fetchedResultsController;
@synthesize managedObjectContext = __managedObjectContext;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
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
    self.view = [[[UIView alloc] initWithFrame:[[UIScreen mainScreen] bounds]] autorelease];
                 
    MapViewController *mapViewController = [[MapViewController alloc] init];
    mapViewController.tabBarItem = [[[UITabBarItem alloc] initWithTitle:@"Map" image:[UIImage imageNamed:@"glyphish_map"] tag:0] autorelease];
    mapViewController.dataManager = self.dataManager;
    
    EtasViewController *etasViewController = [[EtasViewController alloc] init];
    etasViewController.tabBarItem = [[[UITabBarItem alloc] initWithTitle:@"ETAs" image:[UIImage imageNamed:@"glyphish_clock"] tag:1] autorelease];
    etasViewController.dataManager = self.dataManager;
    
    UINavigationController *etasTableNavController = [[UINavigationController alloc] initWithRootViewController:etasViewController];
    [etasViewController release];
    
    //	Note that this class (MainViewController) gets a reference to timeDisplayFormatter
    //	via the class creating it.
    etasViewController.timeDisplayFormatter = self.timeDisplayFormatter;
    
    IASKAppSettingsViewController *settingsViewController = [[IASKAppSettingsViewController alloc] initWithNibName:@"IASKAppSettingsView" bundle:nil];
    settingsViewController.title = @"Settings";
    settingsViewController.delegate = self.dataManager;
    
    UINavigationController *settingsNavController = [[UINavigationController alloc] initWithRootViewController:settingsViewController];
    [settingsViewController release];
    settingsNavController.tabBarItem = [[[UITabBarItem alloc] initWithTitle:@"Settings" image:[UIImage imageNamed:@"glyphish_gear"] tag:2] autorelease];
    
    //  Device-specific view creation
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
        //  Make a split view, with ETAs on the left and the map on the right.
        
        self.splitViewController = [[[UISplitViewController alloc] init] autorelease];
        self.splitViewController.delegate = mapViewController;
        self.splitViewController.viewControllers = [NSArray arrayWithObjects:etasTableNavController, mapViewController, nil];
        
        [self.view addSubview:self.splitViewController.view];
    } else if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) {
        //  Create a tabbed view, with a map view, ETA view, and settings view.
        
        self.title = NSLocalizedString(@"Shuttles", @"Shuttles");
        self.tabBarController = [[[UITabBarController alloc] init] autorelease];
        
        self.tabBarController.viewControllers = [NSArray arrayWithObjects:mapViewController, etasTableNavController, settingsNavController, nil];
        
        [self.view addSubview:self.tabBarController.view];
    }
    
    [mapViewController release];
    [etasTableNavController release];
    [settingsNavController release];
}

/*
// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad
{
    [super viewDidLoad];
}
*/

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
