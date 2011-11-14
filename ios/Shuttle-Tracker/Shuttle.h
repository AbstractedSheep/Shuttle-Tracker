//
//  Shuttle.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 11/13/11.
//  Copyright (c) 2011 Naga Softworks, LLC. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class ETA, Route;

@interface Shuttle : NSManagedObject

@property (nonatomic, retain) NSString * cardinalPoint;
@property (nonatomic, retain) NSNumber * heading;
@property (nonatomic, retain) NSNumber * latitude;
@property (nonatomic, retain) NSNumber * longitude;
@property (nonatomic, retain) NSString * name;
@property (nonatomic, retain) NSNumber * routeId;
@property (nonatomic, retain) NSNumber * speed;
@property (nonatomic, retain) NSDate * updateTime;
@property (nonatomic, retain) NSSet *eta;
@property (nonatomic, retain) Route *route;
@end

@interface Shuttle (CoreDataGeneratedAccessors)

- (void)addEtaObject:(ETA *)value;
- (void)removeEtaObject:(ETA *)value;
- (void)addEta:(NSSet *)values;
- (void)removeEta:(NSSet *)values;

@end
