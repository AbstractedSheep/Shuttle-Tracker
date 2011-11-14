//
//  RoutePt.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 11/14/11.
//  Copyright (c) 2011 Brendon Justin. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class Route;

@interface RoutePt : NSManagedObject

@property (nonatomic, retain) NSNumber * latitude;
@property (nonatomic, retain) NSNumber * longitude;
@property (nonatomic, retain) NSNumber * pointNumber;
@property (nonatomic, retain) Route *route;

@end
