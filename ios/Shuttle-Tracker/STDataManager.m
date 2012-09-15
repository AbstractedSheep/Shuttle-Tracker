//
//  STDataManager.m
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 2/20/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import "STMapPlacemark.h"
#import "STDataManager.h"
#import "STDataUrls.h"

#define kRemoveShuttleThreshold     90.0f


@interface STDataManager()
- (void)loadFromJson;
- (void)routeJsonLoaded;
- (void)updateVehicleData;
- (void)updateEtaData;

@end

@implementation STDataManager

@synthesize timeDisplayFormatter = m_timeDisplayFormatter;


- (id)init {
    if ((self = [super init])) {
        m_timeDisplayFormatter = [[NSDateFormatter alloc] init];

        NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
        BOOL use24Time = [[defaults objectForKey:@"use24Time"] boolValue];

        if (use24Time) {
            [m_timeDisplayFormatter setDateFormat:@"HH:mm"];
        } else {
            [m_timeDisplayFormatter setDateFormat:@"hh:mm a"];
        }
        
        m_routesStopsJsonParser = [[STJSONParser alloc] init];
        m_vehiclesJsonParser = [[STJSONParser alloc] init];
        m_etasJsonParser = [[STJSONParser alloc] init];

        m_loadVehicleJsonQueue = NULL;
        m_loadEtaJsonQueue = NULL;
    }
    
    return self;
}


- (void)dealloc {
    if (m_vehiclesJsonParser) {
        [m_vehiclesJsonParser release];
    }
    
    if (m_etasJsonParser) {
        [m_etasJsonParser release];
    }
    
    [m_shuttleJsonUrl release];
    [m_etasJsonUrl release];

    if (m_loadMapInfoJsonQueue) {
        dispatch_release(m_loadMapInfoJsonQueue);
    }
    
    if (m_loadVehicleJsonQueue) {
        dispatch_release(m_loadVehicleJsonQueue);
    }
    
    if (m_loadEtaJsonQueue) {
        dispatch_release(m_loadEtaJsonQueue);
    }

    [super dealloc];
}


//  Load the routes/stops from JSON asynchronously
- (void)loadRoutesAndStops {
    [self loadFromJson];
}


- (void)loadFromJson {
    if (!m_loadMapInfoJsonQueue) {
        m_loadMapInfoJsonQueue = dispatch_queue_create("com.abstractedsheep.jsonqueue", NULL);
    }
    
    dispatch_async(m_loadMapInfoJsonQueue, ^{
        NSError *theError = nil;
        NSURL *routesStopsUrl = [NSURL URLWithString:kSTRoutesandStopsUrl];
        NSString *jsonString = [NSString stringWithContentsOfURL:routesStopsUrl 
                                                        encoding:NSUTF8StringEncoding 
                                                           error:&theError];
        
        if (theError) {
            NSLog(@"Error retrieving JSON data: %@", theError);
        } else {
            [m_routesStopsJsonParser performSelectorOnMainThread:@selector(parseRoutesandStopsFromJson:)
                                                    withObject:jsonString
                                                 waitUntilDone:YES];
            [self performSelectorOnMainThread:@selector(routeJsonLoaded) withObject:nil waitUntilDone:NO];
        }
    });
}


- (void)routeJsonLoaded {
    [[NSNotificationCenter defaultCenter] postNotificationName:kDMRoutesandStopsLoaded object:self];
}


//  Update vehicle positions, ETAs, and any other data that changes frequently.
- (void)updateData {
    [self updateVehicleData];
//    [self updateEtaData];
}


//  Pull updated vehicle data and posts a notification that it has done so.
//  Note that the notification is not expected to be on the main thread.
- (void)updateVehicleData {
    
    if (!m_loadVehicleJsonQueue) {
        m_loadVehicleJsonQueue = dispatch_queue_create("com.abstractedsheep.jsonqueue", NULL);
    }
    
    dispatch_async(m_loadVehicleJsonQueue, ^{
        NSError *theError = nil;
//        m_shuttleJsonUrl = [NSURL URLWithString:kSTShuttlesUrl];
        m_shuttleJsonUrl = [NSURL URLWithString:kSTShuttlesBackupUrl];
        NSString *jsonString = [NSString stringWithContentsOfURL:m_shuttleJsonUrl
                                                        encoding:NSUTF8StringEncoding 
                                                           error:&theError];
        
        if (theError) {
            NSLog(@"Error retrieving JSON data: %@", theError);
        } else {
            [m_vehiclesJsonParser performSelectorOnMainThread:@selector(parseShuttlesFromJson:)
                                                 withObject:jsonString
                                              waitUntilDone:YES];
            [[NSNotificationCenter defaultCenter] postNotificationName:kDMVehiclesUpdated
                                                                object:nil
                                                              userInfo:nil];
        }
    });
    
}


- (void)updateEtaData {
    if (!m_loadEtaJsonQueue) {
        m_loadEtaJsonQueue = dispatch_queue_create("com.abstractedsheep.jsonqueue", NULL);
    }

    dispatch_async(m_loadEtaJsonQueue, ^{
        NSError *theError = nil;
        m_etasJsonUrl = [NSURL URLWithString:kSTNextEtasUrl];
        NSString *jsonString = [NSString stringWithContentsOfURL:m_etasJsonUrl 
                                                        encoding:NSUTF8StringEncoding 
                                                           error:&theError];
        
        if (theError) {
            NSLog(@"Error retrieving JSON data: %@", theError);
        } else {
            [m_vehiclesJsonParser performSelectorOnMainThread:@selector(parseEtasFromJson:)
                                                 withObject:jsonString
                                              waitUntilDone:YES];
            [[NSNotificationCenter defaultCenter] postNotificationName:kDMEtasUpdated
                                                                object:nil
                                                              userInfo:nil];
        }
    });
    
}


- (void)setTimeDisplayFormatter:(NSDateFormatter *)newTimeDisplayFormatter {
    m_timeDisplayFormatter = newTimeDisplayFormatter;

    m_vehiclesJsonParser.timeDisplayFormatter = m_timeDisplayFormatter;
}


- (void)setParserManagedObjectContext:(NSManagedObjectContext *)context {
    m_routesStopsJsonParser.managedObjectContext = context;
    m_vehiclesJsonParser.managedObjectContext = context;
    m_etasJsonParser.managedObjectContext = context;
}

@end
