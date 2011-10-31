//
//  JSONParser.m
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 2/12/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import "JSONParser.h"
#import "NSDictionary_JSONExtensions.h"
#import "DataUrls.h"
#import "EtaWrapper.h"
#import "MapPlacemark.h"
#import "ETA.h"
#import "Route.h"
#import "RoutePt.h"
#import "Stop.h"

@implementation JSONParser

@synthesize routes;
@synthesize stops;
@synthesize vehicles;
@synthesize etas;
@synthesize extraEtas;
@synthesize timeDisplayFormatter;
@synthesize fetchedResultsController = __fetchedResultsController;
@synthesize managedObjectContext = __managedObjectContext;


//  Assume a call to init is for a shuttle JSON parser
- (id)init {
    self = [self initWithUrl:[NSURL URLWithString:kDMShuttlesUrl]];
    
    return self;
}


//  Init and parse JSON from some specified URL
- (id)initWithUrl:(NSURL *)url {
    if ((self = [super init])) {
        jsonUrl = url;
        [jsonUrl retain];
        
        routes = [[NSArray alloc] initWithObjects:nil];
        stops = [[NSArray alloc] initWithObjects:nil];
    }
    
    return self;
}


- (BOOL)parseRoutesandStops {
    NSError *theError = nil;
    NSString *jsonString = [NSString stringWithContentsOfURL:jsonUrl 
													encoding:NSUTF8StringEncoding 
													   error:&theError];
    NSDictionary *jsonDict = nil;
    
    if (theError) {
        NSLog(@"Error retrieving JSON data");
        
        return NO;
    } else {
        if (jsonString) {
            jsonDict = [NSDictionary dictionaryWithJSONString:jsonString error:&theError];
        } else {
            jsonDict = nil;
			
			return NO;
        }
        
        NSMutableArray *mutableRoutes = [[NSMutableArray alloc] init];
        NSMutableArray *mutableStops = [[NSMutableArray alloc] init];
        
		NSAutoreleasePool *smallPool = [[NSAutoreleasePool alloc] init];
        NSDictionary *value;
        NSString *string;
        
        NSDictionary *jsonRoutes = [jsonDict objectForKey:@"routes"];
        
        NSEnumerator *routesEnum = [jsonRoutes objectEnumerator];
		
        while ((value = [routesEnum nextObject])) {
            Route *route = (Route *)[NSEntityDescription insertNewObjectForEntityForName:@"Route" inManagedObjectContext:self.managedObjectContext];
            
            route.routeId = [value objectForKey:@"id"];
            
            NSNumber *number = [value objectForKey:@"width"];
            route.width = number;
            
            string = [value objectForKey:@"name"];
            route.name = string;
            
            NSDictionary *coordsDict = [value objectForKey:@"coords"];
            NSEnumerator *coordsEnum = [coordsDict objectEnumerator];
            NSDictionary *coordsValues;
            
            long ptCount = 0;
            while ((coordsValues = [coordsEnum nextObject])) {
                RoutePt *routePt = (RoutePt *)[NSEntityDescription insertNewObjectForEntityForName:@"RoutePt" inManagedObjectContext:self.managedObjectContext];
                
                string = [coordsValues objectForKey:@"latitude"];
                routePt.latitude = [NSNumber numberWithFloat:[string floatValue]];
                
                string = [coordsValues objectForKey:@"longitude"];
                routePt.longitude = [NSNumber numberWithFloat:[string floatValue] ];
                
                routePt.route = route;
                routePt.pointNumber = [NSNumber numberWithLong:ptCount++];
            }
            
            // Save the context.
            NSError *error = nil;
            if (![self.managedObjectContext save:&error]) {
                /*
                 Replace this implementation with code to handle the error appropriately.
                 
                 abort() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development. 
                 */
                NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
                abort();
            }
        }
        
        [routes release];
        routes = mutableRoutes;
        
        NSDictionary *jsonStops = [jsonDict objectForKey:@"stops"];
        
        NSEnumerator *stopsEnum = [jsonStops objectEnumerator];
        
        while ((value = [stopsEnum nextObject])) {
            Stop *stop = (Stop *)[NSEntityDescription insertNewObjectForEntityForName:@"Stop" inManagedObjectContext:self.managedObjectContext];
            
            string = [value objectForKey:@"latitude"];
            stop.latitude = [NSNumber numberWithFloat:[string floatValue]];
            
            string = [value objectForKey:@"longitude"];
            stop.longitude = [NSNumber numberWithFloat:[string floatValue]];
            
            string = [value objectForKey:@"name"];
            stop.name = string;
			
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
            
//            NSDictionary *routesDict = [value objectForKey:@"routes"];
//            NSEnumerator *routesEnum = [routesDict objectEnumerator];
//            NSDictionary *routeValues;
//            
//            NSMutableArray *tempRouteIds = [[NSMutableArray alloc] init];
//            NSMutableArray *tempRouteNames = [[NSMutableArray alloc] init];
//            
//            NSNumber *number;
//            
//            //  Associate the stop with its routes
//            while ((routeValues = [routesEnum nextObject])) {
//                number = [routeValues objectForKey:@"id"];
//                [tempRouteIds addObject:number];
//                
//                string = [routeValues objectForKey:@"name"];
//                [tempRouteNames addObject:string];
//            }
//            
//            NSEntityDescription *entityDescription = [NSEntityDescription
//                                                      entityForName:@"Route" inManagedObjectContext:self.managedObjectContext];
//            NSFetchRequest *request = [[[NSFetchRequest alloc] init] autorelease];
//            [request setEntity:entityDescription];
//            
//            // Set example predicate and sort orderings...
//            NSPredicate *predicate = [NSPredicate predicateWithFormat:
//                                      @"(routeId LIKE[c] '%d')", stopId];
//            [request setPredicate:predicate];
//            
//            NSSortDescriptor *sortDescriptor = [[NSSortDescriptor alloc]
//                                                initWithKey:@"firstName" ascending:YES];
//            [request setSortDescriptors:[NSArray arrayWithObject:sortDescriptor]];
//            [sortDescriptor release];
//            
//            NSError *error = nil;
//            NSArray *array = [moc executeFetchRequest:request error:&error];
//            if (array == nil)
//            {
//                // Deal with error...
//            }
        }
        
        [stops release];
        stops = mutableStops;
		
		[smallPool release];
        
        return YES;
    }
    
    return NO;
}


