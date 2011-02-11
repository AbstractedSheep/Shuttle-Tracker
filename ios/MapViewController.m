//
//  MapViewController.m
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 1/29/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import "MapViewController.h"
#import "KMLParser.h"
#import "ViewForX.h"


@interface MapViewController()
- (void)routeKmlLoaded;
- (void)addRoute:(KMLRoute *)route;
- (void)addStop:(KMLStop *)stop;

@end


@implementation MapViewController


// Implement loadView to create a view hierarchy programmatically, without using a nib.
- (void)loadView {
	self.view = [[UIView alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
	
    vfx = [[ViewForX alloc] init];
    
	mapView = [[MKMapView alloc] initWithFrame:self.view.frame];
    mapView.delegate = vfx;
    
	[self.view addSubview:mapView];
	
	//	Shuttles KML: http://shuttles.rpi.edu/displays/netlink.kml
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];
    
    routeLines = [[NSMutableArray alloc] init];
    routeLineViews = [[NSMutableArray alloc] init];
    
    vfx.routeLines = routeLines;
    vfx.routeLineViews = routeLineViews;
    
    NSURL *xmlUrl = [[NSBundle mainBundle] URLForResource:@"netlink" withExtension:@"kml"];
    
    routeKmlParser = [[KMLParser alloc] initWithContentsOfUrl:xmlUrl];
    [self routeKmlLoaded];
    
//    mapView.showsUserLocation = YES;
    
    //  The student union is at -73.6765441399,42.7302712352
    MKCoordinateRegion region;
    region.center.longitude = -73.674;
    region.center.latitude = 42.73027;
    region.span.latitudeDelta = 0.0180;
    region.span.longitudeDelta = 0.0120;
    
    mapView.region = region;
}

- (void)routeKmlLoaded {
    [routeKmlParser parse];
    
    routes = [routeKmlParser routes];
    [routes retain];
    
    stops = [routeKmlParser stops];
    [stops retain];
    
    vehicles = nil;
    
    for (KMLRoute *route in routes) {
        [self addRoute:route];
    }
    
    for (KMLStop *stop in stops) {
        [self addStop:stop];
    }
}

- (void)addRoute:(KMLRoute *)route {
    NSArray *temp;
    CLLocationCoordinate2D clLoc;
    MKMapPoint *points = malloc(sizeof(MKMapPoint) * route.lineString.count);
    
    int counter = 0;
    
    for (NSString *coordinate in route.lineString) {
        temp = [coordinate componentsSeparatedByString:@","];
        
        if (temp) {
            //  Get a CoreLocation coordinate from the coordinate string
            clLoc = CLLocationCoordinate2DMake([[temp objectAtIndex:0] floatValue], [[temp objectAtIndex:1] floatValue]);
            
            points[counter] = MKMapPointForCoordinate(clLoc);
            counter++;
        }
        
    }
    
    MKPolyline *polyLine = [MKPolyline polylineWithPoints:points count:counter];
    [routeLines addObject:polyLine];
    
    free(points);
    
    MKPolylineView *routeView = [[MKPolylineView alloc] initWithPolyline:polyLine];
    [routeLineViews addObject:routeView];
    
    routeView.lineWidth = route.style.width;
//    routeView.fillColor = route.style.color;
//    routeView.strokeColor = route.style.color;
    routeView.fillColor = [UIColor redColor];
    routeView.strokeColor = [UIColor redColor];
    
    [mapView addOverlay:polyLine];
}

- (void)addStop:(KMLStop *)stop {
    [mapView addAnnotation:stop];
    
}

/*
// Override to allow orientations other than the default portrait orientation.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations.
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}
*/

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc. that aren't in use.
}

- (void)viewDidUnload {
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}


- (void)dealloc {
    [mapView release];
    [super dealloc];
}

#pragma mark MKMapViewDelegate




@end
