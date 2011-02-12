//
//  JSONParser.m
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 2/12/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import "JSONParser.h"
#import "TouchJSON/Extensions/NSDictionary_JSONExtensions.h"

@implementation JSONParser

@synthesize jsonDict;
@synthesize vehicles;


- (id)init {
    [self initWithUrl:[NSURL URLWithString:@"http://nagasoftworks.com/ShuttleTracker/shuttleOutputData.txt"]];
    
    return self;
}


//  Init and parse JSON from some specified URL
- (id)initWithUrl:(NSURL *)url {
    if ((self = [super init])) {
        jsonUrl = url;
        [jsonUrl retain];
        
    }
    
    return self;
}


- (void)parse {
    NSError *theError = nil;
    NSString *jsonString = [NSString stringWithContentsOfURL:jsonUrl encoding:NSUTF8StringEncoding error:&theError];
    
    [vehicles release];
    
    vehicles = [[NSMutableArray alloc] init];
    
    if (theError) {
        NSLog(@"Error retrieving JSON data");
    } else {
        if (jsonString) {
            jsonDict = [NSDictionary dictionaryWithJSONString:jsonString error:&theError];
        } else {
            jsonDict = nil;
        }
        
        
        //  Iterate through the items found, create vehicles, set their locations, and add them to the vehicles array
        for (NSString *string in jsonDict) {
            JSONVehicle *vehicle = [[JSONVehicle alloc] init];
            vehicle.name = string;
            vehicle.description = @"lol";
            
            NSArray *coordinates = [[jsonDict objectForKey:string] objectsForKeys:[NSArray arrayWithObjects:@"Latitude", @"Longitude", nil] notFoundMarker:[NSNull null]];
            
            vehicle.coordinate = CLLocationCoordinate2DMake([[coordinates objectAtIndex:0] doubleValue], [[coordinates objectAtIndex:1] doubleValue]);
            
            [vehicles addObject:vehicle];
        }
    }
    
}


- (void)dealloc {
    [super dealloc];
}


@end


@implementation JSONVehicle

@synthesize name;
@synthesize description;
@synthesize coordinate;
@synthesize ETAs;
@synthesize annotationView;


- (id)init {
    if ((self = [super init])) {
        name = nil;
        ETAs = nil;
    }

    return self;
}

@end