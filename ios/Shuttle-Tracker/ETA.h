//
//  ETA.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 10/28/11.
//  Copyright (c) 2011 Naga Softworks, LLC. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class Shuttle, Stop;

@interface ETA : NSManagedObject

@property (nonatomic, retain) NSNumber * route;
@property (nonatomic, retain) NSNumber * shuttleId;
@property (nonatomic, retain) NSString * stopId;
@property (nonatomic, retain) NSDate * eta;
@property (nonatomic, retain) Shuttle *shuttle;
@property (nonatomic, retain) Stop *stop;

@end
