//
//  EtaManager.m
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 2/20/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import "DataManager.h"
#import "EtaWrapper.h"
#import "IASKSettingsReader.h"

#define kRemoveShuttleThreshold		90.0f


@interface DataManager()
- (void)loadFromKml;
- (void)routeKmlLoaded;
- (void)updateVehicleData;
- (void)vehicleJsonRefresh;
- (void)updateEtaData;
- (void)etaJsonRefresh;

@end

@implementation DataManager


@synthesize routes;
@synthesize stops;
@synthesize vehicles;
@synthesize etas;
@synthesize numberEtas;
@synthesize eastEtas;
@synthesize westEtas;
@synthesize timeDisplayFormatter;


- (id)init {
    if ((self = [super init])) {
        routes = nil;
        stops = nil;
        
		numberEtas = [[NSMutableDictionary alloc] init];
        eastEtas = 0;
        westEtas = 0;
		
		timeDisplayFormatter = [[NSDateFormatter alloc] init];
		
		NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
		BOOL use24Time = [[defaults objectForKey:@"use24Time"] boolValue];
		
		if (use24Time) {
			[timeDisplayFormatter setDateFormat:@"HH:mm"];
		} else {
			[timeDisplayFormatter setDateFormat:@"hh:mm a"];
		}
        
        //  shuttleJSONUrl = [NSURL URLWithString:@"http://nagasoftworks.com/ShuttleTracker/shuttleOutputData.txt"];
        shuttleJsonUrl = [NSURL URLWithString:@"http://www.abstractedsheep.com/~ashulgach/data_service.php?action=get_shuttle_positions"];
        vehiclesJsonParser = [[JSONParser alloc] initWithUrl:shuttleJsonUrl];
        
        etasJsonUrl = [NSURL URLWithString:@"http://www.abstractedsheep.com/~ashulgach/data_service.php?action=get_all_eta"];
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
    if (routeKmlParser) {
        [routeKmlParser release];
    }
    
    if (vehiclesKmlParser) {
        [vehiclesKmlParser release];
    }
    
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
	
	if (loadVehicleJsonQueue) {
		dispatch_release(loadVehicleJsonQueue);
	}
    
	if (loadEtaJsonQueue) {
		dispatch_release(loadEtaJsonQueue);
	}
	
    [super dealloc];
}

//  Load the routes/stops KML file asynchronously
- (void)loadRoutesAndStops {
	[self loadFromKml];
}

- (void)loadFromKml {
    //  Use the local copy of the routes/stops KML file
    NSURL *routeKmlUrl = [[NSBundle mainBundle] URLForResource:@"netlink" withExtension:@"kml"];
    
    routeKmlParser = [[KMLParser alloc] initWithContentsOfUrl:routeKmlUrl];
	
	dispatch_queue_t loadRoutesQueue = dispatch_queue_create("com.abstractedsheep.routesqueue", NULL);
	dispatch_async(loadRoutesQueue, ^{
        [routeKmlParser parse];
		[self performSelectorOnMainThread:@selector(routeKmlLoaded) withObject:nil waitUntilDone:NO];
	});
	
	dispatch_release(loadRoutesQueue);
}

//  TODO: Remove this or adjust it to be appropriate for DataManager. Taken from MapViewController.
- (void)routeKmlLoaded {
    routes = [routeKmlParser routes];
    [routes retain];
    
    stops = [routeKmlParser stops];
    [stops retain];
	
	[[NSNotificationCenter defaultCenter] postNotificationName:kDMRoutesandStopsLoaded object:self];
    
}


- (void)updateData {
    [self updateVehicleData];
    [self updateEtaData];
}


//	Pull updated vehicle data and posts a notification that it has done so.
//	Note that the notification is not on the main thread.
- (void)updateVehicleData {
    
	if (!loadVehicleJsonQueue) {
		loadVehicleJsonQueue = dispatch_queue_create("com.abstractedsheep.jsonqueue", NULL);
	}
    
    dispatch_async(loadVehicleJsonQueue, ^{
        if ([vehiclesJsonParser parseShuttles]) {
            [self performSelectorOnMainThread:@selector(vehicleJsonRefresh) withObject:nil waitUntilDone:YES];
			[[NSNotificationCenter defaultCenter] postNotificationName:kDMVehiclesUpdated
																object:vehicles 
															  userInfo:[NSDictionary dictionaryWithObject:vehicles forKey:@"vehicles"]];
        }
    });
    
}

- (void)vehicleJsonRefresh {
    BOOL alreadyAdded = NO;
    
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
	
	NSMutableArray *vehiclesToRemove = [[NSMutableArray alloc] init];
	
	for (JSONVehicle *vehicle in vehicles) {
		//	Remove vehicles which have not been updated for three minutes
		if ([vehicle.updateTime timeIntervalSinceNow] < -kRemoveShuttleThreshold) {
			[vehiclesToRemove addObject:vehicle];
		}
	}
	
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
															  userInfo:[NSDictionary dictionaryWithObject:etas forKey:@"ETAs"]];
        }
    });
    
}

