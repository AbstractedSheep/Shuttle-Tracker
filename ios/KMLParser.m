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

@synthesize routes = _routes;
@synthesize stops = _stops;
@synthesize vehiclesUrl;

- (id)init {
    //  Init with the default KML, netlink.kml
    [self initWithContentsOfUrl:[[NSBundle mainBundle] URLForResource:@"netlink" withExtension:@"kml"]];
	
	return self;
}

- (id)initWithContentsOfUrl:(NSURL *)url {
    if ((self = [super init])) {
		_placemarks = [[NSMutableArray alloc] init];
		_styles = [[NSMutableArray alloc] init];
        
        _routes = [[NSMutableArray alloc] init];
		_stops = [[NSMutableArray alloc] init];
        _vehicles = [[NSMutableArray alloc] init];
        
        currentStyle = nil;
        currentPlacemark = nil;
        
        state.inStyle = NO;
        state.inPlacemark = NO;
        state.inNetworkLink = NO;
        
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
    if (!accumulation) {
        accumulation = [[NSMutableString alloc] init];
    }
    
    //  KML elements describing a style
    if ([elementName isEqualToString:@"Style"]) {
        state.inStyle = YES;
        currentStyle = [[KMLStyle alloc] init];
        
        NSString *idTag = [attributeDict objectForKey:@"id"];
        
        currentStyle.idTag = idTag;
        
    } else if (state.inStyle && [elementName isEqualToString:@"LineStyle"]) {
        currentStyle.styleType = lineStyle;
        
    } else if (state.inStyle && [elementName isEqualToString:@"color"]) {
        currentStyle.parseState = colorState;
        
    } else if (state.inStyle && [elementName isEqualToString:@"width"]) {
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
        
        currentPlacemark.idTag = idTag;
        
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
    //  KML elements describing where to find vehicle data
    else if ([elementName isEqualToString:@"NetworkLink"]) {
        state.inNetworkLink = YES;
    }
}


//  Called with the string found inside a tag, if it is not another tag or CDATA.
- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string {
//    NSLog(@"Found characters: %@", string);
    
    //  Append the current string, minus whitespace and newlines at the ends, to the string accumulation,
    //  to be used in parser:didEndElement:
    [accumulation appendString:string];
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
    NSString *trimmedAccumulation = [accumulation stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
    
    if (trimmedAccumulation) {
        [trimmedAccumulation retain];
        NSLog(@"Trimmed: %@", trimmedAccumulation);
    } else {
        trimmedAccumulation = @"";
    }
    
    //  KML elements describing a style
    if (state.inStyle && [elementName isEqualToString:@"Style"]) {
        [_styles addObject:currentStyle];
        
        state.inStyle = NO;
        [currentStyle release];
        currentStyle = nil;
        
    } else if (state.inStyle && [elementName isEqualToString:@"LineStyle"]) {
        //  Nothing
        
    } else if (state.inStyle && currentStyle.parseState == colorState) {
        //  For whatever reason, this one has "\n \n    " (that's four spaces at the end) as a prefix, so get rid of it
        currentStyle.colorString = trimmedAccumulation;
//        currentStyle.color = [currentStyle UIColorFromRGBAString:currentStyle.colorString];
        currentStyle.parseState = nilStyleParseState;
        
    } else if (state.inStyle && currentStyle.parseState == widthState) {
        currentStyle.width = [trimmedAccumulation intValue];
        currentStyle.parseState = nilStyleParseState;
        
    }
    //  KML elements describing a placemark
    else if (state.inPlacemark && [elementName isEqualToString:@"Placemark"]) {
        state.inPlacemark = NO;
        
        NSString *idTag = currentPlacemark.idTag;
        
        if (idTag && [idTag rangeOfString:@"route"].location != NSNotFound) {
            [_routes addObject:(KMLRoute *)currentPlacemark];
            [(KMLRoute *)currentPlacemark release];
            
        } else if (idTag && [idTag rangeOfString:@"stop"].location != NSNotFound) {
            [_stops addObject:(KMLStop *)currentPlacemark];
            [(KMLStop *)currentPlacemark release];
            
        } else if (idTag && [idTag rangeOfString:@"vehicle"].location != NSNotFound) {
            //  Handles shuttles
            [_vehicles addObject:(KMLVehicle *)currentPlacemark];
            [(KMLVehicle *)currentPlacemark release];
            
        } else if (idTag) {
            NSLog(@"Unrecognized id: %@", idTag);
            assert(0);
        }
        
        currentPlacemark = nil;
    } else if (state.inPlacemark && currentPlacemark.parseState == nameState) {
        currentPlacemark.name = [trimmedAccumulation copy];
        currentPlacemark.parseState = nilPlacemarkParseState;
        
    } else if (state.inPlacemark && currentPlacemark.parseState == descriptionState) {
        currentPlacemark.description = [trimmedAccumulation copy];
        currentPlacemark.parseState = nilPlacemarkParseState;
        
    } else if (state.inPlacemark && currentPlacemark.parseState == styleUrlState) {
        currentPlacemark.styleUrl = [trimmedAccumulation copy];
        
        for (KMLStyle *style in _styles) {
            if ([trimmedAccumulation rangeOfString:style.idTag].location != NSNotFound) {
                currentPlacemark.style = style;
            }
        }
        currentPlacemark.parseState = nilPlacemarkParseState;
        
    } else if (state.inPlacemark && [elementName isEqualToString:@"LineString"] || [elementName isEqualToString:@"Point"]) {
        //  These indicate the type of the following element. Do nothing for both LineString and Point!
        
    } else if (state.inPlacemark && currentPlacemark.parseState == coordinatesState) {
        KMLRoute *routePlacemark;
        KMLStop *stopPlacemark;
        KMLVehicle *vehiclePlacemark;
        
        NSArray *coordinates;
        
        switch (currentPlacemark.placemarkType) {
            case routeType:
                routePlacemark = (KMLRoute *)currentPlacemark;
                
                //  Store the list of coordinates in the route
                routePlacemark.lineString = [[trimmedAccumulation componentsSeparatedByString:@"\n"] autorelease];
                
                break;
                
            case pointType:     //  Should never be used directly, only subclassed.  So do nothing.
                NSLog(@"Error, pointType should not be used directly, only subclassed.");
                
                break;
                
            case stopType:
                stopPlacemark = (KMLStop *)currentPlacemark;
                coordinates = [trimmedAccumulation componentsSeparatedByString:@","];
                
                if (coordinates && [coordinates count] == 2) {
                    stopPlacemark.coordinate = CLLocationCoordinate2DMake([[coordinates objectAtIndex:1] doubleValue], 
                                                                           [[coordinates objectAtIndex:0] doubleValue]);                        
                }
                
                break;
                
            case vehicleType:
                vehiclePlacemark = (KMLVehicle *)currentPlacemark;
                coordinates = [trimmedAccumulation componentsSeparatedByString:@","];
                
                if (coordinates && [coordinates count] == 2) {
                    vehiclePlacemark.coordinate = CLLocationCoordinate2DMake([[coordinates objectAtIndex:1] doubleValue], 
                                                                              [[coordinates objectAtIndex:0] doubleValue]);                        
                }
                
                break;
                
            case nilType:   //  Something went wrong if we get here!
                NSLog(@"Error, nilType encountered.");
                
                break;
                
            default:        //  Something unexpected?
                NSLog(@"Error, type not found.");
                
                break;
                
        }
        currentPlacemark.parseState = nilPlacemarkParseState;
        
    }
    //  KML elements describing where to find vehicle data
    else if (state.inNetworkLink && [elementName isEqualToString:@"Link"]) {
        vehiclesUrl = [NSURL URLWithString:trimmedAccumulation];
    }
    
    
    if (trimmedAccumulation) {
        [trimmedAccumulation release];
    }

    [accumulation release];
    accumulation = nil;
}


@end


#pragma mark -
#pragma mark KML Objects

@interface KMLStyle ()

- (UIColor *)UIColorFromRGBAString:(NSString *)rgbaString;

@end
@implementation KMLStyle

@synthesize idTag;
@synthesize color;
@synthesize width;
@synthesize styleType;
@synthesize parseState;

- (id)init {
    if ((self = [super init])) {
        colorString = nil;
        color = [UIColor whiteColor];
        width = 0;
        
        styleType = nilStyle;
        parseState = nilStyleParseState;
    }
    
    return self;
}

- (NSString *)colorString {
    return colorString;
}

- (void)setColorString:(NSString *)newColorString {
    colorString = newColorString;
    
    [colorString retain];
    
    color = [self UIColorFromRGBAString:colorString];
}


//  Take an NSString formatted as such: RRGGBBAA and return a UIColor
- (UIColor *)UIColorFromRGBAString:(NSString *)rgbaString {
    NSScanner *scanner;
    unsigned int rgbaValue;
    
    if (rgbaString) {
        scanner = [NSScanner scannerWithString:rgbaString];
        [scanner scanHexInt:&rgbaValue];
        
    } else {
        rgbaValue = 0;
    }
    
//    NSLog(@"%@", [UIColor colorWithRed:((float)((rgbaValue & 0xFF000000) >> 24))/255.0
//                                 green:((float)((rgbaValue & 0xFF0000) >> 16))/255.0
//                                  blue:((float)((rgbaValue & 0xFF00) >> 8))/255.0
//                                 alpha:((float)(rgbaValue & 0xFF))/255.0]);
    
    return [UIColor colorWithRed:((float)((rgbaValue & 0xFF000000) >> 24))/255.0
                           green:((float)((rgbaValue & 0xFF0000) >> 16))/255.0
                            blue:((float)((rgbaValue & 0xFF00) >> 8))/255.0
                           alpha:((float)(rgbaValue & 0xFF))/255.0];
}

@end


@implementation KMLPlacemark

@synthesize name;
@synthesize idTag;
@synthesize description;
@synthesize styleUrl;
@synthesize style;
@synthesize placemarkType;
@synthesize parseState;

- (id)init {
    if ((self = [super init])) {
        name = nil;
        idTag = nil;
        description = nil;
        styleUrl = nil;
        style = nil;
        
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

@synthesize coordinate;


//  The subtitle for a map pin
- (NSString *)subtitle {
    return description;
}

//  The title for a map pin
- (NSString *)title {
    return name;
}

- (id)initWithLocation:(CLLocationCoordinate2D)coord {
    [self init];
    
    if (self) {
        coordinate = coord;
    }
    
    return self;
}

@end


@implementation KMLStop


@end

@implementation KMLVehicle


@end

