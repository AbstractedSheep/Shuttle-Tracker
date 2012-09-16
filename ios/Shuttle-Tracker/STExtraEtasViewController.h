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

    STDataManager       *__weak m_dataManager;
    NSTimer             *m_updateTimer;
    NSDateFormatter     *__weak m_timeDisplayFormatter;
    
    UIBarButtonItem     *m_favoriteButton;
    
    BOOL                m_useRelativeTimes;
}

- (id)initWithStop:(STStop *)stop forRouteNumber:(NSNumber *)routeNumber;
- (void)toggleFavorite:(id)sender;

@property (nonatomic, strong) STStop *stop;
@property (nonatomic, strong) NSNumber *routeNum;
@property (strong, nonatomic) NSManagedObjectContext *managedObjectContext;
@property (nonatomic, weak) STDataManager *dataManager;
@property (nonatomic, weak) NSDateFormatter *timeDisplayFormatter;
@property (nonatomic) BOOL useRelativeTimes;


@end
