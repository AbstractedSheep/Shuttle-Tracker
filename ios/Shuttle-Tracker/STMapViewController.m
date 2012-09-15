//
//  STMapViewController.m
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 1/29/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import "STMapViewController.h"

#import "STMapPlacemark.h"
#import "STRoute.h"
#import "STRoutePt.h"
#import "STShuttle.h"
#import "STStop.h"

const NSTimeInterval UPDATE_THRESHOLD = -180.0f;    //  3 minutes
const NSTimeInterval CLEANUP_INTERVAL = 30.0f;      //  30 seconds

@interface UIImage (magentatocolor)

- (UIImage *)copyMagentaImageasColor:(UIColor *)newColor;

@end

@interface UIColor (stringcolor)

+ (UIColor *)UIColorFromRGBString:(NSString *)rgbString;
+ (UIColor *)UIColorFromRGBAString:(NSString *)rgbaString;

@end

//  From Stack Overflow (SO), with modifications:
@implementation UIImage (magentatocolor)

typedef enum {
    ALPHA = 0,
    BLUE = 1,
    GREEN = 2,
    RED = 3
} PIXELS;


//  Convert the magenta pixels in an image to a new color.
//  Returns a new autoreleased image.
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
//  End from SO


@interface STMapViewController()

@property (strong, nonatomic) UIPopoverController *masterPopoverController;

- (void)managedRoutesLoaded;
//  notifyVehiclesUpdated may not be called on the main thread, so use it to call
//  vehicles updated on the main thread.
- (void)notifyVehiclesUpdated:(NSNotification *)notification;
- (void)vehiclesUpdated:(NSNotification *)notification;
//  Adding routes and stops is not guaranteed to be done on the main thread.
- (void)addRoute:(STRoute *)route;
- (void)addStop:(STStop *)stop;
//  Adding vehicles should only be done on the main thread.
- (void)addVehicle:(STShuttle *)vehicle;
- (void)setAnnotationImageForVehicle:(STMapVehicle *)vehicle;

@end

@implementation STMapViewController

@synthesize dataManager = m_dataManager;
@synthesize managedObjectContext = __managedObjectContext;
@synthesize masterPopoverController = _masterPopoverController;