- (void)etaJsonRefresh {
    [etas release];
    etas = [etasJsonParser.etas copy];
    
	[numberEtas release];
	numberEtas = [[NSMutableDictionary alloc] init];
	
    westEtas = 0;
    eastEtas = 0;
    
    for (EtaWrapper *eta in etas) {
		NSString *routeName = nil;
		
		NSArray *currentRouteNames = [self routeNames];
		
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
		
		if (eta.route == 1) {
            westEtas++;
        } else if (eta.route == 2) {
            eastEtas++;
        }
    }
    
    for (EtaWrapper *eta in etas) {
        for (KMLStop *stop in stops) {
            if (NULL) {
                //	None
				//	Eventually, this should set the next ETA for
				//	each stop, for each route that the stop is on.
            }
        }
    }
}


//	Iterate through the list of routes, and return a list of the route names
- (NSArray *)routeNames {
	if (!routes) {
		return [NSArray arrayWithObject:nil];
	}
	
	NSMutableArray *routeNames = [[[NSMutableArray alloc] init] autorelease];
	
	BOOL alreadyCounted;
	
	for (KMLRoute *route in routes) {
		alreadyCounted = NO;
		
		for (NSString *existingName in routeNames) {
			if ([route.name isEqualToString:existingName]) {
				alreadyCounted = YES;
			}
		}
		
		if (!alreadyCounted) {
			[routeNames addObject:route.name];
		}
	}
	
	return routeNames;
}


//	Iterate as above, but return only the first word from the route names.
//	This is prettier than the full names.
- (NSArray *)routeShortNames {
	if (!routes) {
		return [NSArray arrayWithObject:nil];
	}
	
	NSMutableArray *routeNames = [[[NSMutableArray alloc] init] autorelease];
	
	BOOL alreadyCounted;
	
	for (KMLRoute *route in routes) {
		alreadyCounted = NO;
		
		for (NSString *existingName in routeNames) {
			if ([route.name isEqualToString:existingName]) {
				alreadyCounted = YES;
			}
		}
		
		if (!alreadyCounted) {
			[routeNames addObject:route.name];
		}
	}
	
	NSMutableArray *routeFirstNames = [[[NSMutableArray alloc] init] autorelease];
	
	for (NSString *name in routeNames) {
		[routeFirstNames addObject:[[name componentsSeparatedByString:@" "] objectAtIndex:0]];
	}
	
	return routeFirstNames;
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
	
	KMLRoute *route = [routes objectAtIndex:routeNo];
	
	if (route) {
		NSNumber *noEtas = [numberEtas objectForKey:route.name];
		
		return noEtas ? [noEtas intValue] : 0;
	}
	
	return 0;
}

//	Called by InAppSettingsKit whenever a setting is changed in the settings view inside the app.
//	Currently only handles the 12/24 hour time toggle.
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
	}
}

@end
