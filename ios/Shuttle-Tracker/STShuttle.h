//
//  STShuttle.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 11/14/11.
//  Copyright (c) 2011 Brendon Justin. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class STETA, STRoute;

@interface STShuttle : NSManagedObject

@property (nonatomic, strong) NSNumber * heading;
@property (nonatomic, strong) NSNumber * latitude;
@property (nonatomic, strong) NSNumber * longitude;
@property (nonatomic, strong) NSString * name;
@property (nonatomic, strong) NSNumber * speed;
@property (nonatomic, strong) NSDate * updateTime;
@property (nonatomic, strong) NSNumber * routeId;
@property (nonatomic, strong) NSSet *eta;
@property (nonatomic, strong) STRoute *route;
@end

@interface STShuttle (CoreDataGeneratedAccessors)

- (void)addEtaObject:(STETA *)value;
- (void)removeEtaObject:(STETA *)value;
- (void)addEta:(NSSet *)values;
- (void)removeEta:(NSSet *)values;

@end
