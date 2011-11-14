//
//  AppDelegate.m
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 10/29/11.
//  Copyright (c) 2011 Brendon Justin. All rights reserved.
//

#import "AppDelegate.h"

#import "EtasViewController.h"
#import "MapViewController.h"
#import "IASKAppSettingsViewController.h"

#import "DataManager.h"


@implementation AppDelegate

@synthesize window = _window;
@synthesize managedObjectContext = __managedObjectContext;
@synthesize managedObjectModel = __managedObjectModel;
@synthesize persistentStoreCoordinator = __persistentStoreCoordinator;
@synthesize navigationController = _navigationController;
@synthesize splitViewController = _splitViewController;
@synthesize dataManager = _dataManager;
@synthesize tabBarController = _tabBarController;
@synthesize timeDisplayFormatter = _timeDisplayFormatter;
@synthesize dataUpdateTimer = _dataUpdateTimer;

- (void)dealloc
{
    [_window release];
    [__managedObjectContext release];
    [__managedObjectModel release];
    [__persistentStoreCoordinator release];
    [_navigationController release];
    [_splitViewController release];
    [super dealloc];
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    self.window = [[[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]] autorelease];
    
    // Override point for customization after application launch.
    DataManager *dataManager = [[DataManager alloc] init];
    self.dataManager = dataManager;
    [dataManager release];
    [self.dataManager setParserManagedObjectContext:self.managedObjectContext];
    
    //	dataManager creates a timeDisplayFormatter in its init method, so get
    //	a reference to it.
    self.timeDisplayFormatter = self.dataManager.timeDisplayFormatter;
    
    MapViewController *mapViewController = [[MapViewController alloc] init];
    mapViewController.tabBarItem = [[[UITabBarItem alloc] initWithTitle:@"Map" image:[UIImage imageNamed:@"glyphish_map"] tag:0] autorelease];
    mapViewController.dataManager = self.dataManager;
    mapViewController.managedObjectContext = self.managedObjectContext;
    
    UINavigationController *mapNavController = [[UINavigationController alloc] initWithRootViewController:mapViewController];
    [mapViewController release];
    
    EtasViewController *etasViewController = [[EtasViewController alloc] init];
    etasViewController.tabBarItem = [[[UITabBarItem alloc] initWithTitle:@"ETAs" image:[UIImage imageNamed:@"glyphish_clock"] tag:1] autorelease];
    etasViewController.dataManager = self.dataManager;
    etasViewController.managedObjectContext = self.managedObjectContext;
    
    UINavigationController *etasTableNavController = [[UINavigationController alloc] initWithRootViewController:etasViewController];
    [etasViewController release];
    
    //	Note that this class (MainViewController) gets a reference to timeDisplayFormatter
    //	via the class creating it.
    etasViewController.timeDisplayFormatter = self.timeDisplayFormatter;
    
    //  Device-specific view creation
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
        //  Make a split view, with ETAs on the left and the map on the right.
        self.splitViewController = [[[UISplitViewController alloc] init] autorelease];
        self.splitViewController.viewControllers = [NSArray arrayWithObjects:etasTableNavController, mapNavController, nil];
//        self.splitViewController.view.frame = self.window.frame;
        self.splitViewController.delegate = mapViewController;
        
        self.window.rootViewController = self.splitViewController;
    } else if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) {
        //  Create the settings view controller, only found on the iPhone
        IASKAppSettingsViewController *settingsViewController = [[IASKAppSettingsViewController alloc] initWithNibName:@"IASKAppSettingsView" bundle:nil];
        settingsViewController.title = @"Settings";
        settingsViewController.delegate = self.dataManager;
        settingsViewController.showDoneButton = NO;
        
        UINavigationController *settingsNavController = [[UINavigationController alloc] initWithRootViewController:settingsViewController];
        [settingsViewController release];
        settingsNavController.tabBarItem = [[[UITabBarItem alloc] initWithTitle:@"Settings" image:[UIImage imageNamed:@"glyphish_gear"] tag:2] autorelease];
        
        //  Create a tabbed view, with a map view, ETA view, and settings view.
        self.tabBarController = [[[UITabBarController alloc] init] autorelease];
        
        self.tabBarController.viewControllers = [NSArray arrayWithObjects:mapNavController, etasTableNavController, settingsNavController, nil];
        [settingsNavController release];
        
        self.window.rootViewController = self.tabBarController;
    }
    
    [mapNavController release];
    [etasTableNavController release];
    
    // Check if 12 or 24 hour mode
    BOOL use24Time = NO;
    
    NSDateFormatter *timeFormatter = [[NSDateFormatter alloc] init];
    [timeFormatter setTimeStyle:NSDateFormatterMediumStyle];
    
    NSMutableArray *dateArray = [[NSMutableArray alloc] init];
    [dateArray setArray:[[timeFormatter stringFromDate:[NSDate date]] componentsSeparatedByString:@" "]];
    
    if ([dateArray count] == 1) // if no am/pm extension exists
        use24Time = YES;
    
    [timeFormatter release];
    [dateArray release];
    
    //	Create an empty array to use for the favorite ETAs
    NSMutableArray *favoriteEtasArray = [NSMutableArray array];
    
    // Set the application defaults
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    NSDictionary *appDefaults;
    appDefaults = [NSDictionary dictionaryWithObjects:[NSArray arrayWithObjects:use24Time ? @"YES" : @"NO", 
                                                       @"NO", @"YES", [NSNumber numberWithInt:5], @"NO",
                                                       [NSKeyedArchiver archivedDataWithRootObject:favoriteEtasArray], nil]
                                              forKeys:[NSArray arrayWithObjects:@"use24Time", 
                                                       @"useLocation", @"findClosestStop", 
                                                       @"dataUpdateInterval", @"useRelativeTimes",
                                                       @"favoritesList", nil]];
    [defaults registerDefaults:appDefaults];
    [defaults synchronize];
    
    [[NSNotificationCenter defaultCenter] addObserver:self 
                                             selector:@selector(changeDataUpdateRate:)
                                                 name:@"dataUpdateInterval"
                                               object:nil];
    
	float updateInterval = [[defaults objectForKey:@"dataUpdateInterval"] floatValue];
	
	//	Schedule a timer to make the DataManager pull new data every 5 seconds
    self.dataUpdateTimer = [NSTimer scheduledTimerWithTimeInterval:updateInterval 
                                                            target:self.dataManager 
                                                          selector:@selector(updateData) 
                                                          userInfo:nil 
                                                           repeats:YES];
    
    [self.window makeKeyAndVisible];
    return YES;
}

