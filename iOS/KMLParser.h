//
//  KMLParser.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 1/29/11.
//  Copyright 2011 Naga Softworks, LLC. All rights reserved.
//

#import <Foundation/Foundation.h>

@class KMLStyle, KMLPlacemark;

@interface KMLParser : NSObject {
	NSMutableArray *styles;
	NSMutableArray *placemarks;
}

@property (nonatomic, readonly) NSMutableArray *styles;
@property (nonatomic, readonly) NSMutableArray *placemarks;

@end