//  Parse the shuttle data we will get for the shuttle positions
//  Note: parseShuttles and parseEtas are very similar
- (BOOL)parseShuttles {
    NSError *theError = nil;
    NSString *jsonString = [NSString stringWithContentsOfURL:jsonUrl 
													encoding:NSUTF8StringEncoding 
													   error:&theError];
    NSDictionary *jsonDict = nil;
    
    [vehicles release];
    
    vehicles = [[NSMutableArray alloc] init];
    
    if (theError) {
        NSLog(@"Error retrieving JSON data");
        
        return NO;
    } else {
        if (jsonString && ![jsonString isEqualToString:@"null"]) {
            jsonDict = [NSDictionary dictionaryWithJSONString:jsonString error:&theError];
        } else {
            jsonDict = nil;
			
			return NO;
        }
        
		NSAutoreleasePool *smallPool = [[NSAutoreleasePool alloc] init];
		
        //  Each dictionary corresponds to one set of curly braces ({ and })
        for (NSDictionary *dict in jsonDict) {
            JSONVehicle *vehicle = [[JSONVehicle alloc] init];
            
            CLLocationCoordinate2D coordinate = CLLocationCoordinate2DMake(0.0f, 0.0f);
            
            //  Set the vehicle properties to the corresponding JSON values
            for (NSString *string in dict) {
                if ([string isEqualToString:@"name"]) {
                    vehicle.name = [dict objectForKey:string];
                } else if ([string isEqualToString:@"latitude"]) {
                    coordinate.latitude = [[dict objectForKey:string] floatValue];
                } else if ([string isEqualToString:@"longitude"]) {
                    coordinate.longitude = [[dict objectForKey:string] floatValue];
                } else if ([string isEqualToString:@"heading"]) {
                    vehicle.heading = [[dict objectForKey:string] intValue];
                } else if ([string isEqualToString:@"update_time"]) {
					NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
					[dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
					
					vehicle.updateTime = [dateFormatter dateFromString:[dict objectForKey:string]];
					[dateFormatter release];
				} else if ([string isEqualToString:@"route_id"]) {
					vehicle.routeNo = [[dict objectForKey:string] intValue];
				}
            }
            
            //  Set the coordinate of the vehicle after both the latitude and longitude are set
            vehicle.coordinate = coordinate;
			
			vehicle.timeDisplayFormatter = timeDisplayFormatter;
            
            [vehicles addObject:vehicle];
            [vehicle release];
        }
		
		[smallPool release];
        
        return YES;
    }
    
    return NO;
}


//  Parse the upcoming ETAs we will get for the currently running shuttles
//  Note: parseShuttles and parseEtas are very similar
- (BOOL)parseEtas {
    NSError *theError = nil;
    NSString *jsonString = [NSString stringWithContentsOfURL:jsonUrl 
													encoding:NSUTF8StringEncoding 
													   error:&theError];
    NSDictionary *jsonDict = nil;
    
    [etas release];
    
    etas = [[NSMutableArray alloc] init];
    
    if (theError) {
        NSLog(@"Error retrieving JSON data");
        
        return NO;
    } else {
        if (jsonString  && ![jsonString isEqualToString:@"null"]) {
            jsonDict = [NSDictionary dictionaryWithJSONString:jsonString error:&theError];
        } else {
            jsonDict = nil;
			
			return NO;
        }
		
		if (theError) {
			NSLog(@"Error creating JSON data dictionary from string: %@", jsonString);
			
			return NO;
		}
        
		if (!jsonDict || [jsonDict isKindOfClass:[NSNull class]]) {
//			NSLog(@"Error, no jsonDict created.");
			
			return NO;
		}
        
		NSAutoreleasePool *smallPool = [[NSAutoreleasePool alloc] init];
		
        //  Each dictionary corresponds to one set of curly braces ({ and })
        for (NSDictionary *dict in jsonDict) {
            ETA *eta = (ETA *)[NSEntityDescription insertNewObjectForEntityForName:@"ETA" inManagedObjectContext:self.managedObjectContext];
            
            //  Set the eta properties to the corresponding JSON values
            for (NSString *string in dict) {
                if ([string isEqualToString:@"shuttle_id"]) {
                    eta.shuttleId = [dict objectForKey:string];
                } else if ([string isEqualToString:@"stop_id"]) {
                    eta.stopId = [dict objectForKey:string];
                    
                    //  TODO: set which DB stop it corresponds to
                } else if ([string isEqualToString:@"eta"]) {
                    eta.eta = [NSDate dateWithTimeIntervalSinceNow:[[dict objectForKey:string] 
																	floatValue]/1000.0f];
                } else if ([string isEqualToString:@"route"]) {
                    eta.routeId = [NSNumber numberWithInt:[[dict objectForKey:string] intValue]];
                    
                    //  TODO: set which DB route it corresponds to
                } else if ([string isEqualToString:@"name"]) {
					eta.stopName = [dict objectForKey:string];
				}
            }
            
            //  Find the corresponding stop
            NSEntityDescription *entityDescription = [NSEntityDescription
                                                      entityForName:@"Stop" inManagedObjectContext:self.managedObjectContext];
            NSFetchRequest *request = [[[NSFetchRequest alloc] init] autorelease];
            [request setEntity:entityDescription];
            
            // Set example predicate and sort orderings...
            NSPredicate *predicate = [NSPredicate predicateWithFormat:
                                      @"(stopId == '%d')", eta.stopId];
            [request setPredicate:predicate];
            
            NSSortDescriptor *sortDescriptor = [[NSSortDescriptor alloc]
                                                initWithKey:@"firstName" ascending:YES];
            [request setSortDescriptors:[NSArray arrayWithObject:sortDescriptor]];
            [sortDescriptor release];
            
            NSError *error = nil;
            NSArray *array = [self.managedObjectContext executeFetchRequest:request error:&error];
            if (array == nil)
            {
                // Deal with error...
            } else {
                eta.stop = [array objectAtIndex:0];
            }
            
            // Save the context.
            if (![self.managedObjectContext save:&error]) {
                /*
                 Replace this implementation with code to handle the error appropriately.
                 
                 abort() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development. 
                 */
                NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
                abort();
            }
        }
		
		[smallPool release];
        
        return YES;
    }
    
    return NO;
}


- (BOOL)parseExtraEtas {
	NSError *theError = nil;
    NSString *jsonString = [NSString stringWithContentsOfURL:jsonUrl 
													encoding:NSUTF8StringEncoding 
													   error:&theError];
    NSDictionary *jsonDict = nil;
    
    [extraEtas release];
    
    extraEtas = [[NSMutableArray alloc] init];
    
    if (theError) {
        NSLog(@"Error retrieving JSON data");
        
        return NO;
    } else {
        if (jsonString && ![jsonString isEqualToString:@"null"]) {
            jsonDict = [NSDictionary dictionaryWithJSONString:jsonString error:&theError];
        } else {
            jsonDict = nil;
			
			return NO;
        }
		
		if (theError) {
			NSLog(@"Error creating JSON data dictionary from string: %@", jsonString);
			
			return NO;
		}
        
		if (!jsonDict || [jsonDict isKindOfClass:[NSNull class]]) {
			//			NSLog(@"Error, no jsonDict created.");
			
			return NO;
		}
		
		NSDate *now = [NSDate dateWithTimeIntervalSinceNow:0];
		
        //  Each dictionary corresponds to one set of curly braces ({ and })
        for (NSString *string in jsonDict) {
			//  Set the extra ETAs, one for each entry in the ETA array
			if ([string isEqualToString:@"eta"]) {
				for (NSString *etaString in [jsonDict objectForKey:string]) {
					EtaWrapper *eta = [[EtaWrapper alloc] init];
					
					eta.eta = [now dateByAddingTimeInterval:[etaString floatValue]/1000.0f];
					[extraEtas addObject:eta];
					
					[eta release];
				}

			} else if ([string isEqualToString:@"name"]) {
				NSString *stopName = [jsonDict objectForKey:string];
				
				for (EtaWrapper *eta in extraEtas) {
					eta.stopName = stopName;
				}
			}
        }
		
		return YES;
	}
	
	return NO;
}

#pragma mark - Fetched results controller

- (NSFetchedResultsController *)fetchedResultsController
{
    if (__fetchedResultsController != nil) {
        return __fetchedResultsController;
    }
    
    // Set up the fetched results controller.
    // Create the fetch request for the entity.
    NSFetchRequest *fetchRequest = [[[NSFetchRequest alloc] init] autorelease];
    // Edit the entity name as appropriate.
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"ETA" inManagedObjectContext:self.managedObjectContext];
    [fetchRequest setEntity:entity];
    
    // Set the batch size to a suitable number.
    [fetchRequest setFetchBatchSize:20];
    
    // Edit the sort key as appropriate.
    NSSortDescriptor *sortDescriptor = [[[NSSortDescriptor alloc] initWithKey:@"timeStamp" ascending:NO] autorelease];
    NSArray *sortDescriptors = [NSArray arrayWithObjects:sortDescriptor, nil];
    
    [fetchRequest setSortDescriptors:sortDescriptors];
    
    // Edit the section name key path and cache name if appropriate.
    // nil for section name key path means "no sections".
    NSFetchedResultsController *aFetchedResultsController = [[[NSFetchedResultsController alloc] initWithFetchRequest:fetchRequest managedObjectContext:self.managedObjectContext sectionNameKeyPath:nil cacheName:@"Master"] autorelease];
    aFetchedResultsController.delegate = self;
    self.fetchedResultsController = aFetchedResultsController;
    
	NSError *error = nil;
	if (![self.fetchedResultsController performFetch:&error]) {
	    /*
	     Replace this implementation with code to handle the error appropriately.
         
	     abort() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development. 
	     */
	    NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
	    abort();
	}
    
    return __fetchedResultsController;
}


