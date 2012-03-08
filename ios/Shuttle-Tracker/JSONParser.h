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
	NSDateFormatter *m_timeDisplayFormatter;
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