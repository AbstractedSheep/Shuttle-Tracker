//
//  EtaManager.m
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 2/20/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import "MapPlacemark.h"
#import "DataManager.h"
#import "EtaWrapper.h"
#import "IASKSettingsReader.h"
#import "DataUrls.h"

#define kRemoveShuttleThreshold		90.0f


@interface DataManager()
- (void)loadFromJson;
- (void)routeJsonLoaded;
- (void)updateVehicleData;
- (void)vehicleJsonRefresh;
- (void)updateEtaData;
- (void)etaJsonRefresh;
- (void)genRouteNames;
- (void)genRouteShortNames;

@end

@implementation DataManager


@synthesize routes;
@synthesize stops;
@synthesize routeNames;
@synthesize routeShortNames;
@synthesize vehicles;
@synthesize etas;
@synthesize soonestEtas;
@synthesize numberEtas;
@synthesize timeDisplayFormatter;


- (id)init {
    if ((self = [super init])) {
        routes = nil;
        stops = nil;
		
		routeNames = [[NSArray alloc] initWithObjects:nil];
		routeShortNames = [[NSArray alloc] initWithObjects:nil];
        
		etas = [[NSArray alloc] initWithObjects:nil];
		soonestEtas = [[NSMutableDictionary alloc] init];
		numberEtas = [[NSMutableDictionary alloc] init];
		
		timeDisplayFormatter = [[NSDateFormatter alloc] init];
		
		NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
		BOOL use24Time = [[defaults objectForKey:@"use24Time"] boolValue];
		
		if (use24Time) {
			[timeDisplayFormatter setDateFormat:@"HH:mm"];
		} else {
			[timeDisplayFormatter setDateFormat:@"hh:mm a"];
		}
        
        onlySoonestEtas = [[defaults objectForKey:@"onlySoonestEtas"] boolValue];
        
        NSURL *routesJsonUrl = [NSURL URLWithString:kDMRoutesandStopsUrl];
        routesStopsJsonParser = [[JSONParser alloc] initWithUrl:routesJsonUrl];
        
        //  shuttleJSONUrl = [NSURL URLWithString:@"http://nagasoftworks.com/ShuttleTracker/shuttleOutputData.txt"];
        shuttleJsonUrl = [NSURL URLWithString:kDMShuttlesUrl];
        vehiclesJsonParser = [[JSONParser alloc] initWithUrl:shuttleJsonUrl];
        
        etasJsonUrl = [NSURL URLWithString:kDMEtasUrl];
        etasJsonParser = [[JSONParser alloc] initWithUrl:etasJsonUrl];
        
        vehicles = [[NSMutableArray alloc] init];
		
		loadVehicleJsonQueue = NULL;
		loadEtaJsonQueue = NULL;
		
		//	Take notice when a setting is changed
		//	Note that this is not the only object that takes notice.
		[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(settingChanged:) name:kIASKAppSettingChanged object:nil];
    }
    
    return self;
}


- (void)dealloc {
    if (vehiclesJsonParser) {
        [vehiclesJsonParser release];
    }
    
    if (etasJsonParser) {
        [etasJsonParser release];
    }
    
    [shuttleJsonUrl release];
    [etasJsonUrl release];
    
    if (routes) {
        [routes release];
    }
    if (stops) {
        [stops release];
    }
    
    [vehicles release];
    
    if (etas) {
        [etas release];
    }
    
    if (soonestEtas) {
        [soonestEtas release];
    }
	
	if (loadVehicleJsonQueue) {
		dispatch_release(loadVehicleJsonQueue);
	}
    
	if (loadEtaJsonQueue) {
		dispatch_release(loadEtaJsonQueue);
	}
	
    [super dealloc];
}


//  Load the routes/stops from JSON asynchronously
- (void)loadRoutesAndStops {
    [self loadFromJson];
}


- (void)loadFromJson {
    dispatch_queue_t loadRoutesQueue = dispatch_queue_create("com.abstractedsheep.routesqueue", NULL);
	dispatch_async(loadRoutesQueue, ^{
        [routesStopsJsonParser parseRoutesandStops];
		[self performSelectorOnMainThread:@selector(routeJsonLoaded) withObject:nil waitUntilDone:NO];
	});
	
	dispatch_release(loadRoutesQueue);
}


- (void)routeJsonLoaded {
    routes = routesStopsJsonParser.routes;
    [routes retain];
    
    stops = routesStopsJsonParser.stops;
    [stops retain];
    
    [[NSNotificationCenter defaultCenter] postNotificationName:kDMRoutesandStopsLoaded object:self];
}


//	Update vehicle positions, ETAs, and any other data that changes frequently.
- (void)updateData {
    [self updateVehicleData];
    [self updateEtaData];
}


