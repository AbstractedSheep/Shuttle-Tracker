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
    NSURL *shuttleJSONUrl;
    
    KMLParser *routeKmlParser;
    KMLParser *vehiclesKmlParser;
    JSONParser *vehiclesJSONParser;
    
    NSArray *routes;
    NSArray *stops;
    
    NSMutableArray *vehicles;
    
    NSArray *ETAs;
    
     NSTimer *vehicleUpdateTimer;
}

@property (nonatomic, retain) NSArray *routes;
@property (nonatomic, retain) NSArray *stops;
@property (nonatomic, retain) NSMutableArray *vehicles;
@property (nonatomic, retain) NSArray *ETAs;


@end
