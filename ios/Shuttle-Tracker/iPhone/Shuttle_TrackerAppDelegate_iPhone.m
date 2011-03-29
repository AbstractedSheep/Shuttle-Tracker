//
//  Shuttle_TrackerAppDelegate_iPhone.m
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 3/28/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import "Shuttle_TrackerAppDelegate_iPhone.h"
#import "MainViewController_iPhone.h"

@implementation Shuttle_TrackerAppDelegate_iPhone

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
	// Override point for customization after application launch.
	mainViewController = [[MainViewController_iPhone alloc] init];
    [self.window addSubview:mainViewController.view];
	
	[self.window makeKeyAndVisible];
    return YES;
}

- (void)dealloc
{
	[mainViewController release];
	[super dealloc];
}

@end