- (id)init {
    if ( (self = [super init]) ) {
        UIImage *magentaShuttleImage, *whiteImage;
        m_vehicles = [[NSMutableDictionary alloc] init];
        
        m_routeLines = [[NSMutableArray alloc] init];
        m_routeLineViews = [[NSMutableArray alloc] init];
        
        m_shuttleImage = [UIImage imageNamed:@"shuttle"];
        [m_shuttleImage retain];
        
        m_magentaShuttleImages = [[NSMutableDictionary alloc] initWithCapacity:4];
        m_shuttleImages = [[NSMutableDictionary alloc] initWithCapacity:4];
        NSMutableDictionary *shuttleImagesEast = [[NSMutableDictionary alloc] init];
        NSMutableDictionary *shuttleImagesNorth = [[NSMutableDictionary alloc] init];
        NSMutableDictionary *shuttleImagesWest = [[NSMutableDictionary alloc] init];
        NSMutableDictionary *shuttleImagesSouth = [[NSMutableDictionary alloc] init];
        
        //  Create east, north, west and south facing shuttle images
        //  East
        magentaShuttleImage = [UIImage imageNamed:@"shuttle_color_east"];
        [m_magentaShuttleImages setObject:magentaShuttleImage forKey:@"east"];
        
        whiteImage = [magentaShuttleImage copyMagentaImageasColor:[UIColor whiteColor]];
        [shuttleImagesEast setObject:whiteImage forKey:[@-1 stringValue]];
        [whiteImage release];
        
        //  North
        magentaShuttleImage = [UIImage imageNamed:@"shuttle_color_north"];
        [m_magentaShuttleImages setObject:magentaShuttleImage forKey:@"north"];
        
        whiteImage = [magentaShuttleImage copyMagentaImageasColor:[UIColor whiteColor]];
        [shuttleImagesNorth setObject:whiteImage forKey:[@-1 stringValue]];
        [whiteImage release];
        
        //  West
        magentaShuttleImage = [UIImage imageNamed:@"shuttle_color_west"];
        [m_magentaShuttleImages setObject:magentaShuttleImage forKey:@"west"];
        
        whiteImage = [magentaShuttleImage copyMagentaImageasColor:[UIColor whiteColor]];
        [shuttleImagesWest setObject:whiteImage forKey:[@-1 stringValue]];
        [whiteImage release];
        
        //  South
        magentaShuttleImage = [UIImage imageNamed:@"shuttle_color_south"];
        [m_magentaShuttleImages setObject:magentaShuttleImage forKey:@"south"];
        
        whiteImage = [magentaShuttleImage copyMagentaImageasColor:[UIColor whiteColor]];
        [shuttleImagesSouth setObject:whiteImage forKey:[@-1 stringValue]];
        [whiteImage release];
        
        [m_shuttleImages setObject:shuttleImagesEast forKey:@"east"];
        [m_shuttleImages setObject:shuttleImagesNorth forKey:@"north"];
        [m_shuttleImages setObject:shuttleImagesWest forKey:@"west"];
        [m_shuttleImages setObject:shuttleImagesSouth forKey:@"south"];
        [shuttleImagesEast release];
        [shuttleImagesNorth release];
        [shuttleImagesWest release];
        [shuttleImagesSouth release];
        
        //  Take notice when the routes and stops are updated.
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(managedRoutesLoaded)
                                                     name:kDMRoutesandStopsLoaded
                                                   object:nil];

        //  Take notice when vehicles are updated.
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(notifyVehiclesUpdated:)
                                                     name:kDMVehiclesUpdated
                                                   object:nil];
    }
    
    return self;
}

// Implement loadView to create a view hierarchy programmatically, without using a nib.
- (void)loadView {
    self.title = NSLocalizedString(@"Shuttles", @"Shuttles");
    
    m_mapView = [[MKMapView alloc] init];
    m_mapView.delegate = self;
    m_mapView.autoresizingMask = UIViewAutoresizingFlexibleHeight | UIViewAutoresizingFlexibleWidth;
    
    MKUserTrackingBarButtonItem *buttonItem = [[MKUserTrackingBarButtonItem alloc] initWithMapView:m_mapView];
    self.navigationController.toolbarHidden = NO;
    self.toolbarItems = @[buttonItem];
    
    self.view = m_mapView;
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];
    
    //  The RPI student union is at -73.6765441399,42.7302712352
    //  The center point used here is a bit south of it
    MKCoordinateRegion region;
    region.center.latitude = 42.7312;
    region.center.longitude = -73.6750;
    region.span.latitudeDelta = 0.0200;
    region.span.longitudeDelta = 0.0132;
    
    m_mapView.region = region;
    
    [m_dataManager loadRoutesAndStops];
    
    [self vehicleCleanup];
    m_shuttleCleanupTimer = [NSTimer scheduledTimerWithTimeInterval:CLEANUP_INTERVAL
                                                             target:self
                                                           selector:@selector(vehicleCleanup)
                                                           userInfo:nil
                                                            repeats:YES];
}

- (void)viewDidUnload {
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
    
    [m_shuttleCleanupTimer invalidate];
}

//  Consider shuttles updated more than UPDATE_THRESHOLD ago as "stale",
//  and remove them.
- (void)vehicleCleanup {
    NSMutableArray *oldVehicles = [NSMutableArray array];
    STMapVehicle *vehicle;
    
    for (NSString *name in m_vehicles) {
        vehicle = [m_vehicles objectForKey:name];
        if ([vehicle.updateTime timeIntervalSinceNow] < UPDATE_THRESHOLD) {
            [oldVehicles addObject:name];
        }
    }
    
    for (NSString *name in oldVehicles) {
        [m_mapView removeAnnotation:[m_vehicles objectForKey:name]];
        [m_vehicles removeObjectForKey:name];
    }
}

