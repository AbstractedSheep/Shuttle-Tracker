//
//  STRoute.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 11/14/11.
//  Copyright (c) 2011 Brendon Justin. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class STETA, STFavoriteStop, STRoutePt, STShuttle, STStop;

@interface STRoute : NSManagedObject

@property (nonatomic, retain) NSString * color;
@property (nonatomic, retain) NSString * name;
@property (nonatomic, retain) NSNumber * routeId;
@property (nonatomic, retain) NSNumber * width;
@property (nonatomic, retain) NSSet *etas;
@property (nonatomic, retain) NSSet *favorites;
@property (nonatomic, retain) NSSet *points;
@property (nonatomic, retain) NSSet *shuttles;
@property (nonatomic, retain) NSSet *stops;
@end

@interface STRoute (CoreDataGeneratedAccessors)

- (void)addEtasObject:(STETA *)value;
- (void)removeEtasObject:(STETA *)value;
- (void)addEtas:(NSSet *)values;
- (void)removeEtas:(NSSet *)values;

- (void)addFavoritesObject:(STFavoriteStop *)value;
- (void)removeFavoritesObject:(STFavoriteStop *)value;
- (void)addFavorites:(NSSet *)values;
- (void)removeFavorites:(NSSet *)values;

- (void)addPointsObject:(STRoutePt *)value;
- (void)removePointsObject:(STRoutePt *)value;
- (void)addPoints:(NSSet *)values;
- (void)removePoints:(NSSet *)values;

- (void)addShuttlesObject:(STShuttle *)value;
- (void)removeShuttlesObject:(STShuttle *)value;
- (void)addShuttles:(NSSet *)values;
- (void)removeShuttles:(NSSet *)values;

- (void)addStopsObject:(STStop *)value;
- (void)removeStopsObject:(STStop *)value;
- (void)addStops:(NSSet *)values;
- (void)removeStops:(NSSet *)values;

@end
