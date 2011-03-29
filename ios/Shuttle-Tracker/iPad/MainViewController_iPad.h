//
//  MainViewController_iPad.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 3/21/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MainViewController.h"

@class MapViewController;

@interface MainViewController_iPad : MainViewController {
	MapViewController *mapViewController;
    
}

@property (nonatomic, retain) MapViewController *mapViewController;

@end
