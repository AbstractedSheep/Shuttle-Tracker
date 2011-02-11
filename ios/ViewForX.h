//
//  ViewForX.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 2/10/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <MapKit/MapKit.h>


@interface ViewForX : NSObject <MKMapViewDelegate> {
    NSMutableArray *routeLines;
    NSMutableArray *routeLineViews;
}

@property (nonatomic, retain) NSMutableArray *routeLines;
@property (nonatomic, retain) NSMutableArray *routeLineViews;

@end
