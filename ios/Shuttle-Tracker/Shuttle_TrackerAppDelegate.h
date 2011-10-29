//
//  Shuttle_TrackerAppDelegate.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 3/28/11.
//  Copyright 2011 Brendon Justin, LLC. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface Shuttle_TrackerAppDelegate : UIResponder <UIApplicationDelegate> {

}

@property (nonatomic, retain) IBOutlet UIWindow *window;

@property (readonly, nonatomic) NSManagedObjectContext *managedObjectContext;
@property (readonly, nonatomic) NSManagedObjectModel *managedObjectModel;
@property (readonly, nonatomic) NSPersistentStoreCoordinator *persistentStoreCoordinator;

- (void)saveContext;
- (NSURL *)applicationDocumentsDirectory;

@end