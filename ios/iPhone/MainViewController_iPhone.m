//
//  MainViewController_iPhone.m
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 2/11/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import "MainViewController_iPhone.h"
#import "MapViewController.h"
#import "../EtaViewController.h"


@implementation MainViewController_iPhone


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
                 
    UITabBarController *tabBarController = [[UITabBarController alloc] init];
    
    MapViewController *mapViewController = [[MapViewController alloc] init];
    mapViewController.tabBarItem = [[UITabBarItem alloc] initWithTitle:@"Map" image:nil tag:0];
    
    EtaViewController *etaViewController = [[EtaViewController alloc] init];
    etaViewController.tabBarItem = [[UITabBarItem alloc] initWithTitle:@"ETAs" image:nil tag:1];
    
    tabBarController.viewControllers = [NSArray arrayWithObjects:mapViewController, etaViewController, nil];
    [self.view addSubview:tabBarController.view];
    
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
