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
#import "IASKSettingsReader.h"

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
@synthesize vehicles;


// Implement loadView to create a view hierarchy programmatically, without using a nib.
- (void)loadView {
    CGRect rect = [[UIScreen mainScreen] bounds];
	
	UIView *theView = [[UIView alloc] initWithFrame:rect];
	self.view = theView;
	[theView release];
    
	_mapView = [[MKMapView alloc] initWithFrame:rect];
    _mapView.delegate = self;
    
	[self.view addSubview:_mapView];
	
	shuttleImage = [UIImage imageNamed:@"shuttle"];
	[shuttleImage retain];
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];
    
    routeLines = [[NSMutableArray alloc] init];
    routeLineViews = [[NSMutableArray alloc] init];
    
    //  Load the routes/stops KML file asynchronously
    dispatch_queue_t loadRoutesQueue = dispatch_queue_create("com.abstractedsheep.routesqueue", NULL);
	dispatch_async(loadRoutesQueue, ^{
        [dataManager loadRoutesAndStops];
        [self managedRoutesLoaded];
	});
    
    //  The RPI student union is at -73.6765441399,42.7302712352
    //  The center point used here is a bit south of it
    MKCoordinateRegion region;
    region.center.latitude = 42.7292;
    region.center.longitude = -73.6750;
    region.span.latitudeDelta = 0.0200;
    region.span.longitudeDelta = 0.0132;
    
    _mapView.region = region;
    
    vehicleUpdateTimer = nil;
    
    vehicleUpdateTimer = [NSTimer scheduledTimerWithTimeInterval:3.0f target:self selector:@selector(refreshVehicleData) userInfo:nil repeats:YES];
	
	NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
	BOOL useLocation = [[defaults objectForKey:@"useLocation"] boolValue];
	
	if (useLocation) {
		//  Show the user's location on the map
		_mapView.showsUserLocation = YES;
	}
	
	//	Take notice when a setting is changed.
	//	Note that this is not the only object that takes notice.
	[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(settingChanged:) name:kIASKAppSettingChanged object:nil];
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
	
	if (!vehicles) {
		return;
	}
	
	for (JSONVehicle *vehicle in vehicles) {
		if ([[_mapView annotations] indexOfObject:vehicle] == NSNotFound) {
			[self addJsonVehicle:vehicle];
		}
	}
	
	for (id existingObject in [_mapView annotations]) {
		if ([existingObject isKindOfClass:[JSONVehicle class]] && [vehicles indexOfObject:existingObject] == NSNotFound) {
			[_mapView removeAnnotation:existingObject];
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
        
        if (temp && [temp count] > 1) {
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
	[routeView release];
    
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
	[shuttleImage release];
    [super dealloc];
}

- (void)setDataManager:(DataManager *)newDataManager {
	dataManager = newDataManager;
	
	vehicles = newDataManager.vehicles;
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
		if ([(KMLStop *)annotation annotationView]) {
			return [(KMLStop *)annotation annotationView];
		}
		
		MKAnnotationView *stopAnnotationView = [[[MKAnnotationView alloc] initWithAnnotation:(KMLStop *)annotation reuseIdentifier:@"stopAnnotation"] autorelease];
        stopAnnotationView.image = [UIImage imageNamed:@"stop_marker"];
        stopAnnotationView.canShowCallout = YES;
        
        [(KMLStop *)annotation setAnnotationView:stopAnnotationView];
		
		return stopAnnotationView;
        
    } else if ([annotation isKindOfClass:[KMLVehicle class]]) {
        if ([(KMLVehicle *)annotation annotationView]) {
            return [(KMLVehicle *)annotation annotationView];
        }
        
        MKAnnotationView *vehicleAnnotationView = [[[MKAnnotationView alloc] initWithAnnotation:(KMLVehicle *)annotation reuseIdentifier:@"vehicleAnnotation"] autorelease];
        vehicleAnnotationView.image = shuttleImage;
        vehicleAnnotationView.canShowCallout = YES;
        
        [(KMLVehicle *)annotation setAnnotationView:vehicleAnnotationView];
		
		return vehicleAnnotationView;
    } else if ([annotation isKindOfClass:[JSONVehicle class]]) {
        if ([(JSONVehicle *)annotation annotationView]) {
            return [(JSONVehicle *)annotation annotationView];
        }
        
        MKAnnotationView *vehicleAnnotationView = [[[MKAnnotationView alloc] initWithAnnotation:(JSONVehicle *)annotation reuseIdentifier:@"vehicleAnnotation"] autorelease];
        vehicleAnnotationView.image = shuttleImage;
        vehicleAnnotationView.canShowCallout = YES;
        
        [(JSONVehicle *)annotation setAnnotationView:vehicleAnnotationView];
		
		return vehicleAnnotationView;
    }
    
    return nil;
}


//	Called by InAppSettingsKit whenever a setting is changed in the settings view inside the app.
//	Currently only handles turning on or off showing the user's location.
//	Other objects may also do something when a setting is changed.
- (void)settingChanged:(NSNotification *)notification {
	NSDictionary *info = [notification userInfo];
	
	//	Set the date format to 24 hour time if the user has set Use 24 Hour Time to true.
	if ([[notification object] isEqualToString:@"useLocation"]) {
		if ([[info objectForKey:@"useLocation"] boolValue]) {
			_mapView.showsUserLocation = YES;
		} else {
			_mapView.showsUserLocation = NO;
		}
	}
}


@end
