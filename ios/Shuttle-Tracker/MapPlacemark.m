//
//  MapPlacemark.m
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 4/8/11.
//  Copyright 2011 Naga Softworks, LLC. All rights reserved.
//

#import "MapPlacemark.h"


@implementation MapPlacemark

@synthesize name;
@synthesize title;
@synthesize idTag;
@synthesize description;
@synthesize subtitle;
@synthesize styleUrl;
@synthesize style;
@synthesize timeDisplayFormatter;

@end

@implementation PlacemarkStyle

@synthesize idTag;
@synthesize color;
@synthesize colorString;
@synthesize width;
@synthesize styleType;

@end

@implementation MapRoute

@synthesize lineString;

@end

@implementation MapPoint

@synthesize coordinate;
@synthesize annotationView;

- (id)initWithLocation:(CLLocationCoordinate2D)coord {
    [self init];
    
    if (self) {
        coordinate = coord;
    }
    
    return self;
}

@end

@implementation MapStop

@synthesize shortName;
@synthesize routeIds;
@synthesize routeNames;

@end

@implementation MapVehicle

@synthesize ETAs;
@synthesize heading;
@synthesize updateTime;
@synthesize routeNo;

- (void)copyAttributesExceptLocation:(MapVehicle *)newVehicle {
    
}

@end