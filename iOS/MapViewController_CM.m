//
//  MapViewController.m
//  Shuttle Tracker
//
//  Created by Brendon Justin on 1/27/11.
//  Copyright 2011 Naga Softworks, LLC. All rights reserved.
//

#import "MapViewController_CM.h"
#import "RMCloudMadeMapSource.h"

@implementation MapViewController_CM

@synthesize mapView;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

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
	[super viewDidLoad];
    
	UIView *theView = [[UIView alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
	self.view = theView;
	
	[theView release];
	
	mapView = [[RMMapView alloc] initWithFrame:theView.frame];
	
    id cmTilesource = [[[RMCloudMadeMapSource alloc] initWithAccessKey:@"415501adfe7b4fa4a98068ede4498893" styleNumber:1] autorelease];
    [[[RMMapContents alloc] initWithView:mapView tilesource: cmTilesource] autorelease];
	
	[self.view addSubview:mapView];
    
    CLLocationCoordinate2D initLocation;
	
    //  The Student Union's coordinates according to the official shuttle website
    //-73.6765441399,42.7302712352
    initLocation.longitude = -73.676544;
    initLocation.latitude  = 42.730271;
    
    [mapView moveToLatLong:initLocation];
	
    [mapView.contents setZoom:14.2];
	
}


/*
// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad
{
    [super viewDidLoad];
    
    id cmTilesource = [[[RMCloudMadeMapSource alloc] initWithAccessKey:@"415501adfe7b4fa4a98068ede4498893" styleNumber:1] autorelease];
    [[[RMMapContents alloc] initWithView:mapView tilesource: cmTilesource] autorelease];
	
	CLLocationCoordinate2D initLocation;
	
    initLocation.longitude = -0.127523;
    initLocation.latitude  = 51.51383;
	
    [mapView moveToLatLong: initLocation];
	
    [mapView.contents setZoom: 16];
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
