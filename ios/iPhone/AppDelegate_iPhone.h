//
//  AppDelegate_iPhone.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 1/29/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import <UIKit/UIKit.h>

@class MainViewController_iPhone;

@interface AppDelegate_iPhone : NSObject <UIApplicationDelegate> {
    UIWindow *window;
	MainViewController_iPhone *mainViewController;
}

@property (nonatomic, retain) IBOutlet UIWindow *window;

@end

