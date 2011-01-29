//
//  MapViewController.h
//  Shuttle Tracker
//
//  Created by Brendon Justin on 1/27/11.
//  Copyright 2011 Naga Softworks, LLC. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "RMMapView.h"


@interface MapViewController_CM : UIViewController {
    RMMapView *mapView;
@private
    
}

@property (nonatomic, retain) RMMapView *mapView;

@end
