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
    NSMutableArray *vehicles;
    
    NSURL *jsonUrl;
}

@property (nonatomic, retain) NSMutableArray *vehicles;

- (id)initWithUrl:(NSURL *)url;
- (BOOL)parse;


@end


@interface JSONVehicle : NSObject <MKAnnotation> {
    NSString *name;
    NSString *description;
    
    CLLocationCoordinate2D coordinate;
    NSDictionary *ETAs;
    
    int heading;
    
    MKAnnotationView *annotationView;
    
}

@property (nonatomic, retain) NSString *name;
@property (nonatomic, readonly) NSString *title;
@property (nonatomic, retain) NSString *description;
@property (nonatomic, readonly) NSString *subtitle;
@property (nonatomic) CLLocationCoordinate2D coordinate;
@property (nonatomic, retain) NSDictionary *ETAs;
@property (nonatomic) int heading;
@property (nonatomic, retain) MKAnnotationView *annotationView;


@end
