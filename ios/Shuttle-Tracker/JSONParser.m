//
//  JSONParser.m
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 2/12/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import "JSONParser.h"
#import "DataUrls.h"
#import "MapPlacemark.h"
#import "ETA.h"
#import "Route.h"
#import "RoutePt.h"
#import "Shuttle.h"
#import "Stop.h"

#import <CoreData/CoreData.h>

@implementation JSONParser

@synthesize routes;
@synthesize stops;
@synthesize vehicles;
@synthesize etas;
@synthesize extraEtas;
@synthesize timeDisplayFormatter = m_timeDisplayFormatter;
@synthesize fetchedResultsController = __fetchedResultsController;
@synthesize managedObjectContext = __managedObjectContext;


//  Assume a call to init is for a shuttle JSON parser
- (id)init {
    if ((self = [super init])) {
        
    }
    
    return self;
}


- (BOOL)parseRoutesandStopsFromJson:(NSString *)jsonString {
    NSError *theError = nil;
    NSData *data = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    NSString *string;
    
    //  Use Apple's JSON parser to read the data
    id jsonDict = [NSJSONSerialization JSONObjectWithData:data 
                                                options:NSJSONReadingMutableLeaves 
                                                  error:&theError];
    
    if (theError != nil) {
        NSLog(@"Error: %@", [theError description]);
        return NO;
    }
    
    if (jsonDict && [jsonDict isKindOfClass:[NSDictionary class]]) {
        NSAutoreleasePool *smallPool = [[NSAutoreleasePool alloc] init];
        
        NSDictionary *jsonRoutes = [jsonDict objectForKey:@"routes"];
        for (NSDictionary *value in jsonRoutes) {
            Route *route = nil;
            
            NSNumber *routeId = [value objectForKey:@"id"];
            
            //  Find the route, if it exists already
            NSEntityDescription *entityDescription = [NSEntityDescription entityForName:@"Route" 
                                                                 inManagedObjectContext:self.managedObjectContext];
            NSFetchRequest *request = [[[NSFetchRequest alloc] init] autorelease];
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
                route = (Route *)[array objectAtIndex:0];
            } else {
                //  Create a new vehicle with this name
                route = (Route *)[NSEntityDescription insertNewObjectForEntityForName:@"Route" 
                                                               inManagedObjectContext:self.managedObjectContext];
                route.routeId = routeId;
            }
            
            if (route) {
                //  Remove existing points on the route first
                NSEntityDescription *entityDescription = [NSEntityDescription entityForName:@"RoutePt"
                                                                     inManagedObjectContext:self.managedObjectContext];
                NSFetchRequest *request = [[[NSFetchRequest alloc] init] autorelease];
                [request setEntity:entityDescription];
                
                // Set predicate and sort orderings...
                NSPredicate *predicate = [NSPredicate predicateWithFormat:@"(route == %@)", route];
                [request setPredicate:predicate];
                
                NSSortDescriptor *sortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"pointNumber" 
                                                                               ascending:YES];
                [request setSortDescriptors:[NSArray arrayWithObject:sortDescriptor]];
                [sortDescriptor release];
                
                NSArray *routePts = [self.managedObjectContext executeFetchRequest:request 
                                                                             error:&error];
                
                if (routePts != nil) {
                    for (RoutePt *pt in routePts) {
                        [self.managedObjectContext deleteObject:pt];
                    }
                }
                
                //  Now set the route using the new info
                NSNumber *number = [value objectForKey:@"width"];
                route.width = number;
                
                string = [value objectForKey:@"name"];
                route.name = string;
                
                string = [value objectForKey:@"color"];
                route.color = string;
                
                NSDictionary *coordsDict = [value objectForKey:@"coords"];
                
                long ptCount = 0;
                for (NSDictionary *coordsValues in coordsDict) {
                    RoutePt *routePt = (RoutePt *)[NSEntityDescription insertNewObjectForEntityForName:@"RoutePt"
                                                                                inManagedObjectContext:self.managedObjectContext];
                    
                    string = [coordsValues objectForKey:@"latitude"];
                    routePt.latitude = [NSNumber numberWithDouble:[string doubleValue]];
                    
                    string = [coordsValues objectForKey:@"longitude"];
                    routePt.longitude = [NSNumber numberWithDouble:[string doubleValue]];
                    
                    [route addPointsObject:routePt];
                    routePt.route = route;
                    routePt.pointNumber = [NSNumber numberWithLong:ptCount++];
                    
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
        
        int stopNum = 0;
        NSDictionary *jsonStops = [jsonDict objectForKey:@"stops"];
        for (NSDictionary *value in jsonStops) {
            Stop *stop = nil;
            
            NSString *stopName = [value objectForKey:@"name"];
            
            //  Find the stop, if it exists already
            NSEntityDescription *entityDescription = [NSEntityDescription entityForName:@"Stop"
                                                                 inManagedObjectContext:self.managedObjectContext];
            NSFetchRequest *request = [[[NSFetchRequest alloc] init] autorelease];
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
                stop = (Stop *)[array objectAtIndex:0];
            } else {
                //  Create a new vehicle with this name
                stop = (Stop *)[NSEntityDescription insertNewObjectForEntityForName:@"Stop"
                                                             inManagedObjectContext:self.managedObjectContext];
                stop.name = stopName;
            }
            
            if (stop) {
                string = [value objectForKey:@"latitude"];
                stop.latitude = [NSNumber numberWithDouble:[string doubleValue]];
                
                string = [value objectForKey:@"longitude"];
                stop.longitude = [NSNumber numberWithDouble:[string doubleValue]];
                
                string = stopName;
                
                //	Special handling for long stop names.
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
                
                stop.stopNum = [NSNumber numberWithInt:stopNum++];
                
                NSDictionary *routesDict = [value objectForKey:@"routes"];
                NSMutableArray *tempRouteIds = [[NSMutableArray alloc] init];
                NSNumber *number;
                
                //  Associate the stop with its routes
                for (NSDictionary *routeValues in routesDict) {
                    number = [routeValues objectForKey:@"id"];
                    [tempRouteIds addObject:number];
                }
                
                entityDescription = [NSEntityDescription entityForName:@"Route"
                                                inManagedObjectContext:self.managedObjectContext];
                request = [[[NSFetchRequest alloc] init] autorelease];
                [request setEntity:entityDescription];
                
                // Set example predicate and sort orderings...
                predicate = [NSPredicate predicateWithFormat: @"(routeId IN $ROUTEIDLIST)"];
                [request setPredicate:[predicate predicateWithSubstitutionVariables:[NSDictionary dictionaryWithObject:tempRouteIds 
                                                                                                                forKey:@"ROUTEIDLIST"]]];
                [tempRouteIds release];
                
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
        
        [smallPool release];
        
        return YES;
    }
    
    return NO;
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
        NSAutoreleasePool *smallPool = [[NSAutoreleasePool alloc] init];
        
        //  Each dictionary corresponds to one set of curly braces ({ and })
        for (NSDictionary *dict in jsonArray) {
            Shuttle *vehicle = nil;
            NSString *vehicleName = [dict objectForKey:@"name"];
            
            //  Find the vehicle, if it exists already
            NSEntityDescription *entityDescription = [NSEntityDescription entityForName:@"Shuttle"
                                                                 inManagedObjectContext:self.managedObjectContext];
            NSFetchRequest *request = [[[NSFetchRequest alloc] init] autorelease];
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
                vehicle = (Shuttle *)[array objectAtIndex:0];
            } else {
                //  Create a new vehicle with this name
                vehicle = (Shuttle *)[NSEntityDescription insertNewObjectForEntityForName:@"Shuttle"
                                                                   inManagedObjectContext:self.managedObjectContext];
                vehicle.name = vehicleName;
            }
            
            if (vehicle) {
                //  Set the vehicle properties to the corresponding JSON values
                for (NSString *string in dict) {
                    if ([string isEqualToString:@"latitude"]) {
                        vehicle.latitude = [NSNumber numberWithDouble:[[dict objectForKey:string] doubleValue]];
                    } else if ([string isEqualToString:@"longitude"]) {
                        vehicle.longitude = [NSNumber numberWithDouble:[[dict objectForKey:string] doubleValue]];
                    } else if ([string isEqualToString:@"heading"]) {
                        vehicle.heading = [NSNumber numberWithInt:[[dict objectForKey:string] intValue]];
                    } else if ([string isEqualToString:@"speed"]) {
                        vehicle.speed = [NSNumber numberWithInt:[[dict objectForKey:string] intValue]];
                    } else if ([string isEqualToString:@"update_time"]) {
                        NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
                        [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
                        
                        vehicle.updateTime = [dateFormatter dateFromString:[dict objectForKey:string]];
                        [dateFormatter release];
                    } else if ([string isEqualToString:@"route_id"]) {
                        vehicle.routeId = [NSNumber numberWithInt:[[dict objectForKey:string] intValue]];
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
        
        [smallPool release];
        
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
    
    if (jsonArray && [jsonArray isKindOfClass:[NSDictionary class]]) {
        NSAutoreleasePool *smallPool = [[NSAutoreleasePool alloc] init];
        
        //  Each dictionary corresponds to one set of curly braces ({ and })
        for (NSDictionary *dict in jsonArray) {
            ETA *eta = nil;
            
            NSString *etaStopId = [dict objectForKey:@"stop_id"];
            NSNumber *etaRouteId = [NSNumber numberWithInt:[[dict objectForKey:@"route"] intValue]];
            
            //  Find the ETA, if it exists already
            NSEntityDescription *entityDescription = [NSEntityDescription entityForName:@"ETA" 
                                                                 inManagedObjectContext:self.managedObjectContext];
            NSFetchRequest *request = [[[NSFetchRequest alloc] init] autorelease];
            [request setEntity:entityDescription];
            
            // Set predicate and sort orderings...
            NSPredicate *predicate = [NSPredicate predicateWithFormat:
                                      @"(stop.idTag == %@) AND (route.routeId == %@)", etaStopId, etaRouteId];
            [request setPredicate:predicate];
            
            NSSortDescriptor *sortDescriptor = [NSSortDescriptor sortDescriptorWithKey:@"eta" ascending:NO];
            [request setSortDescriptors:[NSArray arrayWithObject:sortDescriptor]];
            [request setFetchLimit:1];
            
            NSError *error = nil;
            NSArray *array = [self.managedObjectContext executeFetchRequest:request error:&error];
            if (array == nil)
            {
                // Deal with error...
            } else if ([array count] > 0) {
                //  The ETA for this stop on this route already exists
                eta = (ETA *)[array objectAtIndex:0];
            } else {
                //  Create a new vehicle with this name
                eta = (ETA *)[NSEntityDescription insertNewObjectForEntityForName:@"ETA"
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
                entityDescription = [NSEntityDescription entityForName:@"Stop" 
                                                inManagedObjectContext:self.managedObjectContext];
                request = [[[NSFetchRequest alloc] init] autorelease];
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
                entityDescription = [NSEntityDescription entityForName:@"Route" 
                                                inManagedObjectContext:self.managedObjectContext];
                request = [[[NSFetchRequest alloc] init] autorelease];
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
    
        [smallPool release];
        
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
    
    if (extraEtas) {
        [extraEtas release];
    }
    
    if (jsonDict && [jsonDict isKindOfClass:[NSDictionary class]] &&
        [jsonDict objectForKey:@"eta"] != nil) {
        extraEtas = [jsonDict objectForKey:@"eta"];
        [extraEtas retain];
        
        return YES;
    }
    
    return NO;
}


- (void)dealloc {
    [super dealloc];
}

@end
