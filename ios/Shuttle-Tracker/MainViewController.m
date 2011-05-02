//
//  MainViewController.m
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 2/20/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import "MainViewController.h"

@interface MainViewController ()
- (void)changeDataUpdateRate:(NSNotification *)notification;
@end

@implementation MainViewController

@synthesize dataManager;
@synthesize timeDisplayFormatter;


- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
		//	Create the data manager for all of the parts of the application to use.
        dataManager = [[DataManager alloc] init];
		
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
														   @"NO", @"YES", [NSNumber numberWithInt:5],  
														   [NSKeyedArchiver archivedDataWithRootObject:favoriteEtasArray],
														   [NSNumber numberWithInt:0], nil]
												  forKeys:[NSArray arrayWithObjects:@"use24Time", 
														   @"useLocation", @"findClosestStop", 
														   @"dataUpdateInterval",
														   @"favoritesList", @"defaultTab", nil]];
		[defaults registerDefaults:appDefaults];
		[defaults synchronize];
		
		//	dataManager creates a timeDisplayFormatter in its init method, so get
		//	a reference to it.
        timeDisplayFormatter = dataManager.timeDisplayFormatter;
		
		[[NSNotificationCenter defaultCenter] addObserver:self 
												 selector:@selector(changeDataUpdateRate:)
													 name:@"dataUpdateInterval"
												   object:nil];
    }
    return self;
}

- (void)changeDataUpdateRate:(NSNotification *)notification {
	//	Invalidate the timer so another can be made with a different interval.
	[dataUpdateTimer invalidate];
	
	NSDictionary *info = [notification userInfo];
	
	float updateInterval = [[info objectForKey:@"dataUpdateInterval"] floatValue];
	
	//	Schedule a timer to make the DataManager pull new data every 5 seconds
    dataUpdateTimer = [NSTimer scheduledTimerWithTimeInterval:updateInterval target:dataManager 
													 selector:@selector(updateData) 
													 userInfo:nil 
													  repeats:YES];
}

- (void)dealloc
{
	[dataManager release];
    [super dealloc];
}

- (void)viewDidLoad {
	[super viewDidLoad];
	
	NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
	float updateInterval = [[defaults objectForKey:@"dataUpdateInterval"] floatValue];
	
	//	Schedule a timer to make the DataManager pull new data every 5 seconds
    dataUpdateTimer = [NSTimer scheduledTimerWithTimeInterval:updateInterval 
													   target:dataManager 
													 selector:@selector(updateData) 
													 userInfo:nil 
													  repeats:YES];
}

- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}


@end
