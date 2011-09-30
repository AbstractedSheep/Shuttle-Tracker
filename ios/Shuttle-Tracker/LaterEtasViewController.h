//
//  LaterEtasViewController.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 4/30/11.
//  Copyright 2011 Naga Softworks, LLC. All rights reserved.
//

#import <UIKit/UIKit.h>

@class EtaWrapper, JSONParser, DataManager, UINavigationButton;

@interface LaterEtasViewController : UITableViewController {
    EtaWrapper *wrappedEta;
	
	NSURL *etasUrl;
	JSONParser *extraEtasParser;
	
	NSArray *etas;
	
	DataManager *dataManager;
	NSTimer *updateTimer;
	NSDateFormatter *timeDisplayFormatter;
    
    UIBarButtonItem *favoriteButton;
    
    BOOL useRelativeTimes;
}

- (id)initWithEta:(EtaWrapper *)eta;
- (void)toggleFavorite:(id)sender;

@property (nonatomic, retain) EtaWrapper *wrappedEta;
@property (nonatomic, assign) DataManager *dataManager;
@property (nonatomic, assign) NSDateFormatter *timeDisplayFormatter;
@property (nonatomic) BOOL useRelativeTimes;


@end
