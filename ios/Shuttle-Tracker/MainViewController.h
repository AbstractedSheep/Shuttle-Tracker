//
//  MainViewController.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 2/20/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "DataManager.h"

@interface MainViewController : UIViewController {
    DataManager *dataManager;
    
    NSTimer *dataUpdateTimer;
    
	NSDateFormatter *timeDisplayFormatter;
}

@property (nonatomic, retain) DataManager *dataManager;
@property (nonatomic, assign) NSDateFormatter *timeDisplayFormatter;


@end
