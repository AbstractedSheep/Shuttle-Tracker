//
//  EtaWrapper.m
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 2/20/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import "EtaWrapper.h"


@implementation EtaWrapper

@synthesize shuttleId;
@synthesize stopId;
@synthesize eta;
@synthesize route;
@synthesize stopName;


- (void)encodeWithCoder:(NSCoder *)coder;
{
	[coder encodeObject:stopName forKey:@"stopName"];
    [coder encodeObject:stopId forKey:@"stopId"];
    [coder encodeInteger:route forKey:@"route"];
}

- (id)initWithCoder:(NSCoder *)coder;
{
    if ((self = [[EtaWrapper alloc] init]))
    {
		stopName = [coder decodeObjectForKey:@"stopName"];
		[stopName retain];
		stopId = [coder decodeObjectForKey:@"stopId"];
		[stopId retain];
		route = [coder decodeIntegerForKey:@"route"];
    }
	
    return self;
}


@end
