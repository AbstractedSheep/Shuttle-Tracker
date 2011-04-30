//
//  LaterEtasViewController.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 4/30/11.
//  Copyright 2011 Naga Softworks, LLC. All rights reserved.
//

#import <UIKit/UIKit.h>

@class EtaWrapper, JSONParser;

@interface LaterEtasViewController : UITableViewController {
    EtaWrapper *wrappedEta;
	
	NSURL *etasUrl;
	JSONParser *extraEtasParser;
	
	NSArray *etas;
	
	NSTimer *updateTimer;
	NSDateFormatter *timeDisplayFormatter;
}

- (id)initWithEta:(EtaWrapper *)eta;

@property (nonatomic, retain) EtaWrapper *wrappedEta;
@property (nonatomic, assign) NSDateFormatter *timeDisplayFormatter;


@end
