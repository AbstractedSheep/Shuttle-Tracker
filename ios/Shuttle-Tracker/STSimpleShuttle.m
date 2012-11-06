//
//  STSimpleShuttle.m
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 10/20/12.
//  Copyright (c) 2012 Abstracted Sheep. All rights reserved.
//

#import "STSimpleShuttle.h"

@implementation STSimpleShuttle

- (NSString *)title {
    return self.name;
}

- (void)copyStatusFromShuttle:(STSimpleShuttle *)shuttle
{
    self.statusMessage = shuttle.statusMessage;
    self.cardinalPoint = shuttle.cardinalPoint;
    self.updateTime = shuttle.updateTime;
    self.icon = shuttle.icon;
    self.heading = shuttle.heading;
    self.speed = shuttle.speed;
    self.coordinate = shuttle.coordinate;
}

@end
