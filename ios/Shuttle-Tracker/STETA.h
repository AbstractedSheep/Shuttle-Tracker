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

@property (nonatomic, retain) NSDate * eta;
@property (nonatomic, retain) STRoute *route;
@property (nonatomic, retain) STShuttle *shuttle;
@property (nonatomic, retain) STStop *stop;

@end
