//
//  STJSONParser.m
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 2/12/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import "STJSONParser.h"
#import "STDataUrls.h"
#import "STMapPlacemark.h"
#import "STETA.h"
#import "STRoute.h"
#import "STShuttle.h"
#import "STSimpleShuttle.h"
#import "STStop.h"

#import <CoreData/CoreData.h>

@implementation STJSONParser

@synthesize fetchedResultsController = __fetchedResultsController;
@synthesize managedObjectContext = __managedObjectContext;


//  Assume a call to init is for a shuttle JSON parser
- (id)init {
    if ((self = [super init])) {
        self.simpleShuttles = [NSMutableDictionary dictionary];
    }
    
    return self;
}


- (BOOL)parseRoutesandStopsFromJson:(NSString *)jsonString {
    NSError *theError = nil;
    NSData *data = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    NSString *string = nil;
    
    //  Use Apple's JSON parser to read the data
    id jsonDict = [NSJSONSerialization JSONObjectWithData:data
                                                  options:NSJSONReadingMutableLeaves
                                                    error:&theError];
    
    if (theError != nil) {
        NSLog(@"Error: %@", [theError description]);
        return NO;
    }
    
    if (jsonDict && [jsonDict isKindOfClass:[NSDictionary class]]) {
        @autoreleasepool {
            NSDictionary *jsonRoutes = [jsonDict objectForKey:@"routes"];
            for (NSDictionary *value in jsonRoutes) {
                STRoute *route = nil;
                
                NSNumber *routeId = [value objectForKey:@"id"];
                
                //  Find the route, if it exists already
                NSEntityDescription *entityDescription = [NSEntityDescription entityForName:NSStringFromClass([STRoute class])
                                                                     inManagedObjectContext:self.managedObjectContext];
                NSFetchRequest *request = [[NSFetchRequest alloc] init];
                [request setEntity:entityDescription];
                
                // Set predicate and sort orderings...
                NSPredicate *predicate = [NSPredicate predicateWithFormat:
                                          @"(routeId == %@)", routeId];
                [request setPredicate:predicate];
                
                NSError *error = nil;
                NSArray *array = [self.managedObjectContext executeFetchRequest:request error:&error];
                if (array == nil)
                {
                    // Deal with error...
                } else if ([array count] > 0) {
                    //  The route already exists
                    route = (STRoute *)[array objectAtIndex:0];
                } else {
                    //  Create a new vehicle with this name
                    route = (STRoute *)[NSEntityDescription insertNewObjectForEntityForName:NSStringFromClass([STRoute class])
                                                                     inManagedObjectContext:self.managedObjectContext];
                    route.routeId = routeId;
                }
                
                if (route) {
                    //  Set the route using the new info
                    NSNumber *number = [value objectForKey:@"width"];
                    route.width = number;
                    
                    string = [value objectForKey:@"name"];
                    route.name = string;
                    
                    string = [value objectForKey:@"color"];
                    route.color = string;
                    
                    NSDictionary *coordsDict = [value objectForKey:@"coords"];
                    
                    NSMutableString *pointsString = [NSMutableString string];
                    for (NSDictionary *coordsValues in coordsDict) {
                        [pointsString appendFormat:@"%@,%@;",
                         [coordsValues objectForKey:@"latitude"],
                         [coordsValues objectForKey:@"longitude"]];
                    }
                    
                    string = [NSString stringWithString:pointsString];
                    if ([string length] > 0) {
                        string = [string substringToIndex:[string length] - 1];
                    }
                    
                    route.pointList = string;
                    
                    // Save the context.
                    error = nil;
                    if (![self.managedObjectContext save:&error]) {
                        /*
                         Replace this implementation with code to handle the error appropriately.
                         
                         abort() causes the application to generate a crash log and terminate.
                         You should not use this function in a shipping application, although
                         it may be useful during development.
                         */
                        NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
                    }
                }
            }
            
            int stopNum = 0;
            NSDictionary *jsonStops = [jsonDict objectForKey:@"stops"];
            for (NSDictionary *value in jsonStops) {
                STStop *stop = nil;
                
                NSString *stopName = [value objectForKey:@"name"];
                
                //  Find the stop, if it exists already
                NSEntityDescription *entityDescription = [NSEntityDescription entityForName:NSStringFromClass([STStop class])
                                                                     inManagedObjectContext:self.managedObjectContext];
                NSFetchRequest *request = [[NSFetchRequest alloc] init];
                [request setEntity:entityDescription];
                
                // Set predicate and sort orderings...
                NSPredicate *predicate = [NSPredicate predicateWithFormat:
                                          @"(name == %@)", stopName];
                [request setPredicate:predicate];
                
                NSError *error = nil;
                NSArray *array = [self.managedObjectContext executeFetchRequest:request error:&error];
                if (array == nil)
                {
                    // Deal with error...
                } else if ([array count] > 0) {
                    //  The ETA for this stop on this route already exists
                    stop = (STStop *)[array objectAtIndex:0];
                } else {
                    //  Create a new vehicle with this name
                    stop = (STStop *)[NSEntityDescription insertNewObjectForEntityForName:NSStringFromClass([STStop class])
                                                                   inManagedObjectContext:self.managedObjectContext];
                    stop.name = stopName;
                }
                
                if (stop) {
                    string = [value objectForKey:@"latitude"];
                    stop.latitude = @([string doubleValue]);
                    
                    string = [value objectForKey:@"longitude"];
                    stop.longitude = @([string doubleValue]);
                    
                    string = stopName;
                    
                    //  Special handling for long stop names.
                    if ([string isEqualToString:@"Blitman Residence Commons"]) {
                        string = @"Blitman Commons";
                    } else if ([string isEqualToString:@"Polytechnic Residence Commons"]) {
                        string = @"Polytech Commons";
                    } else if ([string isEqualToString:@"Troy Building Crosswalk"]) {
                        string = @"Troy Bldg. Crossing";
                    } else if ([string isEqualToString:@"6th Ave. and City Station"]) {
                        string = @"6th Ave. & City Stn";
                    }
                    
                    stop.shortName = string;
                    
                    string = [value objectForKey:@"short_name"];
                    stop.idTag = string;
                    
                    stop.stopNum = @(stopNum++);
                    
                    NSDictionary *routesDict = [value objectForKey:@"routes"];
                    NSMutableArray *tempRouteIds = [[NSMutableArray alloc] init];
                    NSNumber *number;
                    
                    //  Associate the stop with its routes
                    for (NSDictionary *routeValues in routesDict) {
                        number = [routeValues objectForKey:@"id"];
                        [tempRouteIds addObject:number];
                    }
                    
                    entityDescription = [NSEntityDescription entityForName:NSStringFromClass([STRoute class])
                                                    inManagedObjectContext:self.managedObjectContext];
                    request = [[NSFetchRequest alloc] init];
                    [request setEntity:entityDescription];
                    
                    // Set example predicate and sort orderings...
                    predicate = [NSPredicate predicateWithFormat: @"(routeId IN $ROUTEIDLIST)"];
                    [request setPredicate:[predicate predicateWithSubstitutionVariables:@{@"ROUTEIDLIST": tempRouteIds}]];
                    
                    error = nil;
                    array = [self.managedObjectContext executeFetchRequest:request error:&error];
                    if (array == nil)
                    {
                        // Deal with error...
                    } else {
                        stop.routes = [NSSet setWithArray:array];
                    }
                    
                    // Save the context.
                    error = nil;
                    if (![self.managedObjectContext save:&error]) {
                        /*
                         Replace this implementation with code to handle the error appropriately.
                         
                         abort() causes the application to generate a crash log and terminate.
                         You should not use this function in a shipping application, although
                         it may be useful during development.
                         */
                        NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
                    }
                }
            }
            
        }
        
        return YES;
    }
    
    return NO;
}