//  The routes and stops were loaded in the dataManager
- (void)managedRoutesLoaded {
    //  Get all routes
    NSEntityDescription *routeEntityDescription = [NSEntityDescription entityForName:@"STRoute"
                                                              inManagedObjectContext:self.managedObjectContext];
    NSFetchRequest *routeRequest = [[[NSFetchRequest alloc] init] autorelease];
    [routeRequest setEntity:routeEntityDescription];
    
    NSError *error = nil;
    NSArray *dbRoutes = [self.managedObjectContext executeFetchRequest:routeRequest
                                                                 error:&error];
    if (dbRoutes == nil)
    {
        // Deal with error...
    } else if ([dbRoutes count] > 0) {
        for (STRoute *route in dbRoutes) {
            [self addRoute:route];
        }
    } else {
        //  No routes, so do nothing
    }
    
    //  Get all stops
    NSEntityDescription *stopEntityDescription = [NSEntityDescription entityForName:@"STStop"
                                                             inManagedObjectContext:self.managedObjectContext];
    NSFetchRequest *stopRequest = [[[NSFetchRequest alloc] init] autorelease];
    [stopRequest setEntity:stopEntityDescription];
    
    error = nil;
    NSArray *dbStops = [self.managedObjectContext executeFetchRequest:stopRequest
                                                                error:&error];
    if (dbStops == nil)
    {
        // Deal with error...
    } else if ([dbStops count] > 0) {
        for (STStop *stop in dbStops) {
            [self addStop:stop];
        }
    } else {
        //  No stops, so do nothing
    }
}

//  A notification is sent by DataManager whenever the vehicles are updated.
//  Call the work function vehiclesUpdated on the main thread.
- (void)notifyVehiclesUpdated:(NSNotification *)notification {
    [self performSelectorOnMainThread:@selector(vehiclesUpdated:)
                           withObject:notification
                        waitUntilDone:NO];
}

//  A notification is sent by DataManager whenever the vehicles are updated.
- (void)vehiclesUpdated:(NSNotification *)notification {
    //  Get all vehicles
    NSEntityDescription *entityDescription;
    entityDescription = [NSEntityDescription entityForName:@"STShuttle"
                                    inManagedObjectContext:self.managedObjectContext];
    NSFetchRequest *request = [[[NSFetchRequest alloc] init] autorelease];
    [request setEntity:entityDescription];
    
    NSError *error = nil;
    NSArray *dbVehicles;
    dbVehicles = [self.managedObjectContext executeFetchRequest:request
                                                          error:&error];
    
    STMapVehicle *existingShuttle;
    double updateTimeDiff, latitude, longitude;
    if (error != nil || dbVehicles == nil)
    {
        // Deal with error...
    } else if ([dbVehicles count] > 0) {
        for (STShuttle *shuttle in dbVehicles) {
            existingShuttle = [m_vehicles objectForKey:shuttle.name];
            updateTimeDiff = [shuttle.updateTime timeIntervalSinceNow];
            
            if (updateTimeDiff > UPDATE_THRESHOLD) {
                if (existingShuttle == nil) {
                    //  Add the shuttle to the map view and our array of active shuttles
                    [self addVehicle:shuttle];
                } else {
                    if ([shuttle.routeId intValue] != existingShuttle.routeId
                        || [shuttle.heading intValue] != existingShuttle.heading) {
                        //  If the shuttle switched routes, then update the image.
                        //  Also update the image if the shuttle has changed heading
                        
                        existingShuttle.routeId = [shuttle.routeId intValue];
                        existingShuttle.heading = [shuttle.heading intValue];
                        
                        //  Flag the vehicle to have its shuttle image updated
                        existingShuttle.routeImageSet = NO;
                    } else if ([shuttle.updateTime timeIntervalSinceDate:existingShuttle.updateTime] > 0) {
                        //  If the shuttle location is out of date, update it
                        
                        latitude = [shuttle.latitude doubleValue];
                        longitude = [shuttle.longitude doubleValue];
                        CLLocationCoordinate2D clLoc = CLLocationCoordinate2DMake(latitude, longitude);
                        existingShuttle.coordinate = clLoc;
                    } else {
                        //  The shuttle has not been updated, leave it be
                    }
                    
                    //  Make sure the shuttle's update time is current
                    [existingShuttle setUpdateTime:shuttle.updateTime
                                     withFormatter:self.dataManager.timeDisplayFormatter];
                }
            }
        }
    } else {
        //  No vehicles, so do nothing
    }
}


