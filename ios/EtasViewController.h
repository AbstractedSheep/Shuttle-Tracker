//
//  EtasViewController.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 2/20/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "DataManager.h"


@interface EtasViewController : UITableViewController {
    DataManager *dataManager;
    
}

@property (nonatomic, assign) DataManager *dataManager;

@end
