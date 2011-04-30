//
//  LaterEtasViewController.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 4/30/11.
//  Copyright 2011 Naga Softworks, LLC. All rights reserved.
//

#import <UIKit/UIKit.h>

@class EtaWrapper;

@interface LaterEtasViewController : UITableViewController {
    EtaWrapper *wrappedEta;
	NSString *etasUrl;
}

- (id)initWithEta:(EtaWrapper *)eta;

@property (nonatomic, retain) EtaWrapper *wrappedEta;

@end
