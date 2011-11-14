//
//  ETA.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 11/14/11.
//  Copyright (c) 2011 Brendon Justin. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class Route, Shuttle, Stop;

@interface ETA : NSManagedObject

@property (nonatomic, retain) NSDate * eta;
@property (nonatomic, retain) Route *route;
@property (nonatomic, retain) Shuttle *shuttle;
@property (nonatomic, retain) Stop *stop;

@end
