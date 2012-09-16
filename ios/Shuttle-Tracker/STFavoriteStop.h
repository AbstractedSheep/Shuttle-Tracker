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

@property (nonatomic, strong) STRoute *route;
@property (nonatomic, strong) STStop *stop;

@end
