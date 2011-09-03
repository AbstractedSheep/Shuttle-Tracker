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
    UIToolbar *toolbar;
	MapViewController *mapViewController;
    
    UIPopoverController *etaPopover;
}

@property (nonatomic, retain) UIToolbar *toolbar;
@property (nonatomic, retain) MapViewController *mapViewController;
@property (nonatomic, retain) UIPopoverController *etaPopover;

- (void)showEtas:(id)sender;

@end
