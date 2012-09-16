//
//  STETA.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 11/14/11.
//  Copyright (c) 2011 Brendon Justin. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class STRoute, STShuttle, STStop;

@interface STETA : NSManagedObject

@property (nonatomic, strong) NSDate * eta;
@property (nonatomic, strong) STRoute *route;
@property (nonatomic, strong) STShuttle *shuttle;
@property (nonatomic, strong) STStop *stop;

@end
