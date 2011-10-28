//
//  Stop.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 10/28/11.
//  Copyright (c) 2011 Naga Softworks, LLC. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class ETA, Route;

@interface Stop : NSManagedObject

@property (nonatomic, retain) NSString * name;
@property (nonatomic, retain) NSString * shortName;
@property (nonatomic, retain) NSString * idTag;
@property (nonatomic, retain) NSSet *routes;
@property (nonatomic, retain) NSSet *etas;
@end

@interface Stop (CoreDataGeneratedAccessors)

- (void)addRoutesObject:(Route *)value;
- (void)removeRoutesObject:(Route *)value;
- (void)addRoutes:(NSSet *)values;
- (void)removeRoutes:(NSSet *)values;
- (void)addEtasObject:(ETA *)value;
- (void)removeEtasObject:(ETA *)value;
- (void)addEtas:(NSSet *)values;
- (void)removeEtas:(NSSet *)values;
@end
