//
//  STJSONParser.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 2/12/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface STJSONParser : NSObject <NSFetchedResultsControllerDelegate>

@property (nonatomic, strong) NSArray *routes;
@property (nonatomic, strong) NSArray *stops;
@property (nonatomic, strong) NSMutableArray *vehicles;
@property (nonatomic, strong) NSDictionary *simpleShuttles;
@property (nonatomic, strong) NSMutableArray *etas;
@property (nonatomic, strong) NSMutableArray *extraEtas;
@property (nonatomic, weak) NSDateFormatter *timeDisplayFormatter;

@property (nonatomic, strong) NSFetchedResultsController *fetchedResultsController;
@property (nonatomic, strong) NSManagedObjectContext *managedObjectContext;

- (BOOL)parseRoutesandStopsFromJson:(NSString *)jsonString;
- (BOOL)parseSimpleShuttlesFromJson:(NSString *)jsonString;
- (BOOL)parseShuttlesFromJson:(NSString *)jsonString;
- (BOOL)parseEtasFromJson:(NSString *)jsonString;
- (BOOL)parseExtraEtasFromJson:(NSString *)jsonString;


@end
