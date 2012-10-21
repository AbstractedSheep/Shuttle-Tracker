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

@interface STMapViewController : UIViewController <MKMapViewDelegate, UISplitViewControllerDelegate>

@property (nonatomic, weak) STDataManager *dataManager;
@property (strong, nonatomic) NSManagedObjectContext *managedObjectContext;


@end
