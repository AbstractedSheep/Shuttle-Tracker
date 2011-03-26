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

#define kDMVehiclesUpdated					@"kDMVehiclesUpdated"


//	Manages all of the routes/stops/shuttles data, as well as application settings.
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
	
	dispatch_queue_t loadVehicleJsonQueue;
	dispatch_queue_t loadEtaJsonQueue;
	
	NSDateFormatter *timeDisplayFormatter;
}

@property (nonatomic, retain) NSArray *routes;
@property (nonatomic, retain) NSArray *stops;
@property (nonatomic, retain) NSMutableArray *vehicles;
@property (nonatomic, retain) NSArray *etas;
@property (nonatomic, readonly) NSInteger eastEtas;
@property (nonatomic, readonly) NSInteger westEtas;
@property (nonatomic, readonly) NSArray *routeNames;
@property (nonatomic, readonly) NSArray *routeShortNames;
@property (nonatomic, retain) NSDateFormatter *timeDisplayFormatter;
//	There should be exactly one retain on timeDisplayFormatter, the way that the program
//	is currently set up.


- (void)loadRoutesAndStops;
- (void)updateData;
- (void)settingChanged:(NSNotification *)notification;


@end
