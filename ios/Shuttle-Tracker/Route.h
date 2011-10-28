//
//  Route.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 10/28/11.
//  Copyright (c) 2011 Naga Softworks, LLC. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface Route : NSManagedObject

@property (nonatomic, retain) NSString * color;
@property (nonatomic, retain) NSNumber * idTag;
@property (nonatomic, retain) NSString * name;
@property (nonatomic, retain) NSNumber * width;
@property (nonatomic, retain) NSData * points;
@property (nonatomic, retain) NSManagedObject *stops;

@end
