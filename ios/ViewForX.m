//
//  ViewForX.m
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 2/10/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import "ViewForX.h"
#import "KMLParser.h"

@implementation ViewForX

@synthesize routeLines;
@synthesize routeLineViews;


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
    if (annotation == mapView.userLocation)
        return nil;
    
    if ([annotation isKindOfClass:[KMLStop class]]) {
        MKPinAnnotationView *pinAnnotationView = [[MKPinAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:[(KMLStop *)annotation idTag]];
        pinAnnotationView.pinColor = MKPinAnnotationColorPurple;
        pinAnnotationView.animatesDrop=TRUE;
        pinAnnotationView.canShowCallout = YES;
        
        return pinAnnotationView;
    }
    
    return nil;
}

@end