- (void)applicationWillResignActive:(UIApplication *)application
{
    /*
     Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
     Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
     */
}

- (void)applicationDidEnterBackground:(UIApplication *)application
{
    /*
     Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later. 
     If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
     */
}

- (void)applicationWillEnterForeground:(UIApplication *)application
{
    /*
     Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
     */
}

- (void)applicationDidBecomeActive:(UIApplication *)application
{
    /*
     Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
     */
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
             
             abort() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development. 
             */
            NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
        } 
    }
}

#pragma mark - Local notification handler

- (void)changeDataUpdateRate:(NSNotification *)notification {
	//	Invalidate the timer so another can be made with a different interval.
	[self.dataUpdateTimer invalidate];
	
	NSDictionary *info = [notification userInfo];
	
	float updateInterval = [[info objectForKey:@"dataUpdateInterval"] floatValue];
	
	//	Schedule a timer to make the DataManager pull new data every 5 seconds
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
    NSDictionary *migrationOption = [NSDictionary dictionaryWithObjectsAndKeys:[NSNumber numberWithBool:YES], NSMigratePersistentStoresAutomaticallyOption, [NSNumber numberWithBool:YES], NSInferMappingModelAutomaticallyOption, nil];
    
    NSError *error = nil;
    __persistentStoreCoordinator = [[NSPersistentStoreCoordinator alloc] initWithManagedObjectModel:[self managedObjectModel]];
    if (![__persistentStoreCoordinator addPersistentStoreWithType:NSSQLiteStoreType configuration:nil URL:storeURL options:migrationOption error:&error])
    {
        /*
         Replace this implementation with code to handle the error appropriately.
         
         abort() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development. 
         
         Typical reasons for an error here include:
         * The persistent store is not accessible;
         * The schema for the persistent store is incompatible with current managed object model.
         Check the error message to determine what the actual problem was.
         
         
         If the persistent store is not accessible, there is typically something wrong with the file path. Often, a file URL is pointing into the application's resources directory instead of a writeable directory.
         
         If you encounter schema incompatibility errors during development, you can reduce their frequency by:
         * Simply deleting the existing store:
         [[NSFileManager defaultManager] removeItemAtURL:storeURL error:nil]
         
         * Performing automatic lightweight migration by passing the following dictionary as the options parameter: 
         [NSDictionary dictionaryWithObjectsAndKeys:[NSNumber numberWithBool:YES], NSMigratePersistentStoresAutomaticallyOption, [NSNumber numberWithBool:YES], NSInferMappingModelAutomaticallyOption, nil];
         
         Lightweight migration will only work for a limited set of schema changes; consult "Core Data Model Versioning and Data Migration Programming Guide" for details.
         
         */
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
