//
//  JSONParser.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 2/12/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <MapKit/MapKit.h>


@interface JSONParser : NSObject {
    NSArray *routes;
    NSArray *stops;
    
    NSMutableArray *vehicles;
    NSMutableArray *etas;
    
    NSURL *jsonUrl;
	NSDateFormatter *timeDisplayFormatter;
}

@property (nonatomic, retain) NSArray *routes;
@property (nonatomic, retain) NSArray *stops;
@property (nonatomic, retain) NSMutableArray *vehicles;
@property (nonatomic, retain) NSMutableArray *etas;
@property (nonatomic, assign) NSDateFormatter *timeDisplayFormatter;


- (id)initWithUrl:(NSURL *)url;
- (BOOL)parseRoutesandStops;
- (BOOL)parseShuttles;
- (BOOL)parseEtas;


@end


@interface JSONPlacemark : NSObject <MKAnnotation> {
    NSString *name;
    NSString *description;
	NSString *subtitle;
    
    CLLocationCoordinate2D coordinate;
    
    MKAnnotationView *annotationView;
	NSDateFormatter *timeDisplayFormatter;
    
}

@property (nonatomic, retain) NSString *name;
@property (nonatomic, readonly) NSString *title;
@property (nonatomic, retain) NSString *description;
@property (nonatomic, retain) NSString *subtitle;
@property (nonatomic) CLLocationCoordinate2D coordinate;
@property (nonatomic, retain) MKAnnotationView *annotationView;
@property (nonatomic, assign) NSDateFormatter *timeDisplayFormatter;


@end


@interface JSONStop : JSONPlacemark {
    
}


@end

@interface JSONVehicle : JSONPlacemark {
    NSDictionary *ETAs;
    int heading;
	NSDate *updateTime;
    int routeNo;
	
}

@property (nonatomic, retain) NSDictionary *ETAs;
@property (nonatomic) int heading;
@property (nonatomic, retain) NSDate *updateTime;
@property (nonatomic) int routeNo;

- (void)copyAttributesExceptLocation:(JSONVehicle *)newVehicle;

@end
