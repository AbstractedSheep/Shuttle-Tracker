//
//  STMapViewController.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 1/29/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <MapKit/MapKit.h>
#import "STDataManager.h"


@class STJSONParser;

@interface STMapViewController : UIViewController <MKMapViewDelegate, UISplitViewControllerDelegate> {
    MKMapView               *m_mapView;
    
    NSMutableDictionary     *m_vehicles;
    NSMutableArray          *m_routeLines;
    NSMutableArray          *m_routeLineViews;

    UIImage                 *m_shuttleImage;
    NSMutableDictionary     *m_magentaShuttleImages;
    NSMutableDictionary     *m_shuttleImages;

    STDataManager           *m_dataManager;
    NSTimer                 *m_shuttleCleanupTimer;
}

@property (nonatomic, assign) STDataManager *dataManager;
@property (strong, nonatomic) NSManagedObjectContext *managedObjectContext;


@end
