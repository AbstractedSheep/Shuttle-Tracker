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

/*
 <name>Student Union</name>
 <name>BARH</name>
 <name>Sunset Terrace</name>
 <name>Beman Lane</name>
 <name>Colonie Apartments</name>
 <name>9th and Sage</name>
 <name>West Hall</name>
 <name>Sage</name>
 <name>Brinsmade Terrace</name>
 <name>Footbridge</name>
 <name>Polytech Apartments</name>
 <name>Blitman Residence Commons</name>
 <name>15th and College</name>
 */

//	Give the name of the stop. Thing about this is, these are hardcoded for the
//	JSON data I am receiving and the RPI shuttles' current routes.
- (NSString *)stopName {
	if ([stopId isEqualToString:@"footbridge"]) {
		return @"Footbridge";
	} else if ([stopId isEqualToString:@"polytech"]) {
		return @"Polytech Apartments";
	} else if ([stopId isEqualToString:@"blitman"]) {
		return @"Blitman Commons";
	} else if ([stopId isEqualToString:@"sage"]) {
		return @"Sage";
	} else if ([stopId isEqualToString:@"9th_sage"]) {
		return @"9th and Sage";
	} else if ([stopId isEqualToString:@"15th_college"]) {
		return @"15th and College";
	} else if ([stopId isEqualToString:@"union"]) {
		return @"Student Union";
	} else if ([stopId isEqualToString:@"west"]) {
		return @"West Hall";
	} else if ([stopId isEqualToString:@"barh"]) {
		return @"BARH";
	} else if ([stopId isEqualToString:@"brinsmade"]) {
		return @"Brinsmade Terrace";
	} else if ([stopId isEqualToString:@"beman"]) {
		return @"Beman Lane";
	} else if ([stopId isEqualToString:@"sunset"]) {
		return @"Sunset Terrace";
	} else if ([stopId isEqualToString:@"colonie"]) {
		return @"Colonie Apartments";
	}
	
	return @"Unknown";
}


@end
