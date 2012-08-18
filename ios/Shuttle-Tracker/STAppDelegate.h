//
//  STAppDelegate.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 10/29/11.
//  Copyright (c) 2011 Brendon Justin. All rights reserved.
//

#import <UIKit/UIKit.h>

@class STDataManager;

@interface STAppDelegate : UIResponder <UIApplicationDelegate>

@property (strong, nonatomic) UIWindow *window;

@property (readonly, strong, nonatomic) NSManagedObjectContext *managedObjectContext;
@property (readonly, strong, nonatomic) NSManagedObjectModel *managedObjectModel;
@property (readonly, strong, nonatomic) NSPersistentStoreCoordinator *persistentStoreCoordinator;

- (void)saveContext;
- (NSURL *)applicationDocumentsDirectory;

@property (strong, nonatomic) UINavigationController *navigationController;

@property (strong, nonatomic) UISplitViewController *splitViewController;

@property (strong, nonatomic) STDataManager *dataManager;
@property (strong, nonatomic) UITabBarController *tabBarController;
@property (strong, nonatomic) NSDateFormatter *timeDisplayFormatter;
@property (strong, nonatomic) NSTimer *dataUpdateTimer;

@end
