//
//  LaterEtasViewController.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 4/30/11.
//  Copyright 2011 Naga Softworks, LLC. All rights reserved.
//

#import <UIKit/UIKit.h>

@class Stop, JSONParser, DataManager, UINavigationButton;

@interface ExtraEtasViewController : UITableViewController {
	NSURL *etasUrl;
	JSONParser *extraEtasParser;
	
	NSArray *etas;
	
	DataManager *dataManager;
	NSTimer *updateTimer;
	NSDateFormatter *timeDisplayFormatter;
    
    UIBarButtonItem *favoriteButton;
    
    BOOL useRelativeTimes;
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
