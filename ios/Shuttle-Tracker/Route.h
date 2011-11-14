//
//  Route.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 11/13/11.
//  Copyright (c) 2011 Naga Softworks, LLC. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class ETA, FavoriteStop, RoutePt, Shuttle, Stop;

@interface Route : NSManagedObject

@property (nonatomic, retain) NSString * color;
@property (nonatomic, retain) NSString * idTag;
@property (nonatomic, retain) NSString * name;
@property (nonatomic, retain) NSNumber * routeId;
@property (nonatomic, retain) NSNumber * width;
@property (nonatomic, retain) NSSet *etas;
@property (nonatomic, retain) NSSet *favorites;
@property (nonatomic, retain) NSSet *points;
@property (nonatomic, retain) NSSet *shuttles;
@property (nonatomic, retain) NSSet *stops;
@end

@interface Route (CoreDataGeneratedAccessors)

- (void)addEtasObject:(ETA *)value;
- (void)removeEtasObject:(ETA *)value;
- (void)addEtas:(NSSet *)values;
- (void)removeEtas:(NSSet *)values;

- (void)addFavoritesObject:(FavoriteStop *)value;
- (void)removeFavoritesObject:(FavoriteStop *)value;
- (void)addFavorites:(NSSet *)values;
- (void)removeFavorites:(NSSet *)values;

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
