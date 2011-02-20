//
//  EtaViewController.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 2/16/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import <UIKit/UIKit.h>

@class DataManager;

@interface EtaViewController : UITableViewController <UITableViewDelegate> {
    DataManager *dataManager;
    UITableView *tableView;
}

@property (nonatomic, assign) DataManager *dataManager;


@end
