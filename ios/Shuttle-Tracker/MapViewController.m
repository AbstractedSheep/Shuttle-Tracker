//
//  MapViewController.m
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 1/29/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import "MapViewController.h"

#import "MapPlacemark.h"
#import "Route.h"
#import "RoutePt.h"
#import "Shuttle.h"
#import "Stop.h"

#import "IASKSettingsReader.h"

//  Set shuttles updated more than 2 minute ago as "stale"
const float UPDATE_THRESHOLD = -120;

@interface UIImage (magentatocolor)

- (UIImage *)copyMagentaImageasColor:(UIColor *)newColor;

@end

@interface UIColor (stringcolor)

+ (UIColor *)UIColorFromRGBString:(NSString *)rgbString;
+ (UIColor *)UIColorFromRGBAString:(NSString *)rgbaString;

@end

//	From Stack Overflow (SO), with modifications:
@implementation UIImage (magentatocolor)

typedef enum {
    ALPHA = 0,
    BLUE = 1,
    GREEN = 2,
    RED = 3
} PIXELS;


//  Convert the magenta pixels in an image to a new color.
//  Returns a new image with retain count 1.
- (UIImage *)copyMagentaImageasColor:(UIColor *)newColor {
    BOOL monochromeModel = NO;
    
    CGSize size = [self size];
    int width = size.width;
    int height = size.height;
    
    // the pixels will be painted to this array
    //  Note that this will hold integer values [0,255]
    uint32_t *pixels = (uint32_t *) malloc(width * height * sizeof(uint32_t));
    
    // clear the pixels so any transparency is preserved
    memset(pixels, 0, width * height * sizeof(uint32_t));
    
    CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceRGB();
    
    // create a context with RGBA pixels
    CGContextRef context = CGBitmapContextCreate(pixels, width, height, 8, width * sizeof(uint32_t), colorSpace, 
                                                 kCGBitmapByteOrder32Little | kCGImageAlphaPremultipliedLast);
    
    CGColorSpaceRef space = CGColorGetColorSpace(newColor.CGColor);
    CGColorSpaceModel model = CGColorSpaceGetModel(space);

    if (model == kCGColorSpaceModelMonochrome) {
        monochromeModel = YES;
    }
    
    //  Get an array of the rgb values of the new color.
    //  Note that these values are floating point values on [0,1] 
    const CGFloat *rgb = CGColorGetComponents(newColor.CGColor);
    
    // paint the bitmap to our context which will fill in the pixels array
    //  Again, the pixels array is storing integer values [0,255]
    CGContextDrawImage(context, CGRectMake(0, 0, width, height), [self CGImage]);
    
    for(int y = 0; y < height; y++) {
        for(int x = 0; x < width; x++) {
            uint8_t *rgbaPixel = (uint8_t *) &pixels[y * width + x];
            
            //  If the color of the current pixel is magenta, which is (255, 0, 255) in RGB,
            //  change the color to the new color.
            if (rgbaPixel[RED] == 255 && rgbaPixel[GREEN] == 0 && rgbaPixel[BLUE] == 255) {
                if (monochromeModel) {
                    rgbaPixel[RED] = rgb[0] * 255.0f;
                    rgbaPixel[GREEN] = rgb[0] * 255.0f;
                    rgbaPixel[BLUE] = rgb[0] * 255.0f;
                } else {
                    rgbaPixel[RED] = rgb[0] * 255.0f;
                    rgbaPixel[GREEN] = rgb[1] * 255.0f;
                    rgbaPixel[BLUE] = rgb[2] * 255.0f;
                }
            }
        }
    }
    
    // create a new CGImageRef from our context with the modified pixels
    CGImageRef image = CGBitmapContextCreateImage(context);
    
    // we're done with the context, color space, and pixels
    CGContextRelease(context);
    CGColorSpaceRelease(colorSpace);
    free(pixels);
    
    // make a new UIImage to return
    UIImage *resultUIImage = [UIImage imageWithCGImage:image];
    [resultUIImage retain];
    
    // we're done with image now too
    CGImageRelease(image);
    
    return resultUIImage;
}

@end
//	End from SO


@interface MapViewController()

@property (strong, nonatomic) UIPopoverController *masterPopoverController;

