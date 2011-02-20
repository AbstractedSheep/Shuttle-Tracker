//
//  JSONParser.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 2/12/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <MapKit/MapKit.h>
#import "EtaWrapper.h"


@interface JSONParser : NSObject {
    NSMutableArray *vehicles;
    NSMutableArray *stops;
    NSMutableArray *ETAs;
    
    NSURL *jsonUrl;
}

@property (nonatomic, retain) NSMutableArray *vehicles;

- (id)initWithUrl:(NSURL *)url;
- (BOOL)parse;
- (BOOL)parseShuttles;
- (BOOL)parseETAs;


@end


@interface JSONPlacemark : NSObject <MKAnnotation> {
    NSString *name;
    NSString *description;
    
    CLLocationCoordinate2D coordinate;
    
    MKAnnotationView *annotationView;
}

@property (nonatomic, retain) NSString *name;
@property (nonatomic, readonly) NSString *title;
@property (nonatomic, retain) NSString *description;
@property (nonatomic, readonly) NSString *subtitle;
@property (nonatomic) CLLocationCoordinate2D coordinate;
@property (nonatomic, retain) MKAnnotationView *annotationView;


@end


@interface JSONStop : JSONPlacemark {
    NSString *idTag;
    
}

@property (nonatomic, retain) NSString *idTag;


@end



@interface JSONVehicle : JSONPlacemark {
    int heading;
    
}


@property (nonatomic) int heading;


@end
