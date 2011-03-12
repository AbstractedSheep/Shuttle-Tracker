//
//  AppDelegate_iPad.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 1/29/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import <UIKit/UIKit.h>

@class MapViewController;

@interface AppDelegate_iPad : NSObject <UIApplicationDelegate> {
    UIWindow *window;
	MapViewController *mapVC;
}

@property (nonatomic, retain) IBOutlet UIWindow *window;

@end