- (void)managedRoutesLoaded;
//	notifyVehiclesUpdated may not be called on the main thread, so use it to call
//	vehicles updated on the main thread.
- (void)notifyVehiclesUpdated:(NSNotification *)notification;
- (void)vehiclesUpdated:(NSNotification *)notification;
//	Adding routes and stops is not guaranteed to be done on the main thread.
- (void)addRoute:(Route *)route;
- (void)addStop:(Stop *)stop;
//	Adding vehicles should only be done on the main thread.
- (MapVehicle *)addVehicle:(Shuttle *)vehicle;
- (void)setVehicleAnnotationImage:(MapVehicle *)vehicle;
- (void)settingChanged:(NSNotification *)notification;

@end

@implementation MapViewController

@synthesize dataManager;
@synthesize managedObjectContext = __managedObjectContext;
@synthesize masterPopoverController = _masterPopoverController;

- (id)init {
    if ((self = [super init])) {
        NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
        BOOL useLocation = [[defaults objectForKey:@"useLocation"] boolValue];
        
        if (useLocation) {
            //  Show the user's location on the map
            _mapView.showsUserLocation = YES;
        }
        
        shuttleImage = [UIImage imageNamed:@"shuttle"];
        [shuttleImage retain];
        
        magentaShuttleImages = [[NSMutableDictionary alloc] initWithCapacity:4];
        
        shuttleImages = [[NSMutableDictionary alloc] initWithCapacity:4];
        NSMutableDictionary *shuttleImagesEast = [[NSMutableDictionary alloc] init];
        NSMutableDictionary *shuttleImagesNorth = [[NSMutableDictionary alloc] init];
        NSMutableDictionary *shuttleImagesWest = [[NSMutableDictionary alloc] init];
        NSMutableDictionary *shuttleImagesSouth = [[NSMutableDictionary alloc] init];
        
        //  Create east, north, west and south shuttle images
        //  East
        UIImage *magentaShuttleImage = [UIImage imageNamed:@"shuttle_color_east"];
        [magentaShuttleImages setObject:magentaShuttleImage forKey:@"east"];
        
        UIImage *whiteImage = [magentaShuttleImage copyMagentaImageasColor:[UIColor whiteColor]];
        [shuttleImagesEast setObject:whiteImage forKey:[[NSNumber numberWithInt:-1] stringValue]];
        [whiteImage release];
        
        //  North
        magentaShuttleImage = [UIImage imageNamed:@"shuttle_color_north"];
        [magentaShuttleImages setObject:magentaShuttleImage forKey:@"north"];
        
        whiteImage = [magentaShuttleImage copyMagentaImageasColor:[UIColor whiteColor]];
        [shuttleImagesNorth setObject:whiteImage forKey:[[NSNumber numberWithInt:-1] stringValue]];
        [whiteImage release];
        
        //  West
        magentaShuttleImage = [UIImage imageNamed:@"shuttle_color_west"];
        [magentaShuttleImages setObject:magentaShuttleImage forKey:@"west"];
        
        whiteImage = [magentaShuttleImage copyMagentaImageasColor:[UIColor whiteColor]];
        [shuttleImagesWest setObject:whiteImage forKey:[[NSNumber numberWithInt:-1] stringValue]];
        [whiteImage release];
        
        //  South
        magentaShuttleImage = [UIImage imageNamed:@"shuttle_color_south"];
        [magentaShuttleImages setObject:magentaShuttleImage forKey:@"south"];
        
        whiteImage = [magentaShuttleImage copyMagentaImageasColor:[UIColor whiteColor]];
        [shuttleImagesSouth setObject:whiteImage forKey:[[NSNumber numberWithInt:-1] stringValue]];
        [whiteImage release];
        
        [shuttleImages setObject:shuttleImagesEast forKey:@"east"];
        [shuttleImages setObject:shuttleImagesNorth forKey:@"north"];
        [shuttleImages setObject:shuttleImagesWest forKey:@"west"];
        [shuttleImages setObject:shuttleImagesSouth forKey:@"south"];
        [shuttleImagesEast release];
        [shuttleImagesNorth release];
        [shuttleImagesWest release];
        [shuttleImagesSouth release];
        
        vehicles = [[NSMutableDictionary alloc] init];
        
        //	Take notice when the routes and stops are updated.
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(managedRoutesLoaded)
                                                     name:kDMRoutesandStopsLoaded
                                                   object:nil];
        
        //	Take notice when vehicles are updated.
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(notifyVehiclesUpdated:)
                                                     name:kDMVehiclesUpdated
                                                   object:nil];
        
        //	Take notice when a setting is changed.
        //	Note that this is not the only object that takes notice.
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(settingChanged:)
                                                     name:kIASKAppSettingChanged
                                                   object:nil];
    }
    
    return self;
}

