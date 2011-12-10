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

@class EtaWrapper;

//	Manages all of the routes/stops/shuttles data, as well as application settings.
@interface DataManager : NSObject {
    NSURL *shuttleJsonUrl;
    NSURL *etasJsonUrl;
    
    JSONParser *routesStopsJsonParser;
    JSONParser *vehiclesJsonParser;
    JSONParser *etasJsonParser;
    
    dispatch_queue_t loadMapInfoJsonQueue;
	dispatch_queue_t loadVehicleJsonQueue;
	dispatch_queue_t loadEtaJsonQueue;
	
	NSDateFormatter *timeDisplayFormatter;
}

@property (nonatomic, retain) NSDateFormatter *timeDisplayFormatter;
//	There should be exactly one retain on timeDisplayFormatter, the way
//	that the program is currently set up.


- (void)loadRoutesAndStops;
- (void)updateData;
- (void)setParserManagedObjectContext:(NSManagedObjectContext *)context;


@end
