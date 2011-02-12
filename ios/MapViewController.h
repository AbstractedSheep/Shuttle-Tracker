//
//  MapViewController.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 1/29/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <MapKit/MapKit.h>


@class KMLParser;

@interface MapViewController : UIViewController <MKMapViewDelegate> {
	MKMapView *_mapView;
    KMLParser *routeKmlParser;
    KMLParser *vehiclesKmlParser;
    
    NSArray *routes;
    NSArray *stops;
    
    NSMutableArray *vehicles;
    
    NSMutableArray *routeLines;
    NSMutableArray *routeLineViews;
    
    NSTimer *vehicleUpdateTimer;
    
}

@end