// Implement loadView to create a view hierarchy programmatically, without using a nib.
- (void)loadView {
    self.title = NSLocalizedString(@"Shuttles", @"Shuttles");
    
    CGRect rect = [[UIScreen mainScreen] bounds];
    
	_mapView = [[MKMapView alloc] initWithFrame:rect];
    _mapView.delegate = self;
	_mapView.autoresizingMask = UIViewAutoresizingFlexibleHeight | UIViewAutoresizingFlexibleWidth;
    
	self.view = _mapView;
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];
    
    routeLines = [[NSMutableArray alloc] init];
    routeLineViews = [[NSMutableArray alloc] init];
    
    //  The RPI student union is at -73.6765441399,42.7302712352
    //  The center point used here is a bit south of it
    MKCoordinateRegion region;
    region.center.latitude = 42.7312;
    region.center.longitude = -73.6750;
    region.span.latitudeDelta = 0.0200;
    region.span.longitudeDelta = 0.0132;
    
    _mapView.region = region;
    
	[dataManager loadRoutesAndStops];
    
    shuttleCleanupTimer = [NSTimer timerWithTimeInterval:30 target:self selector:@selector(vehicleCleanup) userInfo:nil repeats:YES];
}

- (void)viewDidUnload {
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
    
    [shuttleCleanupTimer invalidate];
}

- (void)vehicleCleanup {
    NSMutableArray *oldVehicles = [NSMutableArray array];
    
    for (MapVehicle *vehicle in vehicles) {
        if ([vehicle.updateTime timeIntervalSinceNow] < UPDATE_THRESHOLD) {
            [oldVehicles addObject:vehicle.name];
        }
    }
    
    for (NSString *name in oldVehicles) {
        [vehicles removeObjectForKey:name];
    }
}

//  The routes and stops were loaded in the dataManager
- (void)managedRoutesLoaded {
    //  Get all routes
    NSEntityDescription *routeEntityDescription = [NSEntityDescription entityForName:@"Route" 
                                                         inManagedObjectContext:self.managedObjectContext];
    NSFetchRequest *routeRequest = [[[NSFetchRequest alloc] init] autorelease];
    [routeRequest setEntity:routeEntityDescription];
    
    NSError *error = nil;
    NSArray *dbRoutes = [self.managedObjectContext executeFetchRequest:routeRequest error:&error];
    if (dbRoutes == nil)
    {
        // Deal with error...
    } else if ([dbRoutes count] > 0) {
        for (Route *route in dbRoutes) {
            [self addRoute:route];
        }
    } else {
        //  No routes, so do nothing
    }
    
    //  Get all stops
    NSEntityDescription *stopEntityDescription = [NSEntityDescription entityForName:@"Stop" 
                                                              inManagedObjectContext:self.managedObjectContext];
    NSFetchRequest *stopRequest = [[[NSFetchRequest alloc] init] autorelease];
    [stopRequest setEntity:stopEntityDescription];
    
    error = nil;
    NSArray *dbStops = [self.managedObjectContext executeFetchRequest:stopRequest error:&error];
    if (dbStops == nil)
    {
        // Deal with error...
    } else if ([dbStops count] > 0) {
        for (Stop *stop in dbStops) {
            [self addStop:stop];
        }
    } else {
        //  No stops, so do nothing
    }
}

//	A notification is sent by DataManager whenever the vehicles are updated.
//	Call the work function vehiclesUpdated on the main thread.
- (void)notifyVehiclesUpdated:(NSNotification *)notification {
	[self performSelectorOnMainThread:@selector(vehiclesUpdated:) withObject:notification waitUntilDone:NO];
}

