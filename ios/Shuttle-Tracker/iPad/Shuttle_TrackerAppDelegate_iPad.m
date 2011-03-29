//
//  Shuttle_TrackerAppDelegate_iPad.m
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 3/28/11.
//  Copyright 2011 Naga Softworks, LLC. All rights reserved.
//

#import "Shuttle_TrackerAppDelegate_iPad.h"
#import "MainViewController_iPad.h"

@implementation Shuttle_TrackerAppDelegate_iPad

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
	// Override point for customization after application launch.
	mainViewController = [[MainViewController_iPad alloc] init];
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
