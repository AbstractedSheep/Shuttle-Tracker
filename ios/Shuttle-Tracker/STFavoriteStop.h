//
//  STFavoriteStop.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 11/14/11.
//  Copyright (c) 2011 Brendon Justin. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class STRoute, STStop;

@interface STFavoriteStop : NSManagedObject

@property (nonatomic, retain) STRoute *route;
@property (nonatomic, retain) STStop *stop;

@end
