//
//  STEtasViewController.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 2/20/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "STDataManager.h"


@interface STEtasViewController : UITableViewController <NSFetchedResultsControllerDelegate>

@property (nonatomic, weak) STDataManager *dataManager;
@property (nonatomic, weak) NSDateFormatter *timeDisplayFormatter;
@property (nonatomic, strong) NSManagedObjectContext *managedObjectContext;
@property (nonatomic) BOOL useRelativeTimes;

@end
