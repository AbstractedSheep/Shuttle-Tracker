//
//  EtaManager.m
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 2/20/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import "DataManager.h"


@interface DataManager()

- (void)routeKmlLoaded;
- (void)updateVehicleData;
- (void)vehicleJSONRefresh;

@end

@implementation DataManager


@synthesize routes;
@synthesize stops;
@synthesize vehicles;
@synthesize ETAs;

- (id)init {
    if ((self = [super init])) {
        vehicleUpdateTimer = nil;
        
        //  shuttleJSONUrl = [NSURL URLWithString:@"http://nagasoftworks.com/ShuttleTracker/shuttleOutputData.txt"];
        shuttleJSONUrl = [NSURL URLWithString:@"http://www.abstractedsheep.com/~ashulgach/data_service.php?action=get_shuttle_positions"];
        vehiclesJSONParser = [[JSONParser alloc] initWithUrl:shuttleJSONUrl];
        
        //vehicleUpdateTimer = [NSTimer scheduledTimerWithTimeInterval:5.0f target:self selector:@selector(updateVehicleData) userInfo:nil repeats:YES];
        
    }
    
    return self;
}

- (void)dealloc {
    if (routeKmlParser) {
        [routeKmlParser release];
    }
    
    if (vehicleUpdateTimer) {
        [vehicleUpdateTimer invalidate];
    }
    
    [super dealloc];
}

- (void)routeKmlLoaded {
    [routeKmlParser parse];
    
    routes = [routeKmlParser routes];
    [routes retain];
    
    stops = [routeKmlParser stops];
    [stops retain];
    
    for (KMLRoute *route in routes) {
        [self performSelectorOnMainThread:@selector(addRoute:) withObject:route waitUntilDone:YES];
    }
    
    for (KMLStop *stop in stops) {
        [self performSelectorOnMainThread:@selector(addStop:) withObject:stop waitUntilDone:YES];
    }
    
}

- (void)updateVehicleData {
    
    dispatch_queue_t loadVehicleJsonQueue = dispatch_queue_create("com.abstractedsheep.jsonqueue", NULL);
    dispatch_async(loadVehicleJsonQueue, ^{
        if ([vehiclesJSONParser parse]) {
            [self performSelectorOnMainThread:@selector(vehicleJSONRefresh) withObject:nil waitUntilDone:YES];
        }
    });
    
}

- (void)vehicleJSONRefresh {
    BOOL alreadyAdded = NO;
    
    for (JSONVehicle *newVehicle in vehiclesJSONParser.vehicles) {
        for (JSONVehicle *existingVehicle in vehicles) {
            if ([existingVehicle.name isEqualToString:newVehicle.name]) {
                [UIView animateWithDuration:0.5 animations:^{
                    [existingVehicle setCoordinate:newVehicle.coordinate];
                }];
                
                alreadyAdded = YES;
            }
        }
        
        if (!alreadyAdded) {
            [vehicles addObject:newVehicle];
        }
    }
}


@end
