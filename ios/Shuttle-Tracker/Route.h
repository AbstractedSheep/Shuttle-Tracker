//
//  Route.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 10/28/11.
//  Copyright (c) 2011 Naga Softworks, LLC. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class RoutePt, Shuttle, Stop;

@interface Route : NSManagedObject

@property (nonatomic, retain) NSString * color;
@property (nonatomic, retain) NSString * idTag;
@property (nonatomic, retain) NSString * name;
@property (nonatomic, retain) NSNumber * width;
@property (nonatomic, retain) NSSet *stops;
@property (nonatomic, retain) NSSet *points;
@property (nonatomic, retain) NSSet *shuttles;
@end

@interface Route (CoreDataGeneratedAccessors)

- (void)addStopsObject:(Stop *)value;
- (void)removeStopsObject:(Stop *)value;
- (void)addStops:(NSSet *)values;
- (void)removeStops:(NSSet *)values;
- (void)addPointsObject:(RoutePt *)value;
- (void)removePointsObject:(RoutePt *)value;
- (void)addPoints:(NSSet *)values;
- (void)removePoints:(NSSet *)values;
- (void)addShuttlesObject:(Shuttle *)value;
- (void)removeShuttlesObject:(Shuttle *)value;
- (void)addShuttles:(NSSet *)values;
- (void)removeShuttles:(NSSet *)values;
@end
