//
//  ETA.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 10/31/11.
//  Copyright (c) 2011 Naga Softworks, LLC. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class Route, Shuttle, Stop;

@interface ETA : NSManagedObject

@property (nonatomic, retain) NSDate * eta;
@property (nonatomic, retain) NSNumber * shuttleId;
@property (nonatomic, retain) NSString * stopId;
@property (nonatomic, retain) NSNumber * routeId;
@property (nonatomic, retain) NSString * stopName;
@property (nonatomic, retain) Shuttle *shuttle;
@property (nonatomic, retain) Stop *stop;
@property (nonatomic, retain) Route *route;

@end
