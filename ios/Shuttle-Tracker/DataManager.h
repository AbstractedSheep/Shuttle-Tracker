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
	
	NSArray *routeNames;
	NSArray *routeShortNames;
    
    NSMutableArray *vehicles;
    
    NSArray *etas;
    NSMutableDictionary *soonestEtas;
	NSMutableArray *favoriteStopNames;
	NSMutableArray *favoriteEtas;
	NSMutableDictionary *numberEtas;
	
    BOOL onlySoonestEtas;
	BOOL lockFavorites;
    
	dispatch_queue_t loadVehicleJsonQueue;
	dispatch_queue_t loadEtaJsonQueue;
	
	NSDateFormatter *timeDisplayFormatter;
}

@property (nonatomic, retain) NSArray *routes;
@property (nonatomic, retain) NSArray *stops;
@property (nonatomic, retain) NSMutableArray *vehicles;
@property (nonatomic, retain) NSArray *etas;
@property (nonatomic, retain) NSDictionary *soonestEtas;
@property (nonatomic, retain) NSArray *favoriteEtas;
@property (nonatomic, retain) NSDictionary *numberEtas;
@property (nonatomic, readonly) NSArray *routeNames;
@property (nonatomic, readonly) NSArray *routeShortNames;
@property (nonatomic, readonly) int numberSections;
@property (nonatomic, readonly) NSArray *sectionHeaders;
@property (nonatomic, retain) NSDateFormatter *timeDisplayFormatter;
//	There should be exactly one retain on timeDisplayFormatter, the way
//	that the program is currently set up.


- (void)loadRoutesAndStops;
- (void)updateData;
- (int)numberEtasForSection:(int)sectionNo;
- (NSArray *)etasForSection:(int)sectionNo;
- (void)toggleFavoriteEtaAtIndexPath:(NSIndexPath *)indexPath;
- (void)setEta:(id)eta asFavorite:(BOOL)addFavorite;
- (BOOL)isFavoritesSection:(NSUInteger)section;


@end
