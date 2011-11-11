//
//  EtaManager.m
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 2/20/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import "MapPlacemark.h"
#import "DataManager.h"
#import "IASKSettingsReader.h"
#import "DataUrls.h"

#define kRemoveShuttleThreshold		90.0f


@interface DataManager()
- (void)loadFromJson;
- (void)routeJsonLoaded;
- (void)updateVehicleData;
- (void)updateEtaData;

@end

@implementation DataManager

@synthesize timeDisplayFormatter;


- (id)init {
    if ((self = [super init])) {
		timeDisplayFormatter = [[NSDateFormatter alloc] init];
		
		NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
		BOOL use24Time = [[defaults objectForKey:@"use24Time"] boolValue];
		
		if (use24Time) {
			[timeDisplayFormatter setDateFormat:@"HH:mm"];
		} else {
			[timeDisplayFormatter setDateFormat:@"hh:mm a"];
		}
		
		//	Get the favorite stop names array from the app defaults in packed data form
//		NSData *dataForFavoritesArray = [defaults objectForKey:@"favoritesList"];
//		
//		if (dataForFavoritesArray != nil)
//		{
//			//	Create an array from the packed data, and if the array is a valid object,
//			//	set the favorite stops array to that array
//			NSArray *savedFavoritesArray = [NSKeyedUnarchiver unarchiveObjectWithData:dataForFavoritesArray];
//			if (savedFavoritesArray != nil)
//                favoriteStopNames = [[NSMutableArray alloc] initWithArray:savedFavoritesArray];
//			else
//                favoriteStopNames = [[NSMutableArray alloc] init];
//		} else {
//			favoriteStopNames = [[NSMutableArray alloc] init];
//		}
        
        routesStopsJsonParser = [[JSONParser alloc] init];
        vehiclesJsonParser = [[JSONParser alloc] init];
        etasJsonParser = [[JSONParser alloc] init];
		
		loadVehicleJsonQueue = NULL;
		loadEtaJsonQueue = NULL;
    }
    
    return self;
}


- (void)dealloc {
    if (vehiclesJsonParser) {
        [vehiclesJsonParser release];
    }
    
    if (etasJsonParser) {
        [etasJsonParser release];
    }
    
    [shuttleJsonUrl release];
    [etasJsonUrl release];
	
	if (loadVehicleJsonQueue) {
		dispatch_release(loadVehicleJsonQueue);
	}
    
	if (loadEtaJsonQueue) {
		dispatch_release(loadEtaJsonQueue);
	}
	
    [super dealloc];
}


//  Load the routes/stops from JSON asynchronously
- (void)loadRoutesAndStops {
    [self loadFromJson];
}


- (void)loadFromJson {
    NSError *theError = nil;
    NSURL *routesStopsUrl = [NSURL URLWithString:kDMRoutesandStopsUrl];
    NSString *jsonString = [NSString stringWithContentsOfURL:routesStopsUrl 
                                                    encoding:NSUTF8StringEncoding 
                                                       error:&theError];
    
    if (theError) {
        NSLog(@"Error retrieving JSON data: %@", theError);
    } else {
        [routesStopsJsonParser parseRoutesandStopsFromJson:jsonString];
        [[NSNotificationCenter defaultCenter] postNotificationName:kDMRoutesandStopsLoaded object:self];
    }
}


- (void)routeJsonLoaded {
    [[NSNotificationCenter defaultCenter] postNotificationName:kDMRoutesandStopsLoaded object:self];
}


//	Update vehicle positions, ETAs, and any other data that changes frequently.
- (void)updateData {
    [self updateVehicleData];
    [self updateEtaData];
}


//	Pull updated vehicle data and posts a notification that it has done so.
//	Note that the notification is not expected to be on the main thread.
- (void)updateVehicleData {
    
	if (!loadVehicleJsonQueue) {
		loadVehicleJsonQueue = dispatch_queue_create("com.abstractedsheep.jsonqueue", NULL);
	}
    
    dispatch_async(loadVehicleJsonQueue, ^{
        NSError *theError = nil;
        shuttleJsonUrl = [NSURL URLWithString:kDMShuttlesUrl];
        NSString *jsonString = [NSString stringWithContentsOfURL:shuttleJsonUrl 
                                                        encoding:NSUTF8StringEncoding 
                                                           error:&theError];
        
        if (theError) {
            NSLog(@"Error retrieving JSON data: %@", theError);
        } else {
            [vehiclesJsonParser performSelectorOnMainThread:@selector(parseShuttlesFromJson:)
                                                 withObject:jsonString
                                              waitUntilDone:YES];
			[[NSNotificationCenter defaultCenter] postNotificationName:kDMVehiclesUpdated
																object:nil
															  userInfo:nil];
        }
    });
    
}


- (void)updateEtaData {
	if (!loadEtaJsonQueue) {
		loadEtaJsonQueue = dispatch_queue_create("com.abstractedsheep.jsonqueue", NULL);
	}
	
    dispatch_async(loadEtaJsonQueue, ^{
        NSError *theError = nil;
        etasJsonUrl = [NSURL URLWithString:kDMNextEtasUrl];
        NSString *jsonString = [NSString stringWithContentsOfURL:etasJsonUrl 
                                                        encoding:NSUTF8StringEncoding 
                                                           error:&theError];
        
        if (theError) {
            NSLog(@"Error retrieving JSON data: %@", theError);
        } else {
            [vehiclesJsonParser performSelectorOnMainThread:@selector(parseEtasFromJson:)
                                                 withObject:jsonString
                                              waitUntilDone:YES];
			[[NSNotificationCenter defaultCenter] postNotificationName:kDMEtasUpdated
																object:nil
															  userInfo:nil];
        }
    });
    
}


- (void)setTimeDisplayFormatter:(NSDateFormatter *)newTimeDisplayFormatter {
	timeDisplayFormatter = newTimeDisplayFormatter;
	
	vehiclesJsonParser.timeDisplayFormatter = timeDisplayFormatter;
}


- (void)setParserManagedObjectContext:(NSManagedObjectContext *)context {
    routesStopsJsonParser.managedObjectContext = context;
    vehiclesJsonParser.managedObjectContext = context;
    etasJsonParser.managedObjectContext = context;
}

@end
