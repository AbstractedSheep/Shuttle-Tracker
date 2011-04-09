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

@interface PlacemarkStyle ()

- (UIColor *)UIColorFromRGBAString:(NSString *)rgbaString;

@end

@implementation PlacemarkStyle

@synthesize idTag;
@synthesize color;
@synthesize colorString;
@synthesize width;

- (void)setColorString:(NSString *)newColorString {
    colorString = newColorString;
    [colorString retain];
    
    color = [self UIColorFromRGBAString:colorString];
	[color retain];
}


//  Take an NSString formatted as such: RRGGBBAA and return a UIColor
- (UIColor *)UIColorFromRGBAString:(NSString *)rgbaString {
    NSScanner *scanner;
    unsigned int rgbaValue;
    
    if (rgbaString) {
        scanner = [NSScanner scannerWithString:rgbaString];
        [scanner scanHexInt:&rgbaValue];
        
    } else {
        rgbaValue = 0;
    }
    
    //  For whatever reason, the color comes in as ABGR
    return [UIColor colorWithRed:((float)((rgbaValue & 0xFF)))/255.0
                           green:((float)((rgbaValue & 0xFF00) >> 8))/255.0
                            blue:((float)((rgbaValue & 0xFF0000) >> 16))/255.0
                           alpha:((float)((rgbaValue & 0xFF000000) >> 24))/255.0];
}

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