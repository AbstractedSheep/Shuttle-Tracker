//
//  MainViewController.m
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 2/20/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import "MainViewController.h"
#import "IASKSettingsReader.h"


@implementation MainViewController

@synthesize dataManager;
@synthesize timeDisplayFormatter;


- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
		//	Create the data manager for all of the parts of the application to use.
        dataManager = [[DataManager alloc] init];
		
		// Set the application defaults
		NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
		NSDictionary *appDefaults = [NSDictionary dictionaryWithObject:@"NO" forKey:@"use24Time"];
		[defaults registerDefaults:appDefaults];
		[defaults synchronize];
		
		//	Create this in a later method in a subclass!
        timeDisplayFormatter = nil;
		
		//	Set the dataManager to take notice when a setting is changed
		[[NSNotificationCenter defaultCenter] addObserver:dataManager selector:@selector(settingChanged:) name:kIASKAppSettingChanged object:nil];
    }
    return self;
}

- (void)dealloc
{
	[dataManager release];
    [super dealloc];
}

- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}


@end
