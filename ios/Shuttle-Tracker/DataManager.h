//
//  EtaManager.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 2/20/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "JSONParser.h"

#define kDMRoutesandStopsLoaded				@"kDMRoutesandStopsLoaded"
#define kDMVehiclesUpdated					@"kDMVehiclesUpdated"
#define kDMEtasUpdated						@"kDMEtasUpdated"


//	Manages all of the routes/stops/shuttles data, as well as application settings.
@interface DataManager : NSObject {
    NSURL *shuttleJsonUrl;
    NSURL *etasJsonUrl;
    
    JSONParser *routesStopsJsonParser;
    JSONParser *vehiclesJsonParser;
    JSONParser *etasJsonParser;
    
    NSArray *routes;
    NSArray *stops;
    NSMutableDictionary *numberStops;
    
    NSMutableArray *vehicles;
    
    NSArray *etas;
    NSMutableArray *soonestEtas;
	NSMutableDictionary *numberEtas;
    NSInteger eastEtas;
    NSInteger westEtas;
	
    BOOL onlyNextEtas;
    
	dispatch_queue_t loadVehicleJsonQueue;
	dispatch_queue_t loadEtaJsonQueue;
	
	NSDateFormatter *timeDisplayFormatter;
}

@property (nonatomic, retain) NSArray *routes;
@property (nonatomic, retain) NSArray *stops;
@property (nonatomic, retain) NSMutableArray *vehicles;
@property (nonatomic, retain) NSArray *etas;
@property (nonatomic, retain) NSArray *soonestEtas;
@property (nonatomic, retain) NSDictionary *numberEtas;
@property (nonatomic, readonly) NSInteger eastEtas;
@property (nonatomic, readonly) NSInteger westEtas;
@property (nonatomic, readonly) NSArray *routeNames;
@property (nonatomic, readonly) NSArray *routeShortNames;
@property (nonatomic, retain) NSDateFormatter *timeDisplayFormatter;
//	There should be exactly one retain on timeDisplayFormatter, the way that the program
//	is currently set up.


- (void)loadRoutesAndStops;
- (void)updateData;
- (int)numberEtasForRoute:(int)routeNo;
- (void)settingChanged:(NSNotification *)notification;


@end