//	A notification is sent by DataManager whenever the vehicles are updated.
- (void)vehiclesUpdated:(NSNotification *)notification {
    //  Get all vehicles
    NSEntityDescription *entityDescription = [NSEntityDescription entityForName:@"Shuttle"
                                                         inManagedObjectContext:self.managedObjectContext];
    NSFetchRequest *request = [[[NSFetchRequest alloc] init] autorelease];
    [request setEntity:entityDescription];
    
    NSError *error = nil;
    NSArray *dbVehicles = [self.managedObjectContext executeFetchRequest:request error:&error];
    if (dbVehicles == nil)
    {
        // Deal with error...
    } else if ([dbVehicles count] > 0) {
        for (Shuttle *shuttle in dbVehicles) {
            MapVehicle *existingShuttle = [vehicles objectForKey:shuttle.name];
            if (existingShuttle == nil) {
                //  Add the shuttle to the map view
                if ([shuttle.updateTime timeIntervalSinceNow] > UPDATE_THRESHOLD) {
                    [vehicles setObject:[self addVehicle:shuttle] forKey:shuttle.name];
                }
            } else {
                if ([shuttle.updateTime timeIntervalSinceNow] < UPDATE_THRESHOLD) {
                    [vehicles removeObjectForKey:existingShuttle.name];
                } else if ([shuttle.routeId intValue] != existingShuttle.routeNo || [shuttle.heading intValue] != existingShuttle.heading) {
                    //	If the shuttle switched routes, then update the image.
                    //  Also update the image if the shuttle has changed heading
                    
                    existingShuttle.routeNo = [shuttle.routeId intValue];
                    existingShuttle.heading = [shuttle.heading intValue];
                    
                    [self setVehicleAnnotationImage:existingShuttle];
                } else if ([shuttle.updateTime timeIntervalSinceDate:existingShuttle.updateTime] > 0) {
                    //  If the shuttle location is out of date, update it
                    
                    CLLocationCoordinate2D clLoc = CLLocationCoordinate2DMake([shuttle.latitude doubleValue], [shuttle.longitude doubleValue]);
                    existingShuttle.coordinate = clLoc;
                }
            }
        }
    } else {
        //  No vehicles, so do nothing
    }
}


//  Add the overlay for the route to the map view, and create a shuttle image with
//  a color matching the route's color
- (void)addRoute:(Route *)route {
    CLLocationCoordinate2D clLoc;
    MKMapPoint *points;
    
    //  Get all vehicles
    NSEntityDescription *entityDescription = [NSEntityDescription entityForName:@"RoutePt"
                                                         inManagedObjectContext:self.managedObjectContext];
    NSFetchRequest *request = [[[NSFetchRequest alloc] init] autorelease];
    [request setEntity:entityDescription];
    
    // Set predicate and sort orderings...
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"(route == %@)", route];
    [request setPredicate:predicate];
    
    NSSortDescriptor *sortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"pointNumber" ascending:YES];
    [request setSortDescriptors:[NSArray arrayWithObject:sortDescriptor]];
    [sortDescriptor release];
    
    NSError *error = nil;
    NSArray *routePts = [self.managedObjectContext executeFetchRequest:request error:&error];
    if (routePts == nil)
    {
        // Deal with error...
    } else {
        points = malloc(sizeof(MKMapPoint) * routePts.count);
        
        //  Create an array of coordinates for the polyline which will represent the route
        int counter = 0;
        for (RoutePt *point in routePts) {
            //  Get a CoreLocation coordinate from the point
            clLoc = CLLocationCoordinate2DMake([point.latitude doubleValue], [point.longitude doubleValue]);
            
            points[counter] = MKMapPointForCoordinate(clLoc);
            counter++;
        }
        
        MKPolyline *polyLine = [MKPolyline polylineWithPoints:points count:counter];
        [routeLines addObject:polyLine];
        
        free(points);
        
        MKPolylineView *routeView = [[MKPolylineView alloc] initWithPolyline:polyLine];
        [routeLineViews addObject:routeView];
        [routeView release];
        
        routeView.lineWidth = [route.width intValue];
        routeView.fillColor = [UIColor UIColorFromRGBString:route.color];
        routeView.strokeColor = routeView.fillColor;
        
        [_mapView addOverlay:polyLine];
        
        //  Create the colored shuttle image for the route
        UIImage *coloredImage;
        
        if (routeView.fillColor) {
            coloredImage = [[magentaShuttleImages objectForKey:@"west"] copyMagentaImageasColor:routeView.fillColor];
            [[shuttleImages objectForKey:@"east"] setValue:coloredImage forKey:[route.routeId stringValue]];
            [coloredImage release];
            
            coloredImage = [[magentaShuttleImages objectForKey:@"north"] copyMagentaImageasColor:routeView.fillColor];
            [[shuttleImages objectForKey:@"north"] setValue:coloredImage forKey:[route.routeId stringValue]];
            [coloredImage release];
            
            coloredImage = [[magentaShuttleImages objectForKey:@"west"] copyMagentaImageasColor:routeView.fillColor];
            [[shuttleImages objectForKey:@"west"] setValue:coloredImage forKey:[route.routeId stringValue]];
            [coloredImage release];
            
            coloredImage = [[magentaShuttleImages objectForKey:@"south"] copyMagentaImageasColor:routeView.fillColor];
            [[shuttleImages objectForKey:@"south"] setValue:coloredImage forKey:[route.routeId stringValue]];
            [coloredImage release];
        }
    }
}