- (void)insertNewObject
{
    // Create a new instance of the entity managed by the fetched results controller.
    NSManagedObjectContext *context = [self.fetchedResultsController managedObjectContext];
    NSEntityDescription *entity = [[self.fetchedResultsController fetchRequest] entity];
    NSManagedObject *newManagedObject = [NSEntityDescription insertNewObjectForEntityForName:[entity name] inManagedObjectContext:context];
    
    // If appropriate, configure the new managed object.
    // Normally you should use accessor methods, but using KVC here avoids the need to add a custom class to the template.
    [newManagedObject setValue:[NSDate date] forKey:@"timeStamp"];
    
    // Save the context.
    NSError *error = nil;
    if (![context save:&error]) {
        /*
         Replace this implementation with code to handle the error appropriately.
         
         abort() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development. 
         */
        NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
        abort();
    }
}


- (void)dealloc {
    [routes release];
    [stops release];
    
    [super dealloc];
    [etas release];
    [vehicles release];
    [jsonUrl release];
}

@end


@implementation JSONPlacemark

@synthesize name;
@synthesize description;
@synthesize title;
@synthesize subtitle;
@synthesize coordinate;
@synthesize annotationView;
@synthesize timeDisplayFormatter;


- (id)init {
    if ((self = [super init])) {
        name = nil;
        description = nil;
        
        annotationView = nil;
    }
    
    return self;
}


