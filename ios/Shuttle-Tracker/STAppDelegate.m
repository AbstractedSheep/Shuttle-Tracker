//
//  STAppDelegate.m
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 10/29/11.
//  Copyright (c) 2011 Brendon Justin. All rights reserved.
//

#import "STAppDelegate.h"

#import "STEtasViewController.h"
#import "STMapViewController.h"

#import "STDataManager.h"


@implementation STAppDelegate

@synthesize managedObjectContext = __managedObjectContext;
@synthesize managedObjectModel = __managedObjectModel;
@synthesize persistentStoreCoordinator = __persistentStoreCoordinator;


- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    self.window = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
    
    // Override point for customization after application launch.
    STDataManager *dataManager = [[STDataManager alloc] init];
    self.dataManager = dataManager;
    [self.dataManager setParserManagedObjectContext:self.managedObjectContext];
    
    //  dataManager creates a timeDisplayFormatter in its init method, so get
    //  a reference to it.
    self.timeDisplayFormatter = self.dataManager.timeDisplayFormatter;
    
    STMapViewController *mapViewController = [[STMapViewController alloc] init];
    mapViewController.tabBarItem = [[UITabBarItem alloc] initWithTitle:@"Map"
                                                                 image:[UIImage imageNamed:@"glyphish_map"]
                                                                   tag:0];
    mapViewController.dataManager = self.dataManager;
    mapViewController.managedObjectContext = self.managedObjectContext;
    
    UINavigationController *mapNavController = [[UINavigationController alloc]
                                                initWithRootViewController:mapViewController];
    
    STEtasViewController *etasViewController = [[STEtasViewController alloc] init];
    etasViewController.tabBarItem = [[UITabBarItem alloc] initWithTitle:@"ETAs"
                                                                  image:[UIImage imageNamed:@"glyphish_clock"]
                                                                    tag:1];
    etasViewController.dataManager = self.dataManager;
    etasViewController.managedObjectContext = self.managedObjectContext;
    
    UINavigationController *etasTableNavController = [[UINavigationController alloc]
                                                      initWithRootViewController:etasViewController];
    
    //  Note that this class (MainViewController) gets a reference to timeDisplayFormatter
    //  via the class creating it.
    etasViewController.timeDisplayFormatter = self.timeDisplayFormatter;
    
    //  Device-specific view creation
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
        //  Make a split view, with ETAs on the left and the map on the right.
        self.splitViewController = [[UISplitViewController alloc] init];
        self.splitViewController.viewControllers = @[etasTableNavController, mapNavController];
        self.splitViewController.delegate = mapViewController;
        
        self.window.rootViewController = self.splitViewController;
    } else if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) {
        //  Create a tabbed view, with a map view, ETA view, and settings view.
        //        self.tabBarController = [[[UITabBarController alloc] init] autorelease];
        //
        //        self.tabBarController.viewControllers = @[mapNavController, etasTableNavController];
        //        self.window.rootViewController = self.tabBarController;
        self.window.rootViewController = mapNavController;
    }
    
    
    // Check if 12 or 24 hour mode
    BOOL use24Time = NO;
    
    NSDateFormatter *timeFormatter = [[NSDateFormatter alloc] init];
    [timeFormatter setTimeStyle:NSDateFormatterMediumStyle];
    
    NSMutableArray *dateArray = [[NSMutableArray alloc] init];
    [dateArray setArray:[[timeFormatter stringFromDate:[NSDate date]] componentsSeparatedByString:@" "]];
    
    if ([dateArray count] == 1) // if no am/pm extension exists
        use24Time = YES;
    
    
    //  Create an empty array to use for the favorite ETAs
    NSMutableArray *favoriteEtasArray = [NSMutableArray array];
    
    // Set the application defaults
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    NSDictionary *appDefaults;
    appDefaults = @{
        @"use24Time" : @(use24Time),
        @"findClosestStop" : @(YES),
        @"dataUpdateInterval" : @5,
        @"useRelativeTimes" : @(NO),
        @"favoritesList" : [NSKeyedArchiver archivedDataWithRootObject:favoriteEtasArray]
    };
    [defaults registerDefaults:appDefaults];
    [defaults synchronize];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(changeDataUpdateRate:)
                                                 name:@"dataUpdateInterval"
                                               object:nil];
    
    float updateInterval = [[defaults objectForKey:@"dataUpdateInterval"] floatValue];

    //  Schedule a timer to make the DataManager pull new data every 5 seconds
    self.dataUpdateTimer = [NSTimer scheduledTimerWithTimeInterval:updateInterval
                                                            target:self.dataManager
                                                          selector:@selector(updateData)
                                                          userInfo:nil
                                                           repeats:YES];
    
    [self.window makeKeyAndVisible];
    return YES;
}