// parse shuttle data in the format that shuttles.rpi.edu uses as of 10/21/12
- (BOOL)parseSimpleShuttlesFromJson:(NSString *)jsonString
{
    if ([jsonString isEqualToString:@"null"]) {
        return NO;
    }
    
    NSError *theError = nil;
    NSData *data = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    
    //  Use Apple's JSON parser to read the data
    id jsonArray = [NSJSONSerialization JSONObjectWithData:data
                                                   options:NSJSONReadingMutableLeaves
                                                     error:&theError];
    
    if (theError != nil) {
        NSLog(@"Error: %@", [theError description]);
        return NO;
    }
    
    if (jsonArray && [jsonArray isKindOfClass:[NSArray class]]) {
        NSMutableDictionary *shuttlesDict = [self.simpleShuttles mutableCopy];
        
        for (NSDictionary *shuttleDict in jsonArray) {
            NSDictionary *shuttleDictionary = [shuttleDict objectForKey:@"vehicle"];
            NSDictionary *positionDictionary = [shuttleDictionary objectForKey:@"latest_position"];
            NSNumberFormatter *formatter = [[NSNumberFormatter alloc] init];
            NSNumber *identifier = nil;
            CGFloat latitude = 0;
            CGFloat longitude = 0;
            // RFC 3339 date format
            NSString *dateFormat = @"yyyy'-'MM'-'dd'T'HH':'mm':'ss'Z'";
            NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
            STSimpleShuttle *shuttle = nil;
            
            identifier = [shuttleDictionary objectForKey:@"id"];
            
            shuttle = [shuttlesDict objectForKey:identifier];
            if (!shuttle) {
                shuttle = [[STSimpleShuttle alloc] init];
                [shuttlesDict setObject:shuttle forKey:[identifier stringValue]];
            }
            
            shuttle.identifier = identifier;
            shuttle.name = [shuttleDictionary objectForKey:@"name"];
            shuttle.heading = [[positionDictionary objectForKey:@"heading"] floatValue];
            
            formatter.numberStyle = NSNumberFormatterDecimalStyle;
            latitude = [[formatter numberFromString:[positionDictionary objectForKey:@"latitude"]] floatValue];
            longitude = [[formatter numberFromString:[positionDictionary objectForKey:@"longitude"]] floatValue];
            shuttle.coordinate = CLLocationCoordinate2DMake(latitude, longitude);
            
            dateFormatter.dateFormat = dateFormat;
            dateFormatter.timeZone = [NSTimeZone timeZoneForSecondsFromGMT:0];
            shuttle.updateTime = [dateFormatter dateFromString:[positionDictionary objectForKey:@"timestamp"]];
            
            shuttle.cardinalPoint = [positionDictionary objectForKey:@"cardinal_point"];
            shuttle.statusMessage = [positionDictionary objectForKey:@"public_status_msg"];
        }
        
        self.simpleShuttles = [NSDictionary dictionaryWithDictionary:shuttlesDict];
    }
    
    return YES;
}

