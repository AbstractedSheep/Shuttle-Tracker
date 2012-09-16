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
    STDataManager           *__weak m_dataManager;
    NSDateFormatter         *__weak m_timeDisplayFormatter;
    BOOL                    m_useRelativeTimes;
    NSMutableDictionary     *m_routeStops;
    NSTimer                 *m_freshTimer;
}

@property (nonatomic, weak) STDataManager *dataManager;
@property (nonatomic, weak) NSDateFormatter *timeDisplayFormatter;
@property (nonatomic) BOOL useRelativeTimes;
@property (strong, nonatomic) NSManagedObjectContext *managedObjectContext;

@end