//	Pull updated vehicle data and posts a notification that it has done so.
//	Note that the notification is not expected to be on the main thread.
- (void)updateVehicleData {
    
	if (!loadVehicleJsonQueue) {
		loadVehicleJsonQueue = dispatch_queue_create("com.abstractedsheep.jsonqueue", NULL);
	}
    
    dispatch_async(loadVehicleJsonQueue, ^{
        if ([vehiclesJsonParser parseShuttles]) {
            [self performSelectorOnMainThread:@selector(vehicleJsonRefresh) withObject:nil waitUntilDone:YES];
			[[NSNotificationCenter defaultCenter] postNotificationName:kDMVehiclesUpdated
																object:[vehicles copy] 
															  userInfo:[NSDictionary dictionaryWithObject:vehicles forKey:@"vehicles"]];
        }
    });
    
}


- (void)vehicleJsonRefresh {
    BOOL alreadyAdded = NO;
	NSMutableArray *vehiclesToRemove;
    
    for (JSONVehicle *newVehicle in vehiclesJsonParser.vehicles) {
		alreadyAdded = NO;
		
        for (JSONVehicle *existingVehicle in vehicles) {
            if ([existingVehicle.name isEqualToString:newVehicle.name]) {
				//	Since it may have missed the timeDisplayFormatter when the vehicle
				//	was created, set it properly every time the vehicle is updated.
				//	A tiny performance hit for ease of implementation.
				existingVehicle.timeDisplayFormatter = timeDisplayFormatter;

				[existingVehicle copyAttributesExceptLocation:newVehicle];
				
                [UIView animateWithDuration:0.5 animations:^{
                    [existingVehicle setCoordinate:newVehicle.coordinate];
                }];
                
                alreadyAdded = YES;
            }
        }
        
        if (!alreadyAdded) {
            [vehicles addObject:newVehicle];
        }
    }
	
	vehiclesToRemove = [[NSMutableArray alloc] init];
	
	for (JSONVehicle *vehicle in vehicles) {
		//	Set vehicles with old data to be removed.
		if ([vehicle.updateTime timeIntervalSinceNow] < -kRemoveShuttleThreshold) {
			[vehiclesToRemove addObject:vehicle];
		}
	}
	
	//	Remove any vehicles set to be removed.
	for (JSONVehicle *vehicle in vehiclesToRemove) {
		[vehicles removeObject:vehicle];
	}
	
	[vehiclesToRemove release];
}


- (void)updateEtaData {
	if (!loadEtaJsonQueue) {
		loadEtaJsonQueue = dispatch_queue_create("com.abstractedsheep.jsonqueue", NULL);
	}
	
    dispatch_async(loadEtaJsonQueue, ^{
        if ([etasJsonParser parseEtas]) {
            [self performSelectorOnMainThread:@selector(etaJsonRefresh) withObject:nil waitUntilDone:YES];
			[[NSNotificationCenter defaultCenter] postNotificationName:kDMEtasUpdated
																object:self 
															  userInfo:[NSDictionary dictionaryWithObject:etas 
																								   forKey:@"ETAs"]];
        }
    });
    
}


//	Process the ETAs and generate the lists of route names and route short names.
- (void)etaJsonRefresh {
    [etas release];
    etas = [etasJsonParser.etas copy];
    
    [soonestEtas release];
    soonestEtas = [[NSMutableDictionary alloc] init];
    
	[numberEtas release];
	numberEtas = [[NSMutableDictionary alloc] init];
    
    for (EtaWrapper *eta in etas) {
		NSString *routeName = nil;
		NSArray *currentRouteNames = self.routeNames;
		
		//	Ensure that there are at least as many routes as the route ID number.
		if (eta.route <= [currentRouteNames count]) {
			routeName = [currentRouteNames objectAtIndex:eta.route - 1];
		}
		
        if (routeName) {
			NSNumber *routeEtas = [numberEtas objectForKey:routeName];
			
			NSNumber *newNumberEtas;
			
			if (routeEtas != nil) {
				newNumberEtas = [NSNumber numberWithInt:[routeEtas intValue] + 1];
				[numberEtas setObject:newNumberEtas forKey:routeName];
			} else {
				newNumberEtas = [NSNumber numberWithInt:1];
				[numberEtas setObject:newNumberEtas forKey:routeName];
			}
		}
		
		EtaWrapper *oldSoonEta = nil;
        BOOL addThis = YES;
        BOOL soonEtasChanged = NO;
		
		NSMutableArray *routeSoonestEtas = [soonestEtas objectForKey:[NSNumber numberWithInt:eta.route]];
		
		if (routeSoonestEtas) {
			//  Check to see if the current eta is the next one for its associated stop
			for (EtaWrapper *soonEta in routeSoonestEtas) {
				if ([eta.stopName isEqualToString:soonEta.stopName] && eta.stopId == soonEta.stopId) {
					if ([eta.eta timeIntervalSinceDate:soonEta.eta] > 0) {
						addThis = NO;
						break;
					} else {
						oldSoonEta = soonEta;
						break;
					}
				}
			}
			
			if (addThis) {
				[routeSoonestEtas addObject:eta];
				soonEtasChanged = YES;
			}
			
			if (oldSoonEta) {
				[routeSoonestEtas removeObject:oldSoonEta];
				soonEtasChanged = YES;
			}
		} else {
			routeSoonestEtas = [NSMutableArray arrayWithObjects:nil];
			
			[routeSoonestEtas addObject:eta];
			soonEtasChanged = YES;
		}
		
		if (soonEtasChanged) {
			[soonestEtas setObject:routeSoonestEtas forKey:[NSNumber numberWithInt:eta.route]];
		}
    }
    
	/*
    for (EtaWrapper *eta in soonestEtas) {
        for (MapStop *stop in stops) {
            if (NULL) {
                //	None
				//	Eventually, this should set the next ETA for
				//	each stop, for each route that the stop is on.
            }
        }
    }
	 */
	
	[self genRouteNames];
	[self genRouteShortNames];
}