- (void)applicationWillTerminate:(UIApplication *)application
{
    // Saves changes in the application's managed object context before the application terminates.
    [self saveContext];
}

- (void)saveContext
{
    NSError *error = nil;
    NSManagedObjectContext *managedObjectContext = self.managedObjectContext;
    if (managedObjectContext != nil)
    {
        if ([managedObjectContext hasChanges] && ![managedObjectContext save:&error])
        {
            /*
             Replace this implementation with code to handle the error appropriately.
             
             abort() causes the application to generate a crash log and terminate.
             You should not use this function in a shipping application, although
             it may be useful during development.
             */
            NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
        }
    }
}

#pragma mark - Local notification handler

- (void)changeDataUpdateRate:(NSNotification *)notification {
    //  Invalidate the timer so another can be made with a different interval.
    [self.dataUpdateTimer invalidate];

    NSDictionary *info = [notification userInfo];

    float updateInterval = [[info objectForKey:@"dataUpdateInterval"] floatValue];

    //  Schedule a timer to make the DataManager pull new data every 5 seconds
    self.dataUpdateTimer = [NSTimer scheduledTimerWithTimeInterval:updateInterval target:self.dataManager
                                                          selector:@selector(updateData)
                                                          userInfo:nil
                                                           repeats:YES];
}

#pragma mark - Core Data stack

/**
 Returns the managed object context for the application.
 If the context doesn't already exist, it is created and bound to the persistent store coordinator for the application.
 */
- (NSManagedObjectContext *)managedObjectContext
{
    if (__managedObjectContext != nil)
    {
        return __managedObjectContext;
    }
    
    NSPersistentStoreCoordinator *coordinator = [self persistentStoreCoordinator];
    if (coordinator != nil)
    {
        __managedObjectContext = [[NSManagedObjectContext alloc] init];
        [__managedObjectContext setPersistentStoreCoordinator:coordinator];
    }
    return __managedObjectContext;
}

/**
 Returns the managed object model for the application.
 If the model doesn't already exist, it is created from the application's model.
 */
- (NSManagedObjectModel *)managedObjectModel
{
    if (__managedObjectModel != nil)
    {
        return __managedObjectModel;
    }
    NSURL *modelURL = [[NSBundle mainBundle] URLForResource:@"Shuttle_Tracker" withExtension:@"momd"];
    __managedObjectModel = [[NSManagedObjectModel alloc] initWithContentsOfURL:modelURL];
    return __managedObjectModel;
}

/**
 Returns the persistent store coordinator for the application.
 If the coordinator doesn't already exist, it is created and the application's store added to it.
 */
- (NSPersistentStoreCoordinator *)persistentStoreCoordinator
{
    if (__persistentStoreCoordinator != nil)
    {
        return __persistentStoreCoordinator;
    }
    
    NSURL *storeURL = [[self applicationDocumentsDirectory] URLByAppendingPathComponent:@"Shuttle_Tracker.sqlite"];
    NSDictionary *migrationOption = @{ NSMigratePersistentStoresAutomaticallyOption: @(YES),
NSInferMappingModelAutomaticallyOption: @(YES) };
    
    NSError *error = nil;
    __persistentStoreCoordinator = [[NSPersistentStoreCoordinator alloc]
                                    initWithManagedObjectModel:[self managedObjectModel]];
    if (![__persistentStoreCoordinator addPersistentStoreWithType:NSSQLiteStoreType
                                                    configuration:nil
                                                              URL:storeURL
                                                          options:migrationOption
                                                            error:&error])
    {
        //  TODO: Do something useful here
        NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
    }
    
    return __persistentStoreCoordinator;
}

#pragma mark - Application's Documents directory

/**
 Returns the URL to the application's Documents directory.
 */
- (NSURL *)applicationDocumentsDirectory
{
    return [[[NSFileManager defaultManager] URLsForDirectory:NSDocumentDirectory inDomains:NSUserDomainMask] lastObject];
}

@end
