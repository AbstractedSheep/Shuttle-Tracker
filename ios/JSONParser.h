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
    NSDictionary *jsonDict;
    NSMutableArray *vehicles;
    
    NSURL *jsonUrl;
}

@property (nonatomic, retain) NSDictionary *jsonDict;
@property (nonatomic, retain) NSMutableArray *vehicles;

- (id)initWithUrl:(NSURL *)url;
- (void)parse;


@end


@interface JSONVehicle : NSObject <MKAnnotation> {
    NSString *name;
    NSString *description;
    
    CLLocationCoordinate2D coordinate;
    NSDictionary *ETAs;
    
    MKAnnotationView *annotationView;
    
}

@property (nonatomic, retain) NSString *name;
@property (nonatomic, retain) NSString *description;
@property (nonatomic) CLLocationCoordinate2D coordinate;
@property (nonatomic, retain) NSDictionary *ETAs;
@property (nonatomic, retain) MKAnnotationView *annotationView;


@end