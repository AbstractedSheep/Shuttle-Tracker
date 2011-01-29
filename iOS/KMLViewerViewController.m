/*
 From original header:
 
     File: KMLViewerViewController.m 
 Abstract: 
 Displays an MKMapView and demonstrates how to use the included KMLParser class to place annotations and overlays from a parsed KML file on top of the MKMapView.
  
  Version: 1.1 
  
 Copyright (C) 2010 Apple Inc. All Rights Reserved. 
  
 Shuttle Tracker header:
 
 Version 1.0
 Copyright (C) 2011 Brendon Justin
 */

#import "KMLViewerViewController.h"

@implementation KMLViewerViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    self.view = [[[UIView alloc] initWithFrame:[[UIScreen mainScreen] bounds]] autorelease];
	
    map = [[MKMapView alloc] initWithFrame:self.view.frame];
	map.delegate = self;
	
	[self.view addSubview:map];
    
    // Locate the path to the route.kml file in the application's bundle
    // and parse it with the KMLParser.
    NSString *path = [[NSBundle mainBundle] pathForResource:@"netlink" ofType:@"kml"];
    kml = [[KMLParser parseKMLAtPath:path] retain];
    
    // Add all of the MKOverlay objects parsed from the KML file to the map.
    NSArray *overlays = [kml overlays];
    [map addOverlays:overlays];
    
    // Add all of the MKAnnotation objects parsed from the KML file to the map.
    NSArray *annotations = [kml points];
    [map addAnnotations:annotations];
    
    // Walk the list of overlays and annotations and create a MKMapRect that
    // bounds all of them and store it into flyTo.
    MKMapRect flyTo = MKMapRectNull;
    for (id <MKOverlay> overlay in overlays) {
        if (MKMapRectIsNull(flyTo)) {
            flyTo = [overlay boundingMapRect];
        } else {
            flyTo = MKMapRectUnion(flyTo, [overlay boundingMapRect]);
        }
    }
    
    for (id <MKAnnotation> annotation in annotations) {
        MKMapPoint annotationPoint = MKMapPointForCoordinate(annotation.coordinate);
        MKMapRect pointRect = MKMapRectMake(annotationPoint.x, annotationPoint.y, 0, 0);
        if (MKMapRectIsNull(flyTo)) {
            flyTo = pointRect;
        } else {
            flyTo = MKMapRectUnion(flyTo, pointRect);
        }
    }
    
    // Position the map so that all overlays and annotations are visible on screen.
    map.visibleMapRect = flyTo;
	
	NSLog(@"Shuttle data url: %@", [kml shuttleDataUrl]);
}

#pragma mark MKMapViewDelegate

- (MKOverlayView *)mapView:(MKMapView *)mapView viewForOverlay:(id <MKOverlay>)overlay
{
    return [kml viewForOverlay:overlay];
}

- (MKAnnotationView *)mapView:(MKMapView *)mapView viewForAnnotation:(id <MKAnnotation>)annotation
{
    return [kml viewForAnnotation:annotation];
}

- (void)dealloc {
    [map release];
    [super dealloc];
}
@end
