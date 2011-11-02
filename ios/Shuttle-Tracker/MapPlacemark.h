//
//  MapPlacemark.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 4/8/11.
//  Copyright 2011 Naga Softworks, LLC. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <MapKit/MapKit.h>

@class PlacemarkStyle;

@interface MapPlacemark : NSObject {
    NSString *name;
	NSString *idTag;
	NSString *description;
	NSString *styleUrl;
    
	PlacemarkStyle *style;

    NSDateFormatter *timeDisplayFormatter;
}

@property (nonatomic, retain) NSString *name;
@property (nonatomic, readonly) NSString *title;
@property (nonatomic, retain) NSString *idTag;
@property (nonatomic, retain) NSString *description;
@property (nonatomic, readonly) NSString *subtitle;
@property (nonatomic, retain) NSString *styleUrl;
@property (nonatomic, retain) PlacemarkStyle *style;
@property (nonatomic, retain) NSDateFormatter *timeDisplayFormatter;

@end


//  Use to hold style objects, I have only seen these used for routes
@interface PlacemarkStyle : NSObject {
	NSString *idTag;
	NSString *colorString;
	UIColor *color;
	int width;
}

@property (nonatomic, retain) NSString *idTag;
@property (nonatomic, retain) NSString *colorString;
@property (nonatomic, readonly) UIColor *color;
@property (nonatomic) int width;

@end


//  Routes consist of a list of coordinates, so use an array for coordinates storage
@interface MapRoute : MapPlacemark
{
	NSArray *lineString;
}

@property (nonatomic, retain) NSArray *lineString;


@end

#pragma mark -
#pragma mark Points

//  Base class for objects which have a single set of coordinates, so just use one set of coordinates
@interface MapPoint : MapPlacemark <MKAnnotation>
{
	CLLocationCoordinate2D coordinate;
	MKAnnotationView *annotationView;
}

@property (nonatomic) CLLocationCoordinate2D coordinate;
@property (nonatomic, retain) MKAnnotationView *annotationView;


- (id)initWithLocation:(CLLocationCoordinate2D)coord;

@end

@interface MapStop : MapPoint
{
    NSArray *routeIds;
    NSArray *routeNames;
}

@property (nonatomic, retain) NSArray *routeIds;
@property (nonatomic, retain) NSArray *routeNames;

@end

@interface MapVehicle : MapPoint {
    NSDictionary *ETAs;
    int heading;
	NSDate *updateTime;
    int routeNo;
	
}

@property (nonatomic, retain) NSDictionary *ETAs;
@property (nonatomic) int heading;
@property (nonatomic, retain) NSDate *updateTime;
@property (nonatomic) int routeNo;
@property (nonatomic) BOOL routeImageSet;
@property (nonatomic) BOOL viewNeedsUpdate;

- (void)copyAttributesExceptLocation:(MapVehicle *)newVehicle;

@end
