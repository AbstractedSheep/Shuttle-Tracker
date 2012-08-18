//
//  STEtasViewController.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 2/20/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "STDataManager.h"


@interface STEtasViewController : UITableViewController <NSFetchedResultsControllerDelegate> {
    STDataManager           *m_dataManager;
    NSDateFormatter         *m_timeDisplayFormatter;
    BOOL                    m_useRelativeTimes;
    NSMutableDictionary     *m_routeStops;
    NSTimer                 *m_freshTimer;
}

@property (nonatomic, assign) STDataManager *dataManager;
@property (nonatomic, assign) NSDateFormatter *timeDisplayFormatter;
@property (nonatomic) BOOL useRelativeTimes;
@property (strong, nonatomic) NSManagedObjectContext *managedObjectContext;

@end
