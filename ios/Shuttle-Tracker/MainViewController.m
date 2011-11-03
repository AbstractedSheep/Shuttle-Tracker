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

@interface MainViewController ()

@property (strong, nonatomic) NSTimer *dataUpdateTimer;

@end

@implementation MainViewController

@synthesize dataManager = _dataManager;
@synthesize tabBarController = _tabBarController;
@synthesize timeDisplayFormatter = _timeDisplayFormatter;
@synthesize dataUpdateTimer = _dataUpdateTimer;
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
    
    DataManager *dataManager = [[DataManager alloc] init];
    self.dataManager = dataManager;
    [dataManager release];
    [self.dataManager setParserManagedObjectContext:self.managedObjectContext];
    
    //	dataManager creates a timeDisplayFormatter in its init method, so get
    //	a reference to it.
    self.timeDisplayFormatter = self.dataManager.timeDisplayFormatter;
    
    MapViewController *mapViewController = [[MapViewController alloc] init];
    mapViewController.tabBarItem = [[[UITabBarItem alloc] initWithTitle:@"Map" image:[UIImage imageNamed:@"glyphish_map"] tag:0] autorelease];
    mapViewController.dataManager = self.dataManager;
    mapViewController.managedObjectContext = self.managedObjectContext;
    
    UINavigationController *mapNavController = [[UINavigationController alloc] initWithRootViewController:mapViewController];
    [mapViewController release];
    
    EtasViewController *etasViewController = [[EtasViewController alloc] init];
    etasViewController.tabBarItem = [[[UITabBarItem alloc] initWithTitle:@"ETAs" image:[UIImage imageNamed:@"glyphish_clock"] tag:1] autorelease];
    etasViewController.dataManager = self.dataManager;
    etasViewController.managedObjectContext = self.managedObjectContext;
    
    UINavigationController *etasTableNavController = [[UINavigationController alloc] initWithRootViewController:etasViewController];
    [etasViewController release];
    
    //	Note that this class (MainViewController) gets a reference to timeDisplayFormatter
    //	via the class creating it.
    etasViewController.timeDisplayFormatter = self.timeDisplayFormatter;
    
    //  Device-specific view creation
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
        //  Make a split view, with ETAs on the left and the map on the right.
        self.splitViewController = [[[UISplitViewController alloc] init] autorelease];
        self.splitViewController.view.frame = self.view.frame;
        self.splitViewController.delegate = mapViewController;
        self.splitViewController.viewControllers = [NSArray arrayWithObjects:etasTableNavController, mapNavController, nil];
        
        [self.view addSubview:self.splitViewController.view];
    } else if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) {
        //  Create the settings view controller, only found on the iPhone
        IASKAppSettingsViewController *settingsViewController = [[IASKAppSettingsViewController alloc] initWithNibName:@"IASKAppSettingsView" bundle:nil];
        settingsViewController.title = @"Settings";
        settingsViewController.delegate = self.dataManager;
        
        UINavigationController *settingsNavController = [[UINavigationController alloc] initWithRootViewController:settingsViewController];
        [settingsViewController release];
        settingsNavController.tabBarItem = [[[UITabBarItem alloc] initWithTitle:@"Settings" image:[UIImage imageNamed:@"glyphish_gear"] tag:2] autorelease];
        
        //  Create a tabbed view, with a map view, ETA view, and settings view.
        self.tabBarController = [[[UITabBarController alloc] init] autorelease];
        
        self.tabBarController.viewControllers = [NSArray arrayWithObjects:mapViewController, etasTableNavController, settingsNavController, nil];
        [settingsNavController release];
        
        [self.view addSubview:self.tabBarController.view];
    }
    
    [mapNavController release];
    [etasTableNavController release];
    
    // Check if 12 or 24 hour mode
    BOOL use24Time = NO;
    
    NSDateFormatter *timeFormatter = [[NSDateFormatter alloc] init];
    [timeFormatter setTimeStyle:NSDateFormatterMediumStyle];
    
    NSMutableArray *dateArray = [[NSMutableArray alloc] init];
    [dateArray setArray:[[timeFormatter stringFromDate:[NSDate date]] componentsSeparatedByString:@" "]];
    
    if ([dateArray count] == 1) // if no am/pm extension exists
        use24Time = YES;
    
    [timeFormatter release];
    [dateArray release];
    
    //	Create an empty array to use for the favorite ETAs
    NSMutableArray *favoriteEtasArray = [NSMutableArray array];
    
    // Set the application defaults
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    NSDictionary *appDefaults;
    appDefaults = [NSDictionary dictionaryWithObjects:[NSArray arrayWithObjects:use24Time ? @"YES" : @"NO", 
                                                       @"NO", @"YES", [NSNumber numberWithInt:5], @"NO",
                                                       [NSKeyedArchiver archivedDataWithRootObject:favoriteEtasArray], nil]
                                              forKeys:[NSArray arrayWithObjects:@"use24Time", 
                                                       @"useLocation", @"findClosestStop", 
                                                       @"dataUpdateInterval", @"useRelativeTimes",
                                                       @"favoritesList", nil]];
    [defaults registerDefaults:appDefaults];
    [defaults synchronize];
}


// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad
{
    [super viewDidLoad];
    
    [[NSNotificationCenter defaultCenter] addObserver:self 
                                             selector:@selector(changeDataUpdateRate:)
                                                 name:@"dataUpdateInterval"
                                               object:nil];
    
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
	float updateInterval = [[defaults objectForKey:@"dataUpdateInterval"] floatValue];
	
	//	Schedule a timer to make the DataManager pull new data every 5 seconds
    self.dataUpdateTimer = [NSTimer scheduledTimerWithTimeInterval:updateInterval 
                                                            target:self.dataManager 
                                                          selector:@selector(updateData) 
                                                          userInfo:nil 
                                                           repeats:YES];
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
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
        //  Any orientation on the iPad
        return YES;
    } else {
        //  Portrait only, on the iPhone and iPod Touch
        return (interfaceOrientation == UIInterfaceOrientationPortrait);
    }
}

- (void)changeDataUpdateRate:(NSNotification *)notification {
	//	Invalidate the timer so another can be made with a different interval.
	[self.dataUpdateTimer invalidate];
	
	NSDictionary *info = [notification userInfo];
	
	float updateInterval = [[info objectForKey:@"dataUpdateInterval"] floatValue];
	
	//	Schedule a timer to make the DataManager pull new data every 5 seconds
    self.dataUpdateTimer = [NSTimer scheduledTimerWithTimeInterval:updateInterval target:self.dataManager 
													 selector:@selector(updateData) 
													 userInfo:nil 
													  repeats:YES];
}

@end
