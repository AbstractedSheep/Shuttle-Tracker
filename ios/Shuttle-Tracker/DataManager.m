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
- (void)settingChanged:(NSNotification *)notification;

@end

@implementation DataManager


@synthesize routes;
@synthesize stops;
@synthesize routeNames;
@synthesize routeShortNames;
@synthesize vehicles;
@synthesize etas;
@synthesize soonestEtas;
@synthesize favoriteEtas;
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
		favoriteStopNames = [[NSMutableArray alloc] init];
		favoriteEtas = [[NSMutableArray alloc] init];
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
	if (favoriteStopNames) {
		[favoriteStopNames release];
	}
	if (favoriteEtas) {
		[favoriteEtas release];
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
	EtaWrapper *soonestEtaWrapper = nil;
	
	
    routes = routesStopsJsonParser.routes;
    [routes retain];
    
    stops = routesStopsJsonParser.stops;
    [stops retain];
	
	
	//	For each route, create an array to hold its soonest ETAs
	for (MapRoute *route in routes) {
		NSMutableArray *routeArray = [[NSMutableArray alloc] init];
		[soonestEtas setObject:routeArray 
						forKey:[NSNumber numberWithInt:[route.idTag intValue]]];
	}
	
	//	For each stop, create a dummy ETA for each route it is on.
	//	Add the dummy ETAs to the soonest ETAs array for the
	//	corresponding route
	for (MapStop *stop in stops) {
		for (NSString *routeId in stop.routeIds) {
			soonestEtaWrapper = [[EtaWrapper alloc] init];
			soonestEtaWrapper.stopName = stop.name;
			soonestEtaWrapper.stopId = stop.idTag;
			
			//	Get the array of soonest ETAs for the correct route.
			NSMutableArray *routeSoonestEtas = [soonestEtas objectForKey:
												[NSNumber numberWithInt:[routeId intValue]]];
			
			if (routeSoonestEtas) {
				[routeSoonestEtas addObject:soonestEtaWrapper];
				[soonestEtas setObject:routeSoonestEtas 
								forKey:[NSNumber numberWithInt:[routeId intValue]]];
			}
		}
	}
    
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
    
//    [soonestEtas release];
//    soonestEtas = [[NSMutableDictionary alloc] init];
    
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
		
		EtaWrapper *newSoonEta = nil;
        BOOL setThis = YES;
        BOOL soonEtasChanged = NO;
		
		NSMutableArray *routeSoonestEtas = [soonestEtas objectForKey:[NSNumber numberWithInt:eta.route]];
		
		if (routeSoonestEtas) {
			//  Check to see if the current eta is the next one for its associated stop
			for (EtaWrapper *soonEta in routeSoonestEtas) {
				if ([eta.stopName isEqualToString:soonEta.stopName] && eta.stopId == soonEta.stopId) {
					if ([eta.eta timeIntervalSinceDate:soonEta.eta] > 0) {
						setThis = NO;
					} else {
						newSoonEta = soonEta;
					}
					
					break;
				}
			}
			
			if (setThis) {
				for (EtaWrapper *eta in routeSoonestEtas) {
					if ([eta.stopId isEqualToString:newSoonEta.stopId] && eta.route == newSoonEta.route) {
						eta.eta = newSoonEta.eta;
					}
				}
				
				soonEtasChanged = YES;
			}
		} else {
			NSLog(@"Error: Soonest ETAs array not created for route: %i", eta.route);
			
			routeSoonestEtas = [NSMutableArray arrayWithObjects:nil];
			
			[routeSoonestEtas addObject:eta];
			soonEtasChanged = YES;
		}
		
		//	TODO: Fix/remove/???
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


- (int)numberSections {
	if ([favoriteStopNames count]) {
		return [self.routeNames count] + 1;
	} else {
		return [self.routeNames count];
	}
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


//	Get the number of etas for the route corresponding to this section,
//	or the favorites if it was the favorites section.
//	sectionNo is expected to be 0-indexed in the method call.
- (int)numberEtasForSection:(int)sectionNo {
	int routeNo;
	
	//	If there are favorite etas, then adjust the route number.
	//	If the section is the favorites section, return the number of
	//	stops favorited.
	if ([favoriteStopNames count]) {
		routeNo = sectionNo;
		
		if (sectionNo == 0) {
			return [favoriteStopNames count];
		}
	} else {
		routeNo = sectionNo + 1;
	}
	
	if (!routes || routeNo > [routes count]) {
		return 0;
	}
	
	MapRoute *route = [routes objectAtIndex:routeNo - 1];
	
	if (route) {
		NSNumber *noEtas = nil;
        
        if (onlySoonestEtas) {
            NSArray *routeSoonestEtas = [soonestEtas objectForKey:[NSNumber numberWithInt:routeNo]];
			
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


- (NSArray *)sectionHeaders {
	if ([favoriteStopNames count]) {
		return [[NSArray arrayWithObject:@"Favorites"] arrayByAddingObjectsFromArray:[self routeShortNames]];
	} else {
		return [self routeShortNames];
	}
}


//	sectionNo is 0-indexed, but route numbers should be 1-indexed
- (NSArray *)etasForSection:(int)sectionNo {
	int routeNo;
	
	//	If there are favorite etas, then adjust the route number.
	//	If the section is the favorites section, return the etas for
	//	the favorite stops
	if ([favoriteStopNames count]) {
		routeNo = sectionNo;
		
		if (sectionNo == 0) {
			[favoriteEtas release];
			favoriteEtas = [[NSMutableArray alloc] init];
			
			for (EtaWrapper *etaFavorite in favoriteStopNames) {
				for (EtaWrapper *eta in [soonestEtas objectForKey:[NSNumber numberWithInt:etaFavorite.route]]) {
					if ([etaFavorite.stopName isEqualToString:eta.stopName] && etaFavorite.route == eta.route) {
						[favoriteEtas addObject:eta];
					}
				}
			}
			
			return favoriteEtas;
		}
	} else {
		routeNo = sectionNo + 1;
	}
	
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


//	The user selected an ETA, so add it to the favorites if it is not there yet,
//	or remove it from the favorites if it was selected in the favorites section
- (void)selectEtaAtIndexPath:(NSIndexPath *)indexPath {
	//	If the user has favorite stops, check if a favorite stop was selected,
	//	in section 0.  If so, remove the stop as a favorite.  Otherwise, add the stop
	//	as a favorite.
	if ([favoriteStopNames count] && !indexPath.section) {
		EtaWrapper *etaToUnfavorite = [favoriteStopNames objectAtIndex:indexPath.row];
		
		//	Remove the eta from the list of current displayed etas
		[favoriteStopNames removeObject:etaToUnfavorite];
		/*
		BOOL nameFound = NO;
		
		//	Find and remove the eta from the list used to track favorited etas
		for (EtaWrapper *eta in favoriteStopNames) {
			if ([eta.stopName isEqualToString:etaToUnfavorite.stopName] && eta.route == etaToUnfavorite.route) {
				etaToUnfavorite = eta;
				nameFound = YES;
				break;
			}
		}
		
		if (nameFound) {
			[favoriteStopNames removeObject:etaToUnfavorite];
		}
		 */
	} else {
		NSArray *sectionEtas = [self etasForSection:indexPath.section];
		
		if (indexPath.row >= [sectionEtas count]) {
			return;
		}
		
		EtaWrapper *etaToFavorite = [sectionEtas objectAtIndex:indexPath.row];
		
		//	Check if the selected eta already has a corresponding entry in the list of
		//	favorite etas.  If it doesn, nil it out so that it doesn't get added again.
		for (EtaWrapper *eta in favoriteStopNames) {
			if ([eta.stopName isEqualToString:etaToFavorite.stopName] && eta.route == etaToFavorite.route) {
				etaToFavorite = nil;
				break;
			}
		}
		
		if (etaToFavorite) {
			[favoriteStopNames addObject:etaToFavorite];
		}
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
