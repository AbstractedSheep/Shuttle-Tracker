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
- (void)updateVehicleData;
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
    
    vehicleUpdateTimer = nil;
}

- (void)routeKmlLoaded {
    [routeKmlParser parse];
    
    routes = [routeKmlParser routes];
    [routes retain];
    
    stops = [routeKmlParser stops];
    [stops retain];
    
    vehicles = [[NSMutableArray alloc] init];
    
    for (KMLRoute *route in routes) {
        [self performSelectorOnMainThread:@selector(addRoute:) withObject:route waitUntilDone:YES];
    }
    
    for (KMLStop *stop in stops) {
        [self performSelectorOnMainThread:@selector(addStop:) withObject:stop waitUntilDone:YES];
    }
    
    if (routeKmlParser.vehiclesUrl) {
        NSURL *urlFromParser = routeKmlParser.vehiclesUrl;
        vehiclesKmlParser = [[KMLParser alloc] initWithContentsOfUrl:urlFromParser];
        
        vehicleUpdateTimer = [NSTimer scheduledTimerWithTimeInterval:5.0f target:self selector:@selector(updateVehicleData) userInfo:nil repeats:YES];
        
    }
    
}

- (void)updateVehicleData {
    
    dispatch_queue_t loadVehicleKmlQueue = dispatch_queue_create("com.abstractedsheep.kmlqueue", NULL);
    dispatch_async(loadVehicleKmlQueue, ^{
        [vehiclesKmlParser parse];
        [self performSelectorOnMainThread:@selector(vehicleKmlRefresh) withObject:nil waitUntilDone:YES];
    });
}

- (void)vehicleKmlRefresh {
    BOOL alreadyAdded = NO;
    
    for (KMLVehicle *newVehicle in vehiclesKmlParser.vehicles) {
        for (KMLVehicle *existingVehicle in vehicles) {
            if ([existingVehicle.idTag isEqualToString:newVehicle.idTag]) {
                [UIView animateWithDuration:0.5 animations:^{
                    [existingVehicle setCoordinate:newVehicle.coordinate];
                }];
                
                alreadyAdded = YES;
            }
        }
        
        if (!alreadyAdded) {
            [vehicles addObject:newVehicle];
            [self addVehicle:newVehicle];
        }
        
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

// Override to allow orientations other than the default portrait orientation.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations.
//    return (interfaceOrientation == UIInterfaceOrientationPortrait);
    return YES;
}


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
    if (routeKmlParser) {
        [routeKmlParser release];
    }
    
    if (vehicleUpdateTimer) {
        [vehicleUpdateTimer invalidate];
    }
    
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
        
        [(KMLStop *)annotation setAnnotationView:pinAnnotationView];
        
        return pinAnnotationView;
    } else if ([annotation isKindOfClass:[KMLVehicle class]]) {
        if ([(KMLVehicle *)annotation annotationView]) {
            return [(KMLVehicle *)annotation annotationView];
        }
        
        MKAnnotationView *vehicleAnnotationView = [[[MKAnnotationView alloc] initWithAnnotation:(KMLVehicle *)annotation reuseIdentifier:@"vehicleAnnotation"] autorelease];
        vehicleAnnotationView.image = [UIImage imageNamed:@"shuttle_icon.png"];
        
        [(KMLVehicle *)annotation setAnnotationView:vehicleAnnotationView];
    }
    
    return nil;
}



@end