//  Add the overlay for the route to the map view, and create a shuttle image with
//  a color matching the route's color
- (void)addRoute:(STRoute *)route {
    CLLocationCoordinate2D clLoc;
    MKMapPoint *points;
    UIImage *coloredImage;
    
    //  Get all points on the route
    NSEntityDescription *entityDescription = [NSEntityDescription entityForName:@"STRoutePt"
                                                         inManagedObjectContext:self.managedObjectContext];
    NSFetchRequest *request = [[[NSFetchRequest alloc] init] autorelease];
    [request setEntity:entityDescription];
    
    // Set predicate and sort orderings...
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"(route == %@)", route];
    [request setPredicate:predicate];
    
    NSSortDescriptor *sortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"pointNumber"
                                                                   ascending:YES];
    [request setSortDescriptors:@[sortDescriptor]];
    [sortDescriptor release];
    
    NSError *error = nil;
    NSArray *routePts = [self.managedObjectContext executeFetchRequest:request
                                                                 error:&error];
    
    double latitude, longitude;
    if (error != nil || routePts == nil)
    {
        // Deal with error...
    } else {
        MKPolyline *polyLine;
        MKPolylineView *routeView;
        
        points = malloc(sizeof(MKMapPoint) * routePts.count);
        
        //  Create an array of coordinates for the polyline which will represent the route
        int counter = 0;
        for (STRoutePt *point in routePts) {
            //  Get a CoreLocation coordinate from the point
            latitude = [point.latitude doubleValue];
            longitude = [point.longitude doubleValue];
            clLoc = CLLocationCoordinate2DMake(latitude, longitude);
            
            points[counter] = MKMapPointForCoordinate(clLoc);
            counter++;
        }
        
        polyLine = [MKPolyline polylineWithPoints:points
                                            count:counter];
        [m_routeLines addObject:polyLine];
        
        free(points);
        
        routeView = [[MKPolylineView alloc] initWithPolyline:polyLine];
        [m_routeLineViews addObject:routeView];
        [routeView release];
        
        routeView.lineWidth = [route.width intValue];
        routeView.fillColor = [UIColor UIColorFromRGBString:route.color];
        routeView.strokeColor = routeView.fillColor;
        
        [m_mapView addOverlay:polyLine];
        
        //  Create the colored shuttle image for the route
        
        if (routeView.fillColor) {
            coloredImage = [[m_magentaShuttleImages objectForKey:@"west"] copyMagentaImageasColor:routeView.fillColor];
            [[m_shuttleImages objectForKey:@"east"] setValue:coloredImage
                                                      forKey:[route.routeId stringValue]];
            [coloredImage release];
            
            coloredImage = [[m_magentaShuttleImages objectForKey:@"north"] copyMagentaImageasColor:routeView.fillColor];
            [[m_shuttleImages objectForKey:@"north"] setValue:coloredImage
                                                       forKey:[route.routeId stringValue]];
            [coloredImage release];
            
            coloredImage = [[m_magentaShuttleImages objectForKey:@"west"] copyMagentaImageasColor:routeView.fillColor];
            [[m_shuttleImages objectForKey:@"west"] setValue:coloredImage
                                                      forKey:[route.routeId stringValue]];
            [coloredImage release];
            
            coloredImage = [[m_magentaShuttleImages objectForKey:@"south"] copyMagentaImageasColor:routeView.fillColor];
            [[m_shuttleImages objectForKey:@"south"] setValue:coloredImage
                                                       forKey:[route.routeId stringValue]];
            [coloredImage release];
        }
    }
}


