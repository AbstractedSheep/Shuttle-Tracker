/*
 Original header:
 
     File: KMLParser.h
 Abstract: 
 Implements a limited KML parser.
 The following KML types are supported:
         Style,
         LineString,
         Point,
         Polygon,
         Placemark.
      All other types are ignored
 
  Version: 1.1

 Copyright (C) 2010 Apple Inc. All Rights Reserved.
 
 Shuttle-Tracker header:
 
 Version 1.0
 Copyright (C) 2011 Brendon Justin
*/

#import <MapKit/MapKit.h>

@class KMLPlacemark;
@class KMLStyle;
@class KMLNetworkLink;

@interface KMLParser : NSObject <NSXMLParserDelegate> {
    NSMutableDictionary *_styles;
    NSMutableArray *_placemarks;
	NSMutableArray *_networkLinks;
    
    KMLPlacemark *_placemark;
    KMLStyle *_style;
	KMLNetworkLink *_networkLink;
}

+ (KMLParser *)parseKMLAtURL:(NSURL *)url;
+ (KMLParser *)parseKMLAtPath:(NSString *)path;

@property (nonatomic, readonly) NSArray *overlays;
@property (nonatomic, readonly) NSArray *points;
@property (nonatomic, readonly) NSURL *shuttleDataUrl;

- (MKAnnotationView *)viewForAnnotation:(id <MKAnnotation>)point;
- (MKOverlayView *)viewForOverlay:(id <MKOverlay>)overlay;

@end
