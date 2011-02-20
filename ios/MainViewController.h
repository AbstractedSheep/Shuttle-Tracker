//
//  MainViewController.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 2/20/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "EtaManager.h"


@interface MainViewController : UIViewController {
    EtaManager *etaManager;
    
}

@property (nonatomic, retain) EtaManager *etaManager;


@end
