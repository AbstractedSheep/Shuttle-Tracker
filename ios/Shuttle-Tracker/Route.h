//
//  Route.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 10/31/11.
//  Copyright (c) 2011 Naga Softworks, LLC. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class ETA, RoutePt, Shuttle, Stop;

@interface Route : NSManagedObject

@property (nonatomic, retain) NSString * color;
@property (nonatomic, retain) NSString * idTag;
@property (nonatomic, retain) NSString * name;
@property (nonatomic, retain) NSNumber * width;
@property (nonatomic, retain) NSSet *points;
@property (nonatomic, retain) NSSet *shuttles;
@property (nonatomic, retain) NSSet *stops;
@property (nonatomic, retain) ETA *etas;
@end

@interface Route (CoreDataGeneratedAccessors)

- (void)addPointsObject:(RoutePt *)value;
- (void)removePointsObject:(RoutePt *)value;
- (void)addPoints:(NSSet *)values;
- (void)removePoints:(NSSet *)values;

- (void)addShuttlesObject:(Shuttle *)value;
- (void)removeShuttlesObject:(Shuttle *)value;
- (void)addShuttles:(NSSet *)values;
- (void)removeShuttles:(NSSet *)values;

- (void)addStopsObject:(Stop *)value;
- (void)removeStopsObject:(Stop *)value;
- (void)addStops:(NSSet *)values;
- (void)removeStops:(NSSet *)values;

@end
