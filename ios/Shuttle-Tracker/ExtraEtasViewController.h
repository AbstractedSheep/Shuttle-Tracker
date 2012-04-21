//
//  LaterEtasViewController.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 4/30/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import <UIKit/UIKit.h>

@class Stop, JSONParser, DataManager, UINavigationButton;

@interface ExtraEtasViewController : UITableViewController {
	NSURL               *m_etasUrl;
	JSONParser          *m_extraEtasParser;
	
    NSDate              *m_lastEtaRefresh;
	NSArray             *m_etas;
	
	DataManager         *m_dataManager;
	NSTimer             *m_updateTimer;
	NSDateFormatter     *m_timeDisplayFormatter;
    
    UIBarButtonItem     *m_favoriteButton;
    
    BOOL                m_useRelativeTimes;
}

- (id)initWithStop:(Stop *)stop forRouteNumber:(NSNumber *)routeNumber;
- (void)toggleFavorite:(id)sender;

@property (nonatomic, retain) Stop *stop;
@property (nonatomic, retain) NSNumber *routeNum;
@property (strong, nonatomic) NSManagedObjectContext *managedObjectContext;
@property (nonatomic, assign) DataManager *dataManager;
@property (nonatomic, assign) NSDateFormatter *timeDisplayFormatter;
@property (nonatomic) BOOL useRelativeTimes;


@end
