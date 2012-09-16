//
//  STMapPlacemark.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 4/8/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <MapKit/MapKit.h>

@class STPlacemarkStyle;

@interface STMapPlacemark : NSObject {
    NSString *name;
    NSString *idTag;
    NSString *description;
    NSString *styleUrl;
    
    STPlacemarkStyle *style;

    NSDateFormatter *timeDisplayFormatter;
}

@property (nonatomic, strong) NSString *name;
@property (weak, nonatomic, readonly) NSString *title;
@property (nonatomic, strong) NSString *idTag;
@property (nonatomic, strong) NSString *description;
@property (weak, nonatomic, readonly) NSString *subtitle;
@property (nonatomic, strong) NSString *styleUrl;
@property (nonatomic, strong) STPlacemarkStyle *style;
@property (nonatomic, strong) NSDateFormatter *timeDisplayFormatter;

@end


//  Use to hold style objects, I have only seen these used for routes
@interface STPlacemarkStyle : NSObject {
    NSString *idTag;
    NSString *colorString;
    UIColor *__weak color;
    int width;
}

@property (nonatomic, strong) NSString *idTag;
@property (nonatomic, strong) NSString *colorString;
@property (weak, nonatomic, readonly) UIColor *color;
@property (nonatomic) int width;

@end


//  Routes consist of a list of coordinates, so use an array for coordinates storage
@interface STMapRoute : STMapPlacemark
{
    NSArray *lineString;
}

@property (nonatomic, strong) NSArray *lineString;


@end

#pragma mark -
#pragma mark Points

//  Base class for objects which have a single set of coordinates, so just use one set of coordinates
@interface STMapPoint : STMapPlacemark <MKAnnotation>
{
    CLLocationCoordinate2D coordinate;
    MKAnnotationView *annotationView;
}

@property (nonatomic) CLLocationCoordinate2D coordinate;
@property (nonatomic, strong) MKAnnotationView *annotationView;


- (id)initWithLocation:(CLLocationCoordinate2D)coord;

@end

@interface STMapStop : STMapPoint
{
    NSArray *routeIds;
    NSArray *routeNames;
}

@property (nonatomic, strong) NSArray *routeIds;
@property (nonatomic, strong) NSArray *routeNames;

@end

@interface STMapVehicle : STMapPoint {
    NSDictionary *ETAs;
    int heading;
    NSDate *updateTime;
    int routeId;
}

@property (nonatomic, strong) NSDictionary *ETAs;
@property (nonatomic) int heading;
@property (nonatomic, strong) NSDate *updateTime;
@property (nonatomic) int routeId;
@property (nonatomic) BOOL routeImageSet;
@property (nonatomic) BOOL viewNeedsUpdate;

- (void)copyAttributesExceptLocation:(STMapVehicle *)newVehicle;
- (void)setUpdateTime:(NSDate *)updateTime withFormatter:(NSDateFormatter *)dateFormatter;

@end
