//
//  MapViewController.m
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 1/29/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import "MapViewController.h"
#import "KMLParser.h"


@interface MapViewController()
- (void)routeKmlLoaded;
- (void)vehicleKmlRefresh;
- (void)addRoute:(KMLRoute *)route;
- (void)addStop:(KMLStop *)stop;
- (void)addVehicle:(KMLVehicle *)vehicle;

@end


@implementation MapViewController


// Implement loadView to create a view hierarchy programmatically, without using a nib.
- (void)loadView {
	self.view = [[UIView alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
    
	_mapView = [[MKMapView alloc] initWithFrame:self.view.frame];
    _mapView.delegate = self;
    
	[self.view addSubview:_mapView];
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];
    
    routeLines = [[NSMutableArray alloc] init];
    routeLineViews = [[NSMutableArray alloc] init];
    
    NSURL *routeKmlUrl = [[NSBundle mainBundle] URLForResource:@"netlink" withExtension:@"kml"];
    
    //  Load the first KML file asynchronously
    dispatch_queue_t loadFirstKmlQueue = dispatch_queue_create("com.abstractedsheep.kmlqueue", NULL);
	dispatch_async(loadFirstKmlQueue, ^{		
        routeKmlParser = [[KMLParser alloc] initWithContentsOfUrl:routeKmlUrl];
        [self performSelectorOnMainThread:@selector(routeKmlLoaded) withObject:nil waitUntilDone:YES];
        [routeKmlParser release];
	});
    
    //  Show the user's location on the map
    _mapView.showsUserLocation = YES;
    
    //  The student union is at -73.6765441399,42.7302712352
    MKCoordinateRegion region;
    region.center.latitude = 42.73027;
    region.center.longitude = -73.6750;
    region.span.latitudeDelta = 0.0200;
    region.span.longitudeDelta = 0.0132;
    
    _mapView.region = region;
}

- (void)routeKmlLoaded {
    [routeKmlParser parse];
    
    routes = [routeKmlParser routes];
    [routes retain];
    
    stops = [routeKmlParser stops];
    [stops retain];
    
    vehicles = nil;
    
    for (KMLRoute *route in routes) {
        [self performSelectorOnMainThread:@selector(addRoute:) withObject:route waitUntilDone:YES];
    }
    
    for (KMLStop *stop in stops) {
        [self performSelectorOnMainThread:@selector(addStop:) withObject:stop waitUntilDone:YES];
    }
    
    if (routeKmlParser.vehiclesUrl) {
        
        NSURL *shuttleKmlUrl = [[NSBundle mainBundle] URLForResource:@"current" withExtension:@"kml"];
        
        dispatch_queue_t loadVehicleKmlQueue = dispatch_queue_create("com.abstractedsheep.kmlqueue", NULL);
        dispatch_async(loadVehicleKmlQueue, ^{		
//            vehiclesKmlParser = [[KMLParser alloc] initWithContentsOfUrl:routeKmlParser.vehiclesUrl];
            vehiclesKmlParser = [[KMLParser alloc] initWithContentsOfUrl:shuttleKmlUrl];
            [self performSelectorOnMainThread:@selector(vehicleKmlRefresh) withObject:nil waitUntilDone:YES];
//            [vehiclesKmlParser release];
        });
        
    }
    
}

- (void)vehicleKmlRefresh {
    [vehiclesKmlParser parse];
    
    vehicles = vehiclesKmlParser.vehicles;
    
    for (KMLVehicle *vehicle in vehicles) {
        [self addVehicle:vehicle];
    }
    
    [vehicles release];
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
            clLoc = CLLocationCoordinate2DMake([[temp objectAtIndex:1] floatValue], [[temp objectAtIndex:0] floatValue]);
            
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
    routeView.fillColor = route.style.color;
    routeView.strokeColor = route.style.color;
    
    [_mapView addOverlay:polyLine];
}

- (void)addStop:(KMLStop *)stop {
    [_mapView addAnnotation:stop];
    
}

- (void)addVehicle:(KMLVehicle *)vehicle {
    [_mapView addAnnotation:vehicle];
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
    [_mapView release];
    [super dealloc];
}

#pragma mark MKMapViewDelegate

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

- (MKAnnotationView *)mapView:(MKMapView *)mapView viewForAnnotation:(id<MKAnnotation>)annotation {
    //  If the annotation is the user's location, return nil so the platform
    //  just uses the blue dot
    if (annotation == _mapView.userLocation)
        return nil;
    
    if ([annotation isKindOfClass:[KMLStop class]]) {
        MKPinAnnotationView *pinAnnotationView = [[[MKPinAnnotationView alloc] initWithAnnotation:(KMLStop *)annotation reuseIdentifier:@"stopAnnotation"] autorelease];
        pinAnnotationView.pinColor = MKPinAnnotationColorPurple;
        pinAnnotationView.animatesDrop = NO;
        pinAnnotationView.canShowCallout = YES;
        
        return pinAnnotationView;
    } else if ([annotation isKindOfClass:[KMLVehicle class]]) {
        MKAnnotationView *vehicleAnnotationView = [[[MKAnnotationView alloc] initWithAnnotation:(KMLVehicle *)annotation reuseIdentifier:@"vehicleAnnotation"] autorelease];
        vehicleAnnotationView.image = [UIImage imageWithContentsOfFile:@"shuttle_icon.png"];
    }
    
    return nil;
}



@end