- (void)addStop:(Stop *)stop {
    CLLocationCoordinate2D clLoc;
    
    //  Get a CoreLocation coordinate from the point
    clLoc = CLLocationCoordinate2DMake([stop.latitude doubleValue], [stop.longitude doubleValue]);
    
    MapStop *mapStop = [[MapStop alloc] initWithLocation:clLoc];
    mapStop.name = stop.name;
    [_mapView addAnnotation:mapStop];
    [mapStop release];
}


- (MapVehicle *)addVehicle:(Shuttle *)vehicle {
    MapVehicle *newVehicle = [[MapVehicle alloc] init];
    
    //  Get a CoreLocation coordinate from the point
    CLLocationCoordinate2D clLoc = CLLocationCoordinate2DMake([vehicle.latitude doubleValue], [vehicle.longitude doubleValue]);
    newVehicle.coordinate = clLoc;
    newVehicle.heading = [vehicle.heading intValue];
    newVehicle.routeNo = [vehicle.routeId intValue];
    [newVehicle setUpdateTime:vehicle.updateTime withFormatter:self.dataManager.timeDisplayFormatter];
    newVehicle.name = vehicle.name;
    
    [_mapView addAnnotation:newVehicle];
    [vehicles setObject:newVehicle forKey:newVehicle.name];
    [newVehicle release];
    
    return newVehicle;
}


//  Set the vehicle's annotation view based on its current orientation
//  and associated route.
- (void)setVehicleAnnotationImage:(MapVehicle *)vehicle {
    //  Use the colored image for the shuttle's current route.  A route of -1 uses the white image.
    UIImage *coloredImage;
    NSMutableDictionary *shuttleDirectionImages;
    if (vehicle.heading >= 315 || vehicle.heading < 45) {
        shuttleDirectionImages = [shuttleImages objectForKey:@"north"];
    } else if (vehicle.heading >= 45 && vehicle.heading < 135) {
        shuttleDirectionImages = [shuttleImages objectForKey:@"east"];
    } else if (vehicle.heading >= 135 && vehicle.heading < 225) {
        shuttleDirectionImages = [shuttleImages objectForKey:@"south"];
    } else if (vehicle.heading >= 225 && vehicle.heading < 315) {
        shuttleDirectionImages = [shuttleImages objectForKey:@"west"];
    } else {
        shuttleDirectionImages = [shuttleImages objectForKey:@"east"];
    }
    
    coloredImage = [shuttleDirectionImages objectForKey:[[NSNumber numberWithInt:vehicle.routeNo] stringValue]];
    
    if (coloredImage != nil) {
        vehicle.annotationView.image = coloredImage;
        vehicle.routeImageSet = YES;
    } else {
        vehicle.annotationView.image = shuttleImage;
        vehicle.routeImageSet = NO;
    }
}

//	InAppSettingsKit sends out a notification whenever a setting is changed in the settings view inside the app.
//	settingChanged currently only handles turning on or off showing the user's location.
//	Other objects may also do something when a setting is changed.
- (void)settingChanged:(NSNotification *)notification {
	NSDictionary *info = [notification userInfo];
	
	if ([[notification object] isEqualToString:@"useLocation"]) {
		if ([[info objectForKey:@"useLocation"] boolValue]) {
			_mapView.showsUserLocation = YES;
		} else {
			_mapView.showsUserLocation = NO;
		}
	}
}


// Override to allow orientations other than the default portrait orientation.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations.
    return YES;
}


- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc. that aren't in use.
}