- (void)addStop:(STStop *)stop {
    double latitude, longitude;
    CLLocationCoordinate2D clLoc;
    
    latitude = [stop.latitude doubleValue];
    longitude = [stop.longitude doubleValue];
    
    //  Get a CoreLocation coordinate from the point
    clLoc = CLLocationCoordinate2DMake(latitude, longitude);
    
    STMapStop *mapStop = [[STMapStop alloc] initWithLocation:clLoc];
    mapStop.name = stop.name;
    [m_mapView addAnnotation:mapStop];
    [mapStop release];
}


- (void)addVehicle:(STShuttle *)vehicle {
    STMapVehicle *newVehicle = [[STMapVehicle alloc] init];
    double latitude, longitude;
    CLLocationCoordinate2D clLoc;
    
    latitude = [vehicle.latitude doubleValue];
    longitude = [vehicle.longitude doubleValue];
    
    //  Get a CoreLocation coordinate from the point
    clLoc = CLLocationCoordinate2DMake(latitude, longitude);
    newVehicle.coordinate = clLoc;
    newVehicle.heading = [vehicle.heading intValue];
    newVehicle.routeId = [vehicle.routeId intValue];
    [newVehicle setUpdateTime:vehicle.updateTime
                withFormatter:self.dataManager.timeDisplayFormatter];
    newVehicle.name = vehicle.name;
    
    [m_mapView addAnnotation:newVehicle];
    [m_vehicles setObject:newVehicle forKey:newVehicle.name];
    [newVehicle release];
}


