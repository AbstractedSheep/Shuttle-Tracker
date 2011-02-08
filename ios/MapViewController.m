//
//  MapViewController.m
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 1/29/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import "MapViewController.h"
#import "KMLParser.h"

#define UIColorFromRGB(rgbValue) [UIColor colorWithRed:((float)((rgbValue & 0xFF0000) >> 16))/255.0 green:((float)((rgbValue & 0xFF00) >> 8))/255.0 blue:((float)(rgbValue & 0xFF))/255.0 alpha:1.0]


@interface MapViewController()
- (void)routeKmlLoaded;
- (void)drawRoute:(KMLRoute *)route;

@end


@implementation MapViewController

// The designated initializer.  Override if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
/*
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization.
    }
    return self;
}
*/

// Implement loadView to create a view hierarchy programmatically, without using a nib.
- (void)loadView {
	self.view = [[UIView alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
	
	mapView = [[MKMapView alloc] initWithFrame:self.view.frame];
	[self.view addSubview:mapView];
	
	//	Shuttles KML: http://shuttles.rpi.edu/displays/netlink.kml
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];
    
    mapView.showsUserLocation = YES;
    
    //  The student union is at -73.6765441399,42.7302712352
    MKCoordinateRegion region;
    region.center.longitude = -73.67654;
    region.center.latitude = 42.73027;
    region.span.latitudeDelta = 0.0080;
    region.span.longitudeDelta = 0.0070;
    
    mapView.region = region;
    
    routeKmlParser = [[KMLParser alloc] initWithContentsOfUrl:[[NSBundle mainBundle] URLForResource:@"netlink" withExtension:@"kml"]];
    [self routeKmlLoaded];
}

- (void)routeKmlLoaded {
    [routeKmlParser parse];
    
    routes = [routeKmlParser routes];
    [routes retain];
    
    stops = [routeKmlParser stops];
    [stops retain];
    
    for (KMLRoute *route in routes) {
        [self drawRoute:route];
    }
}

- (void)drawRoute:(KMLRoute *)route {
//    MKOverlayPathView *pathView = [[MKOverlayPathView alloc] init];
//    
//    CGMutablePathRef path = CGPathCreateMutable();
//    
//    CGPoint point;
    NSArray *temp;
    CLLocationCoordinate2D clLoc;
    CLLocationCoordinate2D *coordinates = malloc(sizeof(CLLocationCoordinate2D) * route.lineString.count);
    
    int counter = 0;
    
    for (NSString *coordinate in route.lineString) {
        temp = [coordinate componentsSeparatedByString:@","];
        
        if (temp) {
            //  Get a CoreLocation coordinate from the coordinate string
            clLoc = CLLocationCoordinate2DMake([[temp objectAtIndex:0] floatValue], [[temp objectAtIndex:1] floatValue]);
            
            coordinates[counter] = clLoc;
            counter++;
            
//            point = [mapView convertCoordinate:clLoc toPointToView:self.view];
//            
//            //  Add the current point to the path representing this route
//            if (startingPoint) {
//                CGPathMoveToPoint(path, NULL, point.x, point.y);
//            } else {
//                CGPathAddLineToPoint(path, NULL, point.x, point.y);
//            }
        }
        
        [temp release];
    }
    
    MKPolyline *polyLine = [MKPolyline polylineWithCoordinates:coordinates count:counter];
    [polyLine retain];
    
    [routeLines addObject:polyLine];
    
    free(coordinates);
    
    MKPolylineView *routeView = [[MKPolylineView alloc] initWithPolyline:polyLine];
    [routeLineViews addObject:routeView];
    
    routeView.lineWidth = route.style.width;
    routeView.fillColor = route.style.color;
    routeView.strokeColor = route.style.color;
    
    [mapView addOverlay:polyLine];
    
//    //  Close the subpath and add a line from the last point to the first point.
//    CGPathCloseSubpath(path);
//    
//    pathView.path = path;
//    pathView.lineWidth = route.style.width;
//    pathView.strokeColor = [self UIColorFromRGBAString:route.style.color];
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
    [super dealloc];
}

#pragma mark -
#pragma mark MKMapViewDelegate Methods

- (MKOverlayView *)mapView:(MKMapView *)mapView viewForOverlay:(id<MKOverlay>)overlay {
    MKOverlayView* overlayView = nil;
    
    int counter = 0;
    
    for (MKPolyline *routeLine in routeLines) {
        if (routeLine == overlay) {
            overlayView = [routeLineViews objectAtIndex:counter];
            break;
        }
        
        counter++;
    }
    
    return overlayView;
}

- (MKAnnotationView *)mapView:(MKMapView *)theMapView viewForAnnotation:(id<MKAnnotation>)annotation {
    //  If the annotation is the user's location, return nil so the platform
    //  just uses the blue dot
    if (annotation == theMapView.userLocation)
        return nil;
    
    return nil;
}

@end
