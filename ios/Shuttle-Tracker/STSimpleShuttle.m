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

- (void)setCoordinate:(CLLocationCoordinate2D)coordinate {
    _coordinate = coordinate;
    
    [self.annotationView setNeedsDisplay];
}

@end
