//
//  MainViewController_iPad.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 3/21/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MainViewController.h"

@class EtasViewController, MapViewController;

@interface MainViewController_iPad : MainViewController {
    UIToolbar *toolbar;
    UISplitViewController *splitViewController;
	MapViewController *mapViewController;
    EtasViewController *etasViewController;
}

@property (nonatomic, retain) UIToolbar *toolbar;
@property (nonatomic, retain) UISplitViewController *splitViewController;
@property (nonatomic, retain) MapViewController *mapViewController;
@property (nonatomic, retain) EtasViewController *etasViewController;

@end
