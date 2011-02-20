//
//  EtaManager.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 2/20/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface DataManager : NSObject <UITableViewDataSource> {
    NSArray *ETAs;
}

@property (nonatomic, retain) NSArray *ETAs;


@end
