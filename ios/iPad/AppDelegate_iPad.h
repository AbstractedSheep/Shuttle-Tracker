//
//  AppDelegate_iPad.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 1/29/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import <UIKit/UIKit.h>

@class MainViewController_iPad;

@interface AppDelegate_iPad : NSObject <UIApplicationDelegate> {
    UIWindow *window;
	MainViewController_iPad *mapVC;
}

@property (nonatomic, retain) IBOutlet UIWindow *window;

@end

