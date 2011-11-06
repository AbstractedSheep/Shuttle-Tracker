//
//  JSONParser.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 2/12/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <MapKit/MapKit.h>
#import <CoreData/CoreData.h>


@interface JSONParser : NSObject <NSFetchedResultsControllerDelegate> {
	NSDateFormatter *timeDisplayFormatter;
}

@property (nonatomic, retain) NSArray *routes;
@property (nonatomic, retain) NSArray *stops;
@property (nonatomic, retain) NSMutableArray *vehicles;
@property (nonatomic, retain) NSMutableArray *etas;
@property (nonatomic, retain) NSMutableArray *extraEtas;
@property (nonatomic, assign) NSDateFormatter *timeDisplayFormatter;
@property (nonatomic, retain) NSFetchedResultsController *fetchedResultsController;
@property (strong, nonatomic) NSManagedObjectContext *managedObjectContext;

- (BOOL)parseRoutesandStopsFromJson:(NSString *)jsonString;
- (BOOL)parseShuttlesFromJson:(NSString *)jsonString;
- (BOOL)parseEtasFromJson:(NSString *)jsonString;
- (BOOL)parseExtraEtasFromJson:(NSString *)jsonString;


@end


@interface JSONPlacemark : NSObject <MKAnnotation> {
    NSString *name;
    NSString *description;
	NSString *subtitle;
    
    CLLocationCoordinate2D coordinate;
    
    MKAnnotationView *annotationView;
	NSDateFormatter *timeDisplayFormatter;
    
}

@property (nonatomic, retain) NSString *name;
@property (nonatomic, copy) NSString *title;
@property (nonatomic, retain) NSString *description;
@property (nonatomic, copy) NSString *subtitle;
@property (nonatomic) CLLocationCoordinate2D coordinate;
@property (nonatomic, retain) MKAnnotationView *annotationView;
@property (nonatomic, assign) NSDateFormatter *timeDisplayFormatter;


@end


@interface JSONStop : JSONPlacemark {
    
}


@end

@interface JSONVehicle : JSONPlacemark {
    NSDictionary *ETAs;
    int heading;
	NSDate *updateTime;
    int routeNo;
	BOOL routeImageSet;
	BOOL viewNeedsUpdate;

}

@property (nonatomic, retain) NSDictionary *ETAs;
@property (nonatomic) int heading;
@property (nonatomic, retain) NSDate *updateTime;
@property (nonatomic) int routeNo;
@property (nonatomic) BOOL routeImageSet;
@property (nonatomic) BOOL viewNeedsUpdate;

- (void)copyAttributesExceptLocation:(JSONVehicle *)newVehicle;

@end
