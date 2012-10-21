//
//  STLaterEtasViewController.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 4/30/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import <UIKit/UIKit.h>

@class STStop, STJSONParser, STDataManager, UINavigationButton;

@interface STExtraEtasViewController : UITableViewController

- (id)initWithStop:(STStop *)stop forRouteNumber:(NSNumber *)routeNumber;
- (void)toggleFavorite:(id)sender;

@property (nonatomic, strong) STStop *stop;
@property (nonatomic, strong) NSNumber *routeNum;
@property (nonatomic, strong) NSManagedObjectContext *managedObjectContext;
@property (nonatomic, weak) STDataManager *dataManager;
@property (nonatomic, weak) NSDateFormatter *timeDisplayFormatter;
@property (nonatomic) BOOL useRelativeTimes;


@end