//  Parse the shuttle data we will get for the shuttle positions
//  Note: parseShuttles and parseEtas are very similar
- (BOOL)parseShuttlesFromJson:(NSString *)jsonString {
    if ([jsonString isEqualToString:@"null"]) {
        return NO;
    }
    
    NSError *theError = nil;
    NSData *data = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    
    //  Use Apple's JSON parser to read the data
    id jsonArray = [NSJSONSerialization JSONObjectWithData:data
                                                   options:NSJSONReadingMutableLeaves
                                                     error:&theError];
    
    if (theError != nil) {
        NSLog(@"Error: %@", [theError description]);
        return NO;
    }
    
    if (jsonArray && [jsonArray isKindOfClass:[NSArray class]]) {
        @autoreleasepool {
            //  Each dictionary corresponds to one set of curly braces ({ and })
            for (NSDictionary *dict in jsonArray) {
                STShuttle *vehicle = nil;
                NSString *vehicleName = [dict objectForKey:@"name"];
                
                //  Find the vehicle, if it exists already
                NSEntityDescription *entityDescription = [NSEntityDescription entityForName:NSStringFromClass([STShuttle class])
                                                                     inManagedObjectContext:self.managedObjectContext];
                NSFetchRequest *request = [[NSFetchRequest alloc] init];
                [request setEntity:entityDescription];
                
                // Set predicate and sort orderings...
                NSPredicate *predicate = [NSPredicate predicateWithFormat:
                                          @"(name == %@)", vehicleName];
                [request setPredicate:predicate];
                
                NSError *error = nil;
                NSArray *array = [self.managedObjectContext executeFetchRequest:request error:&error];
                if (array == nil)
                {
                    // Deal with error...
                } else if ([array count] > 0) {
                    //  The vehicle with name "name" already exists, so use it
                    vehicle = (STShuttle *)[array objectAtIndex:0];
                } else {
                    //  Create a new vehicle with this name
                    vehicle = (STShuttle *)[NSEntityDescription insertNewObjectForEntityForName:NSStringFromClass([STShuttle class])
                                                                         inManagedObjectContext:self.managedObjectContext];
                    vehicle.name = vehicleName;
                }
                
                if (vehicle) {
                    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
                    [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
                    
                    //  Set some defaults in case our data source doesn't have everything
                    vehicle.updateTime = [NSDate date];
                    vehicle.routeId = @-1;
                    
                    //  Set the vehicle properties to the corresponding JSON values
                    for (NSString *string in dict) {
                        if ([string isEqualToString:@"latitude"]) {
                            vehicle.latitude = @([[dict objectForKey:string] doubleValue]);
                        } else if ([string isEqualToString:@"longitude"]) {
                            vehicle.longitude = @([[dict objectForKey:string] doubleValue]);
                        } else if ([string isEqualToString:@"heading"]) {
                            vehicle.heading = @([[dict objectForKey:string] intValue]);
                        } else if ([string isEqualToString:@"speed"]) {
                            vehicle.speed = @([[dict objectForKey:string] intValue]);
                        } else if ([string isEqualToString:@"update_time"]) {
                            vehicle.updateTime = [dateFormatter dateFromString:[dict objectForKey:string]];
                        } else if ([string isEqualToString:@"route_id"]) {
                            vehicle.routeId = @([[dict objectForKey:string] intValue]);
                        }
                    }
                    
                    // Save the context.
                    error = nil;
                    if (![self.managedObjectContext save:&error]) {
                        /*
                         Replace this implementation with code to handle the error appropriately.
                         
                         abort() causes the application to generate a crash log and terminate.
                         You should not use this function in a shipping application, although
                         it may be useful during development.
                         */
                        NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
                    }
                }
            }
            
        }
        
        return YES;
    }
    
    return NO;
}


//  Parse the upcoming ETAs we will get for the currently running shuttles
//  Note: parseShuttles and parseEtas are very similar
- (BOOL)parseEtasFromJson:(NSString *)jsonString {
    if ([jsonString isEqualToString:@"null"]) {
        return NO;
    }
    
    NSError *theError = nil;
    NSData *data = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    
    //  Use Apple's JSON parser to read the data
    id jsonArray = [NSJSONSerialization JSONObjectWithData:data
                                                   options:NSJSONReadingMutableLeaves
                                                     error:&theError];
    
    if (theError != nil) {
        NSLog(@"Error: %@", [theError description]);
        return NO;
    }
    
    if (jsonArray && [jsonArray isKindOfClass:[NSArray class]]) {
        @autoreleasepool {
            
            //  Each dictionary corresponds to one set of curly braces ({ and })
            for (NSDictionary *dict in jsonArray) {
                STETA *eta = nil;
                
                NSString *etaStopId = [dict objectForKey:@"stop_id"];
                NSNumber *etaRouteId = @([[dict objectForKey:@"route"] intValue]);
                
                //  Find the ETA, if it exists already
                NSEntityDescription *entityDescription = [NSEntityDescription entityForName:NSStringFromClass([STETA class])
                                                                     inManagedObjectContext:self.managedObjectContext];
                NSFetchRequest *request = [[NSFetchRequest alloc] init];
                [request setEntity:entityDescription];
                
                // Set predicate and sort orderings...
                NSPredicate *predicate = [NSPredicate predicateWithFormat:
                                          @"(stop.idTag == %@) AND (route.routeId == %@)", etaStopId, etaRouteId];
                [request setPredicate:predicate];
                
                NSSortDescriptor *sortDescriptor = [NSSortDescriptor sortDescriptorWithKey:@"eta" ascending:NO];
                [request setSortDescriptors:@[sortDescriptor]];
                [request setFetchLimit:1];
                
                NSError *error = nil;
                NSArray *array = [self.managedObjectContext executeFetchRequest:request error:&error];
                if (array == nil)
                {
                    // Deal with error...
                } else if ([array count] > 0) {
                    //  The ETA for this stop on this route already exists
                    eta = (STETA *)[array objectAtIndex:0];
                } else {
                    //  Create a new vehicle with this name
                    eta = (STETA *)[NSEntityDescription insertNewObjectForEntityForName:NSStringFromClass([STETA class])
                                                                 inManagedObjectContext:self.managedObjectContext];
                }
                
                if (eta) {
                    //  Set the eta properties to the corresponding JSON values
                    for (NSString *string in dict) {
                        if ([string isEqualToString:@"eta"]) {
                            int etaTime = [[dict objectForKey:string] intValue];
                            eta.eta = [NSDate dateWithTimeIntervalSinceNow:etaTime/1000.0f];
                        }
                    }
                    
                    //  Find the corresponding stop
                    entityDescription = [NSEntityDescription entityForName:NSStringFromClass([STStop class])
                                                    inManagedObjectContext:self.managedObjectContext];
                    request = [[NSFetchRequest alloc] init];
                    [request setEntity:entityDescription];
                    
                    // Set predicate and sort orderings...
                    predicate = [NSPredicate predicateWithFormat:@"(idTag == %@)", etaStopId];
                    [request setPredicate:predicate];
                    [request setFetchLimit:1];
                    
                    error = nil;
                    array = [self.managedObjectContext executeFetchRequest:request error:&error];
                    if (array == nil)
                    {
                        // Deal with error...
                    } else if ([array count] > 0) {
                        eta.stop = [array objectAtIndex:0];
                    } else {
                        //  No stop was found
                    }
                    
                    //  Find the corresponding route
                    entityDescription = [NSEntityDescription entityForName:NSStringFromClass([STRoute class])
                                                    inManagedObjectContext:self.managedObjectContext];
                    request = [[NSFetchRequest alloc] init];
                    [request setEntity:entityDescription];
                    
                    // Set predicate and sort orderings...
                    predicate = [NSPredicate predicateWithFormat:@"(routeId == %@)", etaRouteId];
                    [request setPredicate:predicate];
                    [request setFetchLimit:1];
                    
                    error = nil;
                    array = [self.managedObjectContext executeFetchRequest:request error:&error];
                    if (array == nil)
                    {
                        // Deal with error...
                    } else if ([array count] > 0) {
                        eta.route = [array objectAtIndex:0];
                    } else {
                        //  No route was found
                    }
                    
                    // Save the context.
                    if (![self.managedObjectContext save:&error]) {
                        /*
                         Replace this implementation with code to handle the error appropriately.
                         
                         abort() causes the application to generate a crash log and terminate.
                         You should not use this function in a shipping application, although
                         it may be useful during development.
                         */
                        NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
                    }
                }
            }
            
        }
        
        return YES;
    }
    
    return NO;
}


- (BOOL)parseExtraEtasFromJson:(NSString *)jsonString {
    if ([jsonString isEqualToString:@"null"]) {
        return NO;
    }
    
    NSError *theError = nil;
    NSData *data = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    
    //  Use Apple's JSON parser to read the data
    id jsonDict = [NSJSONSerialization JSONObjectWithData:data
                                                  options:NSJSONReadingMutableLeaves
                                                    error:&theError];
    
    if (theError != nil) {
        NSLog(@"Error: %@", [theError description]);
        return NO;
    }
    
    
    if (jsonDict && [jsonDict isKindOfClass:[NSDictionary class]] &&
        [jsonDict objectForKey:@"eta"] != nil) {
        self.extraEtas = [jsonDict objectForKey:@"eta"];
        
        return YES;
    }
    
    return NO;
}



@end
