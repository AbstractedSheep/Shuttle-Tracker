//
//  RoutePt.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 10/28/11.
//  Copyright (c) 2011 Naga Softworks, LLC. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class Route;

@interface RoutePt : NSManagedObject

@property (nonatomic, retain) NSNumber * locationX;
@property (nonatomic, retain) NSNumber * locationY;
@property (nonatomic, retain) NSNumber * pointNumber;
@property (nonatomic, retain) Route *route;

@end
