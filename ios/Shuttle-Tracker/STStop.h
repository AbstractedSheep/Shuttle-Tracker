//
//  STStop.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 11/14/11.
//  Copyright (c) 2011 Brendon Justin. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class STETA, STFavoriteStop, STRoute;

@interface STStop : NSManagedObject

@property (nonatomic, strong) NSString * idTag;
@property (nonatomic, strong) NSNumber * latitude;
@property (nonatomic, strong) NSNumber * longitude;
@property (nonatomic, strong) NSString * name;
@property (nonatomic, strong) NSString * shortName;
@property (nonatomic, strong) NSNumber * stopNum;
@property (nonatomic, strong) NSSet *etas;
@property (nonatomic, strong) NSSet *favorites;
@property (nonatomic, strong) NSSet *routes;
@end

@interface STStop (CoreDataGeneratedAccessors)

- (void)addEtasObject:(STETA *)value;
- (void)removeEtasObject:(STETA *)value;
- (void)addEtas:(NSSet *)values;
- (void)removeEtas:(NSSet *)values;

- (void)addFavoritesObject:(STFavoriteStop *)value;
- (void)removeFavoritesObject:(STFavoriteStop *)value;
- (void)addFavorites:(NSSet *)values;
- (void)removeFavorites:(NSSet *)values;

- (void)addRoutesObject:(STRoute *)value;
- (void)removeRoutesObject:(STRoute *)value;
- (void)addRoutes:(NSSet *)values;
- (void)removeRoutes:(NSSet *)values;

@end
