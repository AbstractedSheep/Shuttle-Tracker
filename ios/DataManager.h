//
//  EtaManager.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 2/20/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "KMLParser.h"
#import "JSONParser.h"


@interface DataManager : NSObject {
    NSURL *shuttleJsonUrl;
    NSURL *etasJsonUrl;
    
    KMLParser *routeKmlParser;
    KMLParser *vehiclesKmlParser;
    JSONParser *vehiclesJsonParser;
    JSONParser *etasJsonParser;
    
    
    NSArray *routes;
    NSArray *stops;
    
    NSMutableArray *vehicles;
    
    NSArray *etas;
    NSInteger eastEtas;
    NSInteger westEtas;
}

@property (nonatomic, retain) NSArray *routes;
@property (nonatomic, retain) NSArray *stops;
@property (nonatomic, retain) NSMutableArray *vehicles;
@property (nonatomic, retain) NSArray *etas;
@property (nonatomic, readonly) NSInteger eastEtas;
@property (nonatomic, readonly) NSInteger westEtas;

- (void)loadRoutesAndStops;
- (void)updateData;


@end
