//
//  KMLParser.m
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 1/29/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import "KMLParser.h"
#import <MapKit/MapKit.h>


@implementation KMLParser


- (id)init {
	if ((self == [super init])) {
		_placemarks = [[NSMutableArray alloc] init];
		_styles = [[NSMutableArray alloc] init];
        
        _routes = [[NSMutableArray alloc] init];
		_stops = [[NSMutableArray alloc] init];
        
        currentStyle = nil;
        currentPlacemark = nil;
        
        state.inStyle = NO;
        state.inPlacemark = NO;
        
        accumulation = [[NSMutableString alloc] init];
        
        //  The parser will be the one going through the KML file and extracting tags etc.
        //  TODO: Change to grab KML from the internet
        parser = [[NSXMLParser alloc] initWithContentsOfURL:[[NSBundle mainBundle] URLForResource:@"netlink" withExtension:@"kml"]];
        //  The parser call delegate functions further down in this file
        [parser setDelegate:self];
        
	}
	
	return self;
}

- (id)initWithContentsOfUrl:(NSURL *)url {
    if ((self == [super init])) {
		_placemarks = [[NSMutableArray alloc] init];
		_styles = [[NSMutableArray alloc] init];
        
        _routes = [[NSMutableArray alloc] init];
		_stops = [[NSMutableArray alloc] init];
        
        currentStyle = nil;
        currentPlacemark = nil;
        
        state.inStyle = NO;
        state.inPlacemark = NO;
        
        accumulation = [[NSMutableString alloc] init];
        
        //  The parser will be the one going through the KML file and extracting tags etc.
        //  TODO: Change to grab KML from the internet
        parser = [[NSXMLParser alloc] initWithContentsOfURL:url];
        //  The parser call delegate functions further down in this file
        [parser setDelegate:self];
        
	}
    
    return self;
}

- (void)parse {
    //  Do something with the return value?
    [parser parse];
}


//  Do not return styles or placemarks by themselves
//
//- (NSArray *)styles {
//    return [NSArray arrayWithArray:_styles];
//}
//
//- (NSArray *)placemarks {
//    return [NSArray arrayWithArray:_placemarks];
//}

//  Return an NSArray of all of the routes found in the KML file
- (NSArray *)routes {
    return [NSArray arrayWithArray:_routes];
}

//  Return an NSArray of all of the stops found in the KML file
- (NSArray *)stops {
    return [NSArray arrayWithArray:_stops];
}

- (void)dealloc {
    [_styles release];
    [_placemarks release];
    [_routes release];
    [_stops release];
    [super dealloc];
}

#pragma mark -
#pragma mark NSXMLParserDelegate Functions

