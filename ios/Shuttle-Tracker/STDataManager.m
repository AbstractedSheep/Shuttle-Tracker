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

@property (nonatomic, strong) NSURL *shuttleJsonUrl;
@property (nonatomic, strong) NSURL *etasJsonUrl;
@property (nonatomic, strong) STJSONParser *routesStopsJsonParser;
@property (nonatomic, strong) STJSONParser *vehiclesJsonParser;
@property (nonatomic, strong) STJSONParser *etasJsonParser;
@property (nonatomic) dispatch_queue_t loadMapInfoJsonQueue;
@property (nonatomic) dispatch_queue_t loadVehicleJsonQueue;
@property (nonatomic) dispatch_queue_t loadEtaJsonQueue;

- (void)loadFromJson;
- (void)routeJsonLoaded;
- (void)updateVehicleData;
- (void)updateSimpleVehicleData;
- (void)updateEtaData;

@end

@implementation STDataManager

- (id)init {
    if ((self = [super init])) {
        self.timeDisplayFormatter = [[NSDateFormatter alloc] init];
        
        NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
        BOOL use24Time = [[defaults objectForKey:@"use24Time"] boolValue];
        
        if (use24Time) {
            [self.timeDisplayFormatter setDateFormat:@"HH:mm"];
        } else {
            [self.timeDisplayFormatter setDateFormat:@"hh:mm a"];
        }
        
        self.routesStopsJsonParser = [[STJSONParser alloc] init];
        self.vehiclesJsonParser = [[STJSONParser alloc] init];
        self.etasJsonParser = [[STJSONParser alloc] init];
        
        self.loadVehicleJsonQueue = NULL;
        self.loadEtaJsonQueue = NULL;
    }
    
    return self;
}


- (void)dealloc {
    if (self.loadMapInfoJsonQueue) {
        dispatch_release(self.loadMapInfoJsonQueue);
    }
    
    if (self.loadVehicleJsonQueue) {
        dispatch_release(self.loadVehicleJsonQueue);
    }
    
    if (self.loadEtaJsonQueue) {
        dispatch_release(self.loadEtaJsonQueue);
    }
    
}


//  Load the routes/stops from JSON asynchronously
- (void)loadRoutesAndStops {
    [self loadFromJson];
}


- (void)loadFromJson {
    if (!self.loadMapInfoJsonQueue) {
        self.loadMapInfoJsonQueue = dispatch_queue_create("com.abstractedsheep.jsonqueue", NULL);
    }
    
    dispatch_async(self.loadMapInfoJsonQueue, ^{
        NSError *theError = nil;
        NSURL *routesStopsUrl = [NSURL URLWithString:kSTRoutesandStopsUrl];
        NSString *jsonString = [NSString stringWithContentsOfURL:routesStopsUrl
                                                        encoding:NSUTF8StringEncoding
                                                           error:&theError];
        
        if (theError) {
            NSLog(@"Error retrieving JSON data: %@", theError);
        } else {
            [self.routesStopsJsonParser performSelectorOnMainThread:@selector(parseRoutesandStopsFromJson:)
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
    //    [self updateVehicleData];
    [self updateSimpleVehicleData];
    //    [self updateEtaData];
}


//  Pull updated vehicle data and posts a notification that it has done so.
//  Note that the notification is not expected to be on the main thread.
- (void)updateVehicleData {
    
    if (!self.loadVehicleJsonQueue) {
        self.loadVehicleJsonQueue = dispatch_queue_create("com.abstractedsheep.jsonqueue", NULL);
    }
    
    dispatch_async(self.loadVehicleJsonQueue, ^{
        NSError *theError = nil;
        //        self.shuttleJsonUrl = [NSURL URLWithString:kSTShuttlesUrl];
        self.shuttleJsonUrl = [NSURL URLWithString:kSTShuttlesBackupUrl];
        NSString *jsonString = [NSString stringWithContentsOfURL:self.shuttleJsonUrl
                                                        encoding:NSUTF8StringEncoding
                                                           error:&theError];
        
        if (theError) {
            NSLog(@"Error retrieving JSON data: %@", theError);
        } else {
            [self.vehiclesJsonParser performSelectorOnMainThread:@selector(parseShuttlesFromJson:)
                                                      withObject:jsonString
                                                   waitUntilDone:YES];
            [[NSNotificationCenter defaultCenter] postNotificationName:kDMVehiclesUpdated
                                                                object:nil
                                                              userInfo:nil];
        }
    });
    
}

- (void)updateSimpleVehicleData {
    if (!self.loadVehicleJsonQueue) {
        self.loadVehicleJsonQueue = dispatch_queue_create("com.abstractedsheep.jsonqueue", NULL);
    }
    
    dispatch_async(self.loadVehicleJsonQueue, ^{
        NSError *theError = nil;
        self.shuttleJsonUrl = [NSURL URLWithString:kSTShuttlesBackupUrl];
        NSString *jsonString = [NSString stringWithContentsOfURL:self.shuttleJsonUrl
                                                        encoding:NSUTF8StringEncoding
                                                           error:&theError];
        
        if (theError) {
            NSLog(@"Error retrieving JSON data: %@", theError);
        } else {
            [self.vehiclesJsonParser parseSimpleShuttlesFromJson:jsonString];
            [[NSNotificationCenter defaultCenter] postNotificationName:kDMSimpleVehiclesUpdated
                                                                object:nil
                                                              userInfo:@{ @"simpleShuttles" : self.vehiclesJsonParser.simpleShuttles }];
        }
    });
}

- (void)updateEtaData {
    if (!self.loadEtaJsonQueue) {
        self.loadEtaJsonQueue = dispatch_queue_create("com.abstractedsheep.jsonqueue", NULL);
    }
    
    dispatch_async(self.loadEtaJsonQueue, ^{
        NSError *theError = nil;
        self.etasJsonUrl = [NSURL URLWithString:kSTNextEtasUrl];
        NSString *jsonString = [NSString stringWithContentsOfURL:self.etasJsonUrl
                                                        encoding:NSUTF8StringEncoding
                                                           error:&theError];
        
        if (theError) {
            NSLog(@"Error retrieving JSON data: %@", theError);
        } else {
            [self.vehiclesJsonParser performSelectorOnMainThread:@selector(parseEtasFromJson:)
                                                      withObject:jsonString
                                                   waitUntilDone:YES];
            [[NSNotificationCenter defaultCenter] postNotificationName:kDMEtasUpdated
                                                                object:nil
                                                              userInfo:nil];
        }
    });
    
}


- (void)setTimeDisplayFormatter:(NSDateFormatter *)newTimeDisplayFormatter {
    _timeDisplayFormatter = newTimeDisplayFormatter;
    
    self.vehiclesJsonParser.timeDisplayFormatter = self.timeDisplayFormatter;
}


- (void)setParserManagedObjectContext:(NSManagedObjectContext *)context {
    self.routesStopsJsonParser.managedObjectContext = context;
    self.vehiclesJsonParser.managedObjectContext = context;
    self.etasJsonParser.managedObjectContext = context;
}

@end
