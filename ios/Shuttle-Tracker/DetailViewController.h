//
//  DetailViewController.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 10/29/11.
//  Copyright (c) 2011 Naga Softworks, LLC. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface DetailViewController : UIViewController <UISplitViewControllerDelegate>

@property (strong, nonatomic) id detailItem;

@property (strong, nonatomic) IBOutlet UILabel *detailDescriptionLabel;

@end
