//
//  STRoute.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 11/14/11.
//  Copyright (c) 2011 Brendon Justin. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class STETA, STFavoriteStop, STShuttle, STStop;

@interface STRoute : NSManagedObject

@property (nonatomic, strong) NSString * color;
@property (nonatomic, strong) NSString * name;
@property (nonatomic, strong) NSNumber * routeId;
@property (nonatomic, strong) NSNumber * width;
@property (nonatomic, strong) NSString * pointList;
@property (nonatomic, strong) NSSet *etas;
@property (nonatomic, strong) NSSet *favorites;
@property (nonatomic, strong) NSSet *shuttles;
@property (nonatomic, strong) NSSet *stops;
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

- (void)addShuttlesObject:(STShuttle *)value;
- (void)removeShuttlesObject:(STShuttle *)value;
- (void)addShuttles:(NSSet *)values;
- (void)removeShuttles:(NSSet *)values;

- (void)addStopsObject:(STStop *)value;
- (void)removeStopsObject:(STStop *)value;
- (void)addStops:(NSSet *)values;
- (void)removeStops:(NSSet *)values;

@end
