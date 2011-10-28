//
//  Shuttle.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 10/28/11.
//  Copyright (c) 2011 Naga Softworks, LLC. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class ETA, Route;

@interface Shuttle : NSManagedObject

@property (nonatomic, retain) NSNumber * heading;
@property (nonatomic, retain) NSNumber * locationX;
@property (nonatomic, retain) NSNumber * locationY;
@property (nonatomic, retain) NSString * name;
@property (nonatomic, retain) NSDate * updateTime;
@property (nonatomic, retain) Route *route;
@property (nonatomic, retain) ETA *eta;

@end
