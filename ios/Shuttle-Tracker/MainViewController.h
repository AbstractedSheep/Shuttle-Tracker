//
//  MainViewController.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 10/29/11.
//  Copyright (c) 2011 Naga Softworks, LLC. All rights reserved.
//

#import <UIKit/UIKit.h>

@class DataManager;

@interface MainViewController : UIViewController

@property (strong, nonatomic) DataManager *dataManager;
@property (strong, nonatomic) UITabBarController *tabBarController;
@property (strong, nonatomic) NSDateFormatter *timeDisplayFormatter;
@property (strong, nonatomic) UISplitViewController *splitViewController;

@property (strong, nonatomic) NSFetchedResultsController *fetchedResultsController;
@property (strong, nonatomic) NSManagedObjectContext *managedObjectContext;

@end
