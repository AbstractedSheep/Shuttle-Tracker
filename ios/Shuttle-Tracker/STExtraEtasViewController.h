//
//  STLaterEtasViewController.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 4/30/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import <UIKit/UIKit.h>

@class STStop, STJSONParser, STDataManager, UINavigationButton;

@interface STExtraEtasViewController : UITableViewController {
    NSURL               *m_etasUrl;
    STJSONParser        *m_extraEtasParser;

    NSDate              *m_lastEtaRefresh;
    NSArray             *m_etas;

    STDataManager       *m_dataManager;
    NSTimer             *m_updateTimer;
    NSDateFormatter     *m_timeDisplayFormatter;
    
    UIBarButtonItem     *m_favoriteButton;
    
    BOOL                m_useRelativeTimes;
}

- (id)initWithStop:(STStop *)stop forRouteNumber:(NSNumber *)routeNumber;
- (void)toggleFavorite:(id)sender;

@property (nonatomic, retain) STStop *stop;
@property (nonatomic, retain) NSNumber *routeNum;
@property (strong, nonatomic) NSManagedObjectContext *managedObjectContext;
@property (nonatomic, assign) STDataManager *dataManager;
@property (nonatomic, assign) NSDateFormatter *timeDisplayFormatter;
@property (nonatomic) BOOL useRelativeTimes;


@end
