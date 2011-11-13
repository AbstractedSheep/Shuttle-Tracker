//
//  FavoriteStop.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 11/12/11.
//  Copyright (c) 2011 Naga Softworks, LLC. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>
#import "Stop.h"

@class Route, Stop;

@interface FavoriteStop : Stop

@property (nonatomic, retain) Route *route;
@property (nonatomic, retain) Stop *stop;

@end