- (int)numberRoutes {
	return [self.routeNames count];
}


//	Iterate through the list of routes, and return a list of the route names
- (void)genRouteNames {
	if (!routes) {
		return;
	}
	
	NSMutableArray *newRouteNames = [[NSMutableArray alloc] init];
	BOOL alreadyCounted;
	
	for (MapRoute *route in routes) {
		alreadyCounted = NO;
		
		for (NSString *existingName in newRouteNames) {
			if ([route.name isEqualToString:existingName]) {
				alreadyCounted = YES;
			}
		}
		
		if (!alreadyCounted) {
			[newRouteNames addObject:route.name];
		}
	}
	
	[routeNames release];
	routeNames = newRouteNames;
}


//	Use the results from genRouteNames, but take only the first word
//	from each of the route names. This is prettier than the full names.
- (void)genRouteShortNames {
	if (!routes) {
		return;
	}
	
	NSMutableArray *newRouteShortNames = [[NSMutableArray alloc] init];
	
	for (NSString *name in routeNames) {
		[newRouteShortNames addObject:[[name componentsSeparatedByString:@" "] objectAtIndex:0]];
	}
	
	[routeShortNames release];
	routeShortNames = newRouteShortNames;
}


- (void)setTimeDisplayFormatter:(NSDateFormatter *)newTimeDisplayFormatter {
	timeDisplayFormatter = newTimeDisplayFormatter;
	
	vehiclesJsonParser.timeDisplayFormatter = timeDisplayFormatter;
}


//	Get the number of etas for the routeNo'th route.
//	routeNo is expected to be 0-indexed in the method call.
- (int)numberEtasForRoute:(int)routeNo {
	if (!routes || routeNo > [routes count]) {
		return 0;
	}
	
	MapRoute *route = [routes objectAtIndex:routeNo];
	
	if (route) {
		NSNumber *noEtas = nil;
        
        if (onlySoonestEtas) {
            NSArray *routeSoonestEtas = [soonestEtas objectForKey:[NSNumber numberWithInt:routeNo + 1]];
			
			if (routeSoonestEtas) {
				noEtas = [NSNumber numberWithInt:[routeSoonestEtas count]];
			}
        } else {
            noEtas = [numberEtas objectForKey:route.name];
        }
        
		return noEtas ? [noEtas intValue] : 0;
	}
	
	return 0;
}


- (NSArray *)etasForRoute:(int)routeNo {
	if (onlySoonestEtas) {
		NSArray *routeSoonestEtas = [soonestEtas objectForKey:[NSNumber numberWithInt:routeNo]];
		
		if (routeSoonestEtas) {
			return routeSoonestEtas;
		} else {
			return [NSArray arrayWithObjects:nil];
		}
	} else {
		NSMutableArray *routeEtas = [[NSMutableArray alloc] init];
		
		//  Search for the correct EtaWrapper based on route (route 1 == section 0, route 2 == section 1)
		for (EtaWrapper *eta in etas) {
			if (eta.route == routeNo) {
				[routeEtas addObject:eta];
			}
		}
		
		return routeEtas;
	}
}

//	Called by InAppSettingsKit whenever a setting is changed in the settings view inside the app.
//	Currently handles the 12/24 hour time toggle and toggling all/only soonest ETAs.
//	Other objects may also do something when a setting is changed.
- (void)settingChanged:(NSNotification *)notification {
	NSDictionary *info = [notification userInfo];
	
	//	Set the date format to 24 hour time if the user has set Use 24 Hour Time to true.
	if ([[notification object] isEqualToString:@"use24Time"]) {
		if ([[info objectForKey:@"use24Time"] boolValue]) {
			[timeDisplayFormatter setDateFormat:@"HH:mm"];
		} else {
			[timeDisplayFormatter setDateFormat:@"hh:mm a"];
		}
	} else if ([[notification object] isEqualToString:@"onlySoonestEtas"]) {
        if ([[info objectForKey:@"onlySoonestEtas"] boolValue]) {
            onlySoonestEtas = YES;
        } else {
            onlySoonestEtas = NO;
        }
    }
}

@end
