//
//  EtasViewController.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 2/20/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "DataManager.h"


@interface EtasViewController : UITableViewController <NSFetchedResultsControllerDelegate> {
    DataManager *dataManager;
    NSDateFormatter *timeDisplayFormatter;
    BOOL useRelativeTimes;
	NSMutableDictionary *routeStops;
}

@property (nonatomic, assign) DataManager *dataManager;
@property (nonatomic, assign) NSDateFormatter *timeDisplayFormatter;
@property (nonatomic) BOOL useRelativeTimes;
@property (strong, nonatomic) NSManagedObjectContext *managedObjectContext;

@end