//  Set the vehicle's annotation view based on its current orientation
//  and associated route.
- (void)setAnnotationImageForVehicle:(STMapVehicle *)vehicle {
    //  Use the colored image for the shuttle's current route.  A route
    //  of -1 uses the white image.
    UIImage *coloredImage;
    NSMutableDictionary *shuttleDirectionImages;
    NSString *routeString;
    
    if (vehicle.heading >= 315 || vehicle.heading < 45) {
        shuttleDirectionImages = [m_shuttleImages objectForKey:@"north"];
    } else if (vehicle.heading >= 45 && vehicle.heading < 135) {
        shuttleDirectionImages = [m_shuttleImages objectForKey:@"east"];
    } else if (vehicle.heading >= 135 && vehicle.heading < 225) {
        shuttleDirectionImages = [m_shuttleImages objectForKey:@"south"];
    } else if (vehicle.heading >= 225 && vehicle.heading < 315) {
        shuttleDirectionImages = [m_shuttleImages objectForKey:@"west"];
    } else {
        shuttleDirectionImages = [m_shuttleImages objectForKey:@"east"];
    }
    
    routeString = [@(vehicle.routeId) stringValue];
    coloredImage = [shuttleDirectionImages objectForKey:routeString];
    
    if (coloredImage != nil) {
        vehicle.annotationView.image = coloredImage;
        vehicle.routeImageSet = YES;
    } else {
        vehicle.annotationView.image = m_shuttleImage;
        vehicle.routeImageSet = NO;
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
    [m_mapView release];
    [m_shuttleImage release];
    [super dealloc];
}


#pragma mark MKMapViewDelegate

- (MKOverlayView *)mapView:(MKMapView *)mapView viewForOverlay:(id<MKOverlay>)overlay {
    MKOverlayView* overlayView = nil;
    
    int counter = 0;
    
    for (MKPolyline *routeLine in m_routeLines) {
        if (routeLine == overlay) {
            overlayView = [m_routeLineViews objectAtIndex:counter];
            break;
        }
        
        counter++;
    }
    
    return overlayView;
}

- (MKAnnotationView *)mapView:(MKMapView *)mapView viewForAnnotation:(id<MKAnnotation>)annotation {
    //  If the annotation is the user's location, return nil so the platform
    //  just uses the blue dot
    if (annotation == m_mapView.userLocation)
        return nil;
    
    if ([annotation isKindOfClass:[STMapStop class]]) {
        STMapStop *stop = (STMapStop *)annotation;
        
        if ([stop annotationView]) {
            return [stop annotationView];
        }

        MKAnnotationView *stopAnnotationView;
        stopAnnotationView = [mapView dequeueReusableAnnotationViewWithIdentifier:@"stopAnnotation"];
        
        if (stopAnnotationView == nil) {
            stopAnnotationView = [[MKAnnotationView alloc] initWithAnnotation:stop
                                                              reuseIdentifier:@"stopAnnotation"];
            stopAnnotationView.image = [UIImage imageNamed:@"stop_marker"];
            stopAnnotationView.canShowCallout = YES;
            [stopAnnotationView autorelease];
        }
        
        [(STMapStop *)annotation setAnnotationView:stopAnnotationView];

        return stopAnnotationView;
    } else if ([annotation isKindOfClass:[STMapVehicle class]]) {
        STMapVehicle *vehicle = (STMapVehicle *)annotation;
        
        if (vehicle.annotationView != nil) {
            //  Check to see if the vehicle's image is the plain shuttle image.
            //  If it is, check for a colored shuttle image for the shuttle's route.
            //  Set the shuttle's image to the colored one, if we have it.
            if (!vehicle.routeImageSet) {
                [self setAnnotationImageForVehicle:vehicle];
            }
        } else {
            MKAnnotationView *vehicleAnnotationView;
            vehicleAnnotationView = [mapView dequeueReusableAnnotationViewWithIdentifier:@"vehicleAnnotation"];
            
            if (vehicleAnnotationView == nil) {
                vehicleAnnotationView = [[MKAnnotationView alloc] initWithAnnotation:vehicle
                                                                     reuseIdentifier:@"vehicleAnnotation"];
                vehicleAnnotationView.canShowCallout = YES;
                [vehicleAnnotationView autorelease];
            }
            
            vehicle.annotationView = vehicleAnnotationView;
            
            [self setAnnotationImageForVehicle:vehicle];
        }

        return vehicle.annotationView;
    }
    
    return nil;
}

#pragma mark - Split view

- (void)splitViewController:(UISplitViewController *)splitController
     willHideViewController:(UIViewController *)viewController
          withBarButtonItem:(UIBarButtonItem *)barButtonItem
       forPopoverController:(UIPopoverController *)popoverController
{
    barButtonItem.title = NSLocalizedString(@"ETAs", @"ETAs");
    [self.navigationItem setLeftBarButtonItem:barButtonItem animated:YES];
    self.masterPopoverController = popoverController;
}

- (void)splitViewController:(UISplitViewController *)splitController
     willShowViewController:(UIViewController *)viewController
  invalidatingBarButtonItem:(UIBarButtonItem *)barButtonItem
{
    // Called when the view is shown again in the split view, invalidating the button
    // and popover controller.
    [self.navigationItem setLeftBarButtonItem:nil animated:YES];
    self.masterPopoverController = nil;
}

@end

@implementation UIColor (stringcolor)

//  Take an NSString formatted as RRGGBB and return a UIColor
//  Note that this removes any '#' characters from rgbString
//  before doing anything.
+ (UIColor *)UIColorFromRGBString:(NSString *)rgbString {
    NSScanner *scanner;
    unsigned int rgbValue;
    
    NSCharacterSet *charSet = [NSCharacterSet characterSetWithCharactersInString:@"#"];
    rgbString = [rgbString stringByTrimmingCharactersInSet:charSet];
    
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
