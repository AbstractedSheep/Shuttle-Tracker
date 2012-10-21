//
//  STDataManager.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 2/20/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "STJSONParser.h"

#define kDMRoutesandStopsLoaded             @"kDMRoutesandStopsLoaded"
#define kDMVehiclesUpdated                  @"kDMVehiclesUpdated"
#define kDMSimpleVehiclesUpdated            @"kDMSimpleVehiclesUpdated"
#define kDMEtasUpdated                      @"kDMEtasUpdated"

@class EtaWrapper;

//  Manages all of the routes/stops/shuttles data, as well as application settings.
@interface STDataManager : NSObject

@property (nonatomic, strong) NSDateFormatter *timeDisplayFormatter;
//  DataManager should have the onlyretain on timeDisplayFormatter, the way
//  that the program is set up.


- (void)loadRoutesAndStops;
- (void)updateData;
- (void)setParserManagedObjectContext:(NSManagedObjectContext *)context;


@end
