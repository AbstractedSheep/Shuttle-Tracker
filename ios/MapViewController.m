//
//  MapViewController.m
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 1/29/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import "MapViewController.h"
#import "KMLParser.h"
#import "JSONParser.h"


@interface MapViewController()
- (void)managedRoutesLoaded;
- (void)refreshVehicleData;
- (void)refreshEtaData;
- (void)addRoute:(KMLRoute *)route;
- (void)addStop:(KMLStop *)stop;
- (void)addKmlVehicle:(KMLVehicle *)vehicle;
- (void)addJsonVehicle:(JSONVehicle *)vehicle;

@end


@implementation MapViewController

@synthesize dataManager;


// Implement loadView to create a view hierarchy programmatically, without using a nib.
- (void)loadView {
    CGRect rect = CGRectMake(0.0f, 0.0f, 320.0f, 411.0f);
    
	self.view = [[UIView alloc] initWithFrame:rect];
    
	_mapView = [[MKMapView alloc] initWithFrame:rect];
    _mapView.delegate = self;
    
	[self.view addSubview:_mapView];
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];
    
    routeLines = [[NSMutableArray alloc] init];
    routeLineViews = [[NSMutableArray alloc] init];
    
    vehicles = [[NSMutableArray alloc] init];
    
    //  Load the routes/stops KML file asynchronously
    dispatch_queue_t loadRoutesQueue = dispatch_queue_create("com.abstractedsheep.routesqueue", NULL);
	dispatch_async(loadRoutesQueue, ^{
        [dataManager loadRoutesAndStops];
        [self managedRoutesLoaded];
	});
    
    //  Show the user's location on the map
    _mapView.showsUserLocation = YES;
    
    //  The student union is at -73.6765441399,42.7302712352
    //  The center point used here is a bit south of it
    MKCoordinateRegion region;
    region.center.latitude = 42.7302;
    region.center.longitude = -73.6750;
    region.span.latitudeDelta = 0.0200;
    region.span.longitudeDelta = 0.0132;
    
    _mapView.region = region;
    
    vehicleUpdateTimer = nil;
    
    vehicleUpdateTimer = [NSTimer scheduledTimerWithTimeInterval:3.0f target:self selector:@selector(refreshVehicleData) userInfo:nil repeats:YES];
}


//  The routes and stops were loaded in the dataManager
- (void)managedRoutesLoaded {
    routes = [dataManager routes];
    [routes retain];
    
    stops = [dataManager stops];
    [stops retain];
    
    for (KMLRoute *route in routes) {
        [self performSelectorOnMainThread:@selector(addRoute:) withObject:route waitUntilDone:YES];
    }
    
    for (KMLStop *stop in stops) {
        [self performSelectorOnMainThread:@selector(addStop:) withObject:stop waitUntilDone:YES];
    }
}


//  Grab the most recent data from the data manager and use it
- (void)refreshVehicleData {
    BOOL alreadyAdded = NO;
    
    NSArray *tmpVehicles = [dataManager.vehicles copy];
    
    for (JSONVehicle *newVehicle in tmpVehicles) {
        for (JSONVehicle *existingVehicle in vehicles) {
            if ([existingVehicle.name isEqualToString:newVehicle.name]) {
                [existingVehicle setHeading:newVehicle.heading];
                
                [UIView animateWithDuration:0.5 animations:^{
                    [existingVehicle setCoordinate:newVehicle.coordinate];
                }];
                
                alreadyAdded = YES;
                
//                if (existingVehicle.annotationView) {
//                    //  Note: Same code as in - (MKAnnotationView *)mapView:(MKMapView *)mapView viewForAnnotation:(id<MKAnnotation>)annotation below
//                    [UIView animateWithDuration:0.5 animations:^{
//                        //	Rotate the shuttle image to match the orientation of the shuttle
//                        existingVehicle.annotationView.transform = CGAffineTransformMakeRotation([existingVehicle heading]*2*M_PI/360);
//                    }];
//                    
//                    //  Endnote
//                }

            }
        }
        
		//	Check to make sure that the new vehicle was updated in the past two minutes
        if (!alreadyAdded && [newVehicle.updateTime timeIntervalSinceNow] < 120) {
            [vehicles addObject:newVehicle];
            [self addJsonVehicle:newVehicle];
        }
    }
	
	for (JSONVehicle *vehicle in vehicles) {
		//	Remove vehicles which have not been updated for two minutes
		if ([vehicle.updateTime timeIntervalSinceNow] < 120) {
			[_mapView removeAnnotation:vehicle];
			[vehicles removeObject:vehicle];
		}
	}
}


//  Do nothing as of yet
- (void)refreshEtaData {
    
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

- (void)addKmlVehicle:(KMLVehicle *)vehicle {
    [_mapView addAnnotation:vehicle];
}

- (void)addJsonVehicle:(JSONVehicle *)vehicle {
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
        UIImage *shuttleImage = [UIImage imageNamed:@"shuttle.png"];
        vehicleAnnotationView.image = shuttleImage;
        vehicleAnnotationView.canShowCallout = YES;
        
        [(KMLVehicle *)annotation setAnnotationView:vehicleAnnotationView];
    } else if ([annotation isKindOfClass:[JSONVehicle class]]) {
        if ([(JSONVehicle *)annotation annotationView]) {
            return [(KMLVehicle *)annotation annotationView];
        }
        
        MKAnnotationView *vehicleAnnotationView = [[[MKAnnotationView alloc] initWithAnnotation:(JSONVehicle *)annotation reuseIdentifier:@"vehicleAnnotation"] autorelease];
        UIImage *shuttleImage = [UIImage imageNamed:@"shuttle_color.png"];
        vehicleAnnotationView.image = shuttleImage;
        vehicleAnnotationView.canShowCallout = NO;
        
        //  Note: Same code as in - (void)refreshVehicleData above
        [UIView animateWithDuration:0.5 animations:^{
            //	Rotate the shuttle image to match the orientation of the shuttle
            vehicleAnnotationView.transform = CGAffineTransformMakeRotation([(JSONVehicle *)annotation heading]*2*M_PI/360);
        }];
        
        //  Endnote
        
        [(JSONVehicle *)annotation setAnnotationView:vehicleAnnotationView];
    }
    
    return nil;
}



@end