- (void)dealloc {
    [_mapView release];
	[shuttleImage release];
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
    
    if ([annotation isKindOfClass:[MapStop class]]) {
		if ([(MapStop *)annotation annotationView]) {
			return [(MapStop *)annotation annotationView];
		}
		
		MKAnnotationView *stopAnnotationView = [[[MKAnnotationView alloc] initWithAnnotation:(MapStop *)annotation
                                                                             reuseIdentifier:@"stopAnnotation"] autorelease];
        stopAnnotationView.image = [UIImage imageNamed:@"stop_marker"];
        stopAnnotationView.canShowCallout = YES;
        
        [(MapStop *)annotation setAnnotationView:stopAnnotationView];
		
		return stopAnnotationView;
    } else if ([annotation isKindOfClass:[MapVehicle class]]) {
        MapVehicle *vehicle = (MapVehicle *)annotation;
        
        if ([vehicle annotationView]) {
            //  Check to see if the vehicle's image is the plain shuttle image.
            //  If it is, check for a colored shuttle image for the shuttle's route.
            //  Set the shuttle's image to the colored one, if we have it.
            if (!vehicle.routeImageSet) {
                [self setVehicleAnnotationImage:vehicle];
            }
            
            return [vehicle annotationView];
        } else {
            MKAnnotationView *vehicleAnnotationView = [[[MKAnnotationView alloc] initWithAnnotation:vehicle
                                                                                    reuseIdentifier:@"vehicleAnnotation"] autorelease];
            
            [self setVehicleAnnotationImage:vehicle];
            
            vehicleAnnotationView.canShowCallout = YES;
            
            [vehicle setAnnotationView:vehicleAnnotationView];
        }
		
		return vehicle.annotationView;
    }
    
    return nil;
}

#pragma mark - Split view

- (void)splitViewController:(UISplitViewController *)splitController willHideViewController:(UIViewController *)viewController withBarButtonItem:(UIBarButtonItem *)barButtonItem forPopoverController:(UIPopoverController *)popoverController
{
    barButtonItem.title = NSLocalizedString(@"ETAs", @"ETAs");
    [self.navigationItem setLeftBarButtonItem:barButtonItem animated:YES];
    self.masterPopoverController = popoverController;
}

- (void)splitViewController:(UISplitViewController *)splitController willShowViewController:(UIViewController *)viewController invalidatingBarButtonItem:(UIBarButtonItem *)barButtonItem
{
    // Called when the view is shown again in the split view, invalidating the button and popover controller.
    [self.navigationItem setLeftBarButtonItem:nil animated:YES];
    self.masterPopoverController = nil;
}

@end

@implementation UIColor (stringcolor)

//  Take an NSString formatted as such: RRGGBB and return a UIColor
//  Note that this removes any '#' characters from rgbString
//  before doing anything.
+ (UIColor *)UIColorFromRGBString:(NSString *)rgbString {
    NSScanner *scanner;
    unsigned int rgbValue;
    
    rgbString = [rgbString stringByTrimmingCharactersInSet:[NSCharacterSet characterSetWithCharactersInString:@"#"]];
    
    if (rgbString) {
        scanner = [NSScanner scannerWithString:rgbString];
        [scanner scanHexInt:&rgbValue];
        
    } else {
        rgbValue = 0;
    }
    
    //  From the JSON, color comes as RGB
    UIColor *colorToReturn = [UIColor colorWithRed:((float)((rgbValue & 0xFF0000) >> 16))/255.0
                                             green:((float)((rgbValue & 0xFF00) >> 8))/255.0
                                              blue:((float)((rgbValue & 0xFF)))/255.0
                                             alpha:1];
    
    return colorToReturn;
}

//  Take an NSString formatted as such: RRGGBBAA and return a UIColor
+ (UIColor *)UIColorFromRGBAString:(NSString *)rgbaString {
    NSScanner *scanner;
    unsigned int rgbaValue;
    
    if (rgbaString) {
        scanner = [NSScanner scannerWithString:rgbaString];
        [scanner scanHexInt:&rgbaValue];
        
    } else {
        rgbaValue = 0;
    }
    
    //  Assume ABGR format and convert appropriately
    UIColor *colorToReturn = [UIColor colorWithRed:((float)((rgbaValue & 0xFF)))/255.0
                                             green:((float)((rgbaValue & 0xFF00) >> 8))/255.0
                                              blue:((float)((rgbaValue & 0xFF0000) >> 16))/255.0
                                             alpha:((float)((rgbaValue & 0xFF000000) >> 24))/255.0];
    
    return colorToReturn;
}

@end
