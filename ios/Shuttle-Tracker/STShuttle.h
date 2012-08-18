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

@property (nonatomic, retain) NSNumber * heading;
@property (nonatomic, retain) NSNumber * latitude;
@property (nonatomic, retain) NSNumber * longitude;
@property (nonatomic, retain) NSString * name;
@property (nonatomic, retain) NSNumber * speed;
@property (nonatomic, retain) NSDate * updateTime;
@property (nonatomic, retain) NSNumber * routeId;
@property (nonatomic, retain) NSSet *eta;
@property (nonatomic, retain) STRoute *route;
@end

@interface STShuttle (CoreDataGeneratedAccessors)

- (void)addEtaObject:(STETA *)value;
- (void)removeEtaObject:(STETA *)value;
- (void)addEta:(NSSet *)values;
- (void)removeEta:(NSSet *)values;

@end
