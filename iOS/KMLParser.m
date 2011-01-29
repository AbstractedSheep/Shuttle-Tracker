//
//  KMLParser.m
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 1/29/11.
//  Copyright 2011 Naga Softworks, LLC. All rights reserved.
//

#import "KMLParser.h"


//	Generic placemark; note that subclasses should implement some kind of coordinate storage
@interface KMLPlacemark
{
	NSString *name;
	NSString *idTag;
	NSString *description;
}

@property (nonatomic, retain) NSString *name;
@property (nonatomic, retain) NSString *idTag;
@property (nonatomic, retain) NSString *description;

@end

@interface KMLRoute : KMLPlacemark
{
	NSMutableArray *lineString;
}

@property (nonatomic, retain) NSMutableArray *lineString;


@end

@interface KMLPoint : KMLPlacemark
{
	CGPoint point;
}

@property (nonatomic, retain) CGPoint point;


@end

@interface KMLStop : KMLPoint
{
	
}

@end

@interface KMLShuttle : KMLPoint
{
	
}

@end

@implementation KMLParser

@synthesize styles;
@synthesize placemarks;

- (id)init {
	if ((self == [super init])) {
		placemarks = [[NSMutableArray alloc] init];
		styles = [[NSMutableArray alloc] init];
	}
	
	return self;
}

@implementation KMLPlacemark

@synthesize name;
@synthesize idTag;
@synthesize description;


@end

@implementation KMLRoute

@synthesize lineString;


@end

@implementation KMLPoint

@synthesize point;


@end


@implementation KMLStop


@end

@implementation KMLShuttle



@end

