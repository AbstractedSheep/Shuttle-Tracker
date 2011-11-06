//
//  FavoriteStop.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 11/6/11.
//  Copyright (c) 2011 Naga Softworks, LLC. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>
#import "Stop.h"


@interface FavoriteStop : Stop

@property (nonatomic, retain) NSNumber * routeId;

@end
