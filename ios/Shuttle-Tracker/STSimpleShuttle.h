//
//  STSimpleShuttle.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 10/20/12.
//  Copyright (c) 2012 Abstracted Sheep. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <MapKit/MapKit.h>

@interface STSimpleShuttle : NSObject <MKAnnotation>

@property (strong, nonatomic) NSNumber *identifier;
@property (strong, nonatomic) NSString *name;
@property (strong, nonatomic) NSString *statusMessage;
@property (strong, nonatomic) NSString *cardinalPoint;
@property (strong, nonatomic) NSDate *updateTime;
@property (strong, nonatomic) NSDictionary *icon;
@property (strong, nonatomic) MKAnnotationView *annotationView;
@property (nonatomic) CGFloat heading;
@property (nonatomic) CGFloat speed;
@property (nonatomic) CLLocationCoordinate2D coordinate;

@property (readonly, nonatomic) NSString *title;

- (void)copyStatusFromShuttle:(STSimpleShuttle *)shuttle;

@end
