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
            
            [_routes addObject:(KMLRoute *)currentPlacemark];
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
}


//  Called with the string found inside a tag, if it is not another tag or CDATA.
- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string {
    NSLog(@"Found characters: %@", string);
    
    //  Append the current string to the string accumulation,
    //  to be used in parser:didEndElement:
    [accumulation appendString:string];
    
}

- (void)parser:(NSXMLParser *)parser foundIgnorableWhitespace:(NSString *)whitespaceString {
    //  Note that the first item in the coordinate string is "", and the last is "    \n" (four spaces and a newline)
    if (currentPlacemark && currentPlacemark.parseState == coordinatesState) {
        if ([whitespaceString isEqualToString:@"    \n"]) {
            //  do nothing
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
    if (state.inStyle && [elementName isEqualToString:@"Style"]) {
        state.inStyle = NO;
        [currentStyle release];
        currentStyle = nil;
        
    } else if (state.inStyle && [elementName isEqualToString:@"LineStyle"]) {
        //  Nothing
        
    } else if (state.inStyle && currentStyle.parseState == colorState) {
        currentStyle.colorString = accumulation;
        
    } else if (state.inStyle && currentStyle.parseState == widthState) {
        currentStyle.width = [accumulation intValue];
        
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
        currentPlacemark.name = [accumulation copy];
        
    } else if (state.inPlacemark && currentPlacemark.parseState == descriptionState) {
        currentPlacemark.description = [accumulation copy];
        
    } else if (state.inPlacemark && currentPlacemark.parseState == styleUrlState) {
        currentPlacemark.styleUrl = [accumulation copy];
        
        for (KMLStyle *style in _styles) {
            if ([accumulation rangeOfString:style.idTag].location != NSNotFound) {
                currentPlacemark.style = style;
            }
        }
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
                routePlacemark.lineString = [[accumulation substringFromIndex:1] componentsSeparatedByString:@"\n"];
                for (id coords in routePlacemark.lineString){
                    NSLog(@"Coordinates: |%@|", coords);
                }
                
                break;
                
            case pointType:     //  Should never be used directly, only subclassed.  So do nothing.
                NSLog(@"Error, pointType should not be used directly, only subclassed.");
                
                break;
                
            case stopType:
                stopPlacemark = (KMLStop *)currentPlacemark;
                coordinates = [accumulation componentsSeparatedByString:@","];
                
                if (coordinates && [coordinates count] == 2) {
                    stopPlacemark.coordinates = CLLocationCoordinate2DMake([[coordinates objectAtIndex:0] doubleValue], 
                                                                           [[coordinates objectAtIndex:1] doubleValue]);                        
                }
                
                break;
                
            case vehicleType:
                vehiclePlacemark = (KMLVehicle *)currentPlacemark;
                coordinates = [accumulation componentsSeparatedByString:@","];
                
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
    
    
    return [UIColor colorWithRed:((float)((rgbaValue & 0xFF000000) >> 16))/255.0
                           green:((float)((rgbaValue & 0xFF0000) >> 8))/255.0
                            blue:((float)(rgbaValue & 0xFF00))/255.0
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

@synthesize coordinates;


@end


@implementation KMLStop


@end

@implementation KMLVehicle


@end