- (void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError 
{
    NSLog(@"Error: %@", [parseError localizedDescription]);
}


//  Called when a start tag is found.
//  Set the state of the parser, allocate an object if necessary, and set the state of the object.
- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI
 qualifiedName:(NSString *)qualifiedName attributes:(NSDictionary *)attributeDict {
    
    //  KML elements describing a style
    if ([elementName isEqualToString:@"Style"]) {
        state.inStyle = YES;
        currentStyle = [[KMLStyle alloc] init];
        
    } else if (state.inStyle && [elementName isEqualToString:@"LineStyle"]) {
        currentStyle.styleType = lineStyle;
        
    } else if (state.inStyle && [elementName isEqualToString:@"color"]) {
        //  Do something?
        currentStyle.parseState = colorState;
        
    } else if (state.inStyle && [elementName isEqualToString:@"width"]) {
        //  Do something?
        currentStyle.parseState = widthState;
        
    }
    //  KML elements describing a placemark
    else if ([elementName isEqualToString:@"Placemark"]) {
        state.inPlacemark = YES;
        
        NSString *idTag = [attributeDict objectForKey:@"id"];
        
        if (idTag && [idTag rangeOfString:@"route"].location != NSNotFound) {
            currentPlacemark = (KMLPlacemark *)[[KMLRoute alloc] init];
            currentPlacemark.placemarkType = routeType;
            
        } else if (idTag && [idTag rangeOfString:@"stop"].location != NSNotFound) {
            currentPlacemark = (KMLPlacemark *)[[KMLStop alloc] init];
            currentPlacemark.placemarkType = stopType;
            
        } else if (idTag && [idTag rangeOfString:@"vehicle"].location != NSNotFound) {
            currentPlacemark = (KMLPlacemark *)[[KMLVehicle alloc] init];
            currentPlacemark.placemarkType = vehicleType;
            
        } else {
            assert(0);
        }
        
    } else if (state.inPlacemark && [elementName isEqualToString:@"name"]) {
        currentPlacemark.parseState = nameState;
        
    } else if (state.inPlacemark && [elementName isEqualToString:@"description"]) {
        currentPlacemark.parseState = descriptionState;
        
    } else if (state.inPlacemark && [elementName isEqualToString:@"styleUrl"]) {
        currentPlacemark.parseState = styleUrlState;
        
    } else if (state.inPlacemark && [elementName isEqualToString:@"LineString"] || [elementName isEqualToString:@"Point"]) {
        //  These indicate the type of the following element. Do nothing for both LineString and Point!
        
    } else if (state.inPlacemark && [elementName isEqualToString:@"coordinates"]) {
        currentPlacemark.parseState = coordinatesState;
        
    }
    /* else if ([elementName isEqualToString:@"Placemark"]) {
        
    } */
}


//  Called with the string found inside a tag, if it is not another tag or CDATA.
- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string {
    NSLog(@"Found characters: %@", string);
    
    //  Append the current string to the string accumulation,
    //  but currently nothing is done with the accumulation
    [accumulation appendString:string];
    
    if (state.inStyle && currentStyle) {
        switch (currentStyle.parseState) {
            case colorState:
                currentStyle.color = string;
                break;
                
            case widthState:
                currentStyle.width = [string intValue];
                break;
                
            default:
                NSLog(@"Style parsing error.  Found characters: %@", string);
                break;
                
        }
    } else if (state.inPlacemark && currentPlacemark) {
        KMLRoute *routePlacemark;
        KMLStop *stopPlacemark;
        KMLVehicle *vehiclePlacemark;
        
        //  The name, description and styleUrl are used by all KMLPlacemark types
        if (currentPlacemark.parseState == nameState) {
            currentPlacemark.name = [string copy];
            
        } else if (currentPlacemark.parseState == descriptionState) {
            currentPlacemark.description = [string copy];
            
        } else if (currentPlacemark.parseState == styleUrlState) {
            currentPlacemark.styleUrl = [string copy];
            
        } else if (currentPlacemark.parseState == coordinatesState) {
            NSArray *coordinates;
            
            switch (currentPlacemark.placemarkType) {
                case routeType:
                    routePlacemark = (KMLRoute *)currentPlacemark;
                    
                    //  Note that the first item in the string is "", and the last is "    \n" (four spaces and a newline)
                    NSString *tempString = [string substringWithRange:NSMakeRange(1, [string length] - 1 - 5)];
                    
                    //  Store the list of coordinates in the route
                    routePlacemark.lineString = [tempString componentsSeparatedByString:@"\n"];
                    for (id coords in routePlacemark.lineString){
                        NSLog(@"Coordinates: |%@|", coords);
                    }
                    
                    break;
                    
                case pointType:     //  Should never be used directly, only subclassed.  So do nothing.
                    NSLog(@"Error, pointType should not be used directly, only subclassed.");
                    
                    break;
                    
                case stopType:
                    stopPlacemark = (KMLStop *)currentPlacemark;
                    coordinates = [string componentsSeparatedByString:@","];
                    
                    if (coordinates && [coordinates count] == 2) {
                        stopPlacemark.coordinates = CLLocationCoordinate2DMake([[coordinates objectAtIndex:0] doubleValue], 
                                                                               [[coordinates objectAtIndex:1] doubleValue]);                        
                    }
                    
                    break;
                    
                case vehicleType:
                    vehiclePlacemark = (KMLVehicle *)currentPlacemark;
                    coordinates = [string componentsSeparatedByString:@","];
                    
                    if (coordinates && [coordinates count] == 2) {
                        vehiclePlacemark.coordinates = CLLocationCoordinate2DMake([[coordinates objectAtIndex:0] doubleValue], 
                                                                                  [[coordinates objectAtIndex:1] doubleValue]);                        
                    }
                    
                    break;
                    
                case nilType:   //  Something went wrong if we get here!
                    NSLog(@"Error, nilType encountered.");
                    
                    break;
                    
                default:        //  Something unexpected?
                    NSLog(@"Error, type not found.");
                    
                    break;
                    
            }
        }
    }
}


//  Called for CDATA found inside a tag
- (void)parser:(NSXMLParser *)parser foundCDATA:(NSData *)CDATABlock {
    if (currentPlacemark && currentPlacemark.parseState == descriptionState && currentPlacemark.placemarkType == routeType) {
        currentPlacemark.description = [[[NSString alloc] initWithData:CDATABlock encoding:NSUTF8StringEncoding] autorelease];
        
    } else {
        NSLog(@"Error, uncaught CDATA: %@", [[[NSString alloc] initWithData:CDATABlock encoding:NSUTF8StringEncoding] autorelease]);
    }
}


//  Called when a closing tag is found.
//  Unset the state of the parser, free any objects which are no longer needed, etc.
- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName {
    //  KML elements describing a style
    if ([elementName isEqualToString:@"Style"]) {
        state.inStyle = NO;
        [currentStyle release];
        currentStyle = nil;
        
    } else if (state.inStyle) {
        currentStyle.styleType = nilStyle;
    }
    //  KML elements describing a placemark
    else if ([elementName isEqualToString:@"Placemark"]) {
        state.inPlacemark = NO;
        
        NSString *idTag = currentPlacemark.idTag;
        
        if (idTag && [idTag isEqualToString:@"route"]) {
            [_routes addObject:(KMLRoute *)currentPlacemark];
            [(KMLRoute *)currentPlacemark release];
            
        } else if (idTag && [idTag isEqualToString:@"stop"]) {
            [_stops addObject:(KMLStop *)currentPlacemark];
            [(KMLStop *)currentPlacemark release];
            
        } else if (idTag && [idTag isEqualToString:@"vehicle"]) {
            
            
            
            //  TODO: Handle shuttles!
            
            [(KMLVehicle *)currentPlacemark release];
            
            
            
        } else if (idTag) {
            assert(0);
        }
        
        currentPlacemark = nil;
        
    } else if (state.inPlacemark) {
        currentPlacemark.parseState = nilPlacemarkParseState;
        
    }
    
}


@end


#pragma mark -
#pragma mark KML Objects

@implementation KMLPlacemark

@synthesize name;
@synthesize idTag;
@synthesize description;
@synthesize styleUrl;
@synthesize placemarkType;
@synthesize parseState;

- (id)init {
    if ((self = [super init])) {
        name = nil;
        idTag = nil;
        description = nil;
        
        placemarkType = nilType;
        parseState = nilPlacemarkParseState;
    }
    
    return self;
}

@end

@implementation KMLRoute

@synthesize lineString;


@end

@implementation KMLPoint

@synthesize coordinates;


@end


@implementation KMLStop


@end

@implementation KMLVehicle


@end

@implementation KMLStyle

@synthesize color;
@synthesize width;
@synthesize styleType;
@synthesize parseState;

- (id)init {
    if ((self = [super init])) {
        color = nil;
        width = 0;
        
        styleType = nilStyle;
        parseState = nilStyleParseState;
    }
    
    return self;
}

@end