//  Title is the main line of text displayed in the callout of an MKAnnotation
- (NSString *)title {
	return name;
}


//	Description is an internal only thing now.  It used to be used for the subtitle
//	as well, but if that behavior is desired, add it in a subclass' implementation
- (void)setDescription:(NSString *)newDescription {
	description = newDescription;
	[description retain];
}


@end

@implementation JSONStop


- (id)init {
    if ((self = [super init])) {
        name = nil;
        description = nil;
        
        annotationView = nil;
    }
    
    return self;
}


@end


//	Overrides init, title, and subtitle
@implementation JSONVehicle

@synthesize ETAs;
@synthesize heading;
@synthesize updateTime;
@synthesize routeNo;
@synthesize routeImageSet;
@synthesize viewNeedsUpdate;


- (id)init {
    if ((self = [super init])) {
        name = nil;
        description = nil;
        ETAs = nil;
        annotationView = nil;
		updateTime = nil;
		routeImageSet = NO;
		viewNeedsUpdate = YES;
        
        heading = 0;
		routeNo = 0;
    }

    return self;
}


- (void)dealloc {
	[name release];
	[description release];
	
	if (ETAs) {
		[ETAs release];
	}
	
	[updateTime release];
	[super dealloc];
}

//	Update the attributes of the current vehicle, usually for the same vehicle 
//	in a subsequent data update.
//	Note that this does not get a reference to the new vehicles display formatter.
- (void)copyAttributesExceptLocation:(JSONVehicle *)newVehicle {
	self.name = newVehicle.name;
	self.description = newVehicle.description;
	self.ETAs = newVehicle.ETAs;
	self.updateTime = newVehicle.updateTime;
	
	self.heading = newVehicle.heading;
	
	if (self.routeNo != newVehicle.routeNo) {
		self.routeNo = newVehicle.routeNo;
		self.viewNeedsUpdate = YES;
	}
}


- (void)setUpdateTime:(NSDate *)newUpdateTime {
	
	updateTime = newUpdateTime;
	[updateTime retain];
	
	//	Update the vehicle's subtitle here, since it displays the last updated time
	//  Subtitle is the secondary line of text displayed in the callout of an MKAnnotation	
	//	Don't update the subtitle if the displayed text will be the same
	NSString *newSubtitle;
	
	if (timeDisplayFormatter) {	//	If the object got a timeDisplayFormatter, use it.
		newSubtitle = [@"Updated: " stringByAppendingString:[timeDisplayFormatter 
															 stringFromDate:updateTime]];
		
		//	Check to see if the updated subtitle is the same as the existing one.
		//	If it isn't, then update the subtitle
		if (![newSubtitle isEqualToString:[self subtitle]]) {
			self.subtitle = newSubtitle;
		}
		
	} else {	//	If there is no timeDisplayFormatter, just display in 12 hour format
		NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
		[dateFormatter setDateFormat:@"hh:mm a"];
		
		//	Check to see if the updated subtitle is the same as the existing one.
		//	If it isn't, then update the subtitle
		newSubtitle = [@"Updated: " stringByAppendingString:[dateFormatter 
															 stringFromDate:updateTime]];
		
		if (![newSubtitle isEqualToString:self.subtitle]) {
			self.subtitle = newSubtitle;
		}
		
		[dateFormatter release];
	}
}


@end
