//
//  EtasViewController.m
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 2/20/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

//  Consider consolidating ETA display code from this file and LaterEtasViewController

#import "EtasViewController.h"
#import "EtaWrapper.h"
#import "DataManager.h"
#import "LaterEtasViewController.h"
#import "IASKSettingsReader.h"
#import "ETA.h"
#import "Route.h"
#import "Stop.h"


@interface EtasViewController ()

- (void)delayedTableReload;
- (void)unsafeDelayedTableReload;
- (void)unsafeDelayedTableReloadForced;
- (void)settingChanged:(NSNotification *)notification;

@end

@implementation EtasViewController


@synthesize dataManager;
@synthesize timeDisplayFormatter;
@synthesize useRelativeTimes;
@synthesize fetchedResultsController = __fetchedResultsController;
@synthesize managedObjectContext = __managedObjectContext;


- (id)initWithStyle:(UITableViewStyle)style
{
    if ((self = [super initWithStyle:style])) {
        self.title = NSLocalizedString(@"ETAs", @"ETAs");
        self.contentSizeForViewInPopover = CGSizeMake(320, 600);
        
        NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
        self.useRelativeTimes = [[defaults objectForKey:@"useRelativeTimes"] boolValue];
        
        //	Take notice when a setting is changed.
        //	Note that this is not the only object that takes notice.
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(settingChanged:)
                                                     name:kIASKAppSettingChanged
                                                   object:nil];
    }
	
    return self;
}

- (id)init {
    self = [self initWithStyle:UITableViewStyleGrouped];
    
    return self;
}

- (void)dealloc
{
    [super dealloc];
}

- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

//  Only support landscape on the iPad, and only support portrait on the iPhone
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation {
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
        return YES;
    } else {
        return (toInterfaceOrientation == UIInterfaceOrientationPortrait);
    }
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
	
	//	Take notice when the ETAs are updated
	[[NSNotificationCenter defaultCenter] addObserver:self 
											 selector:@selector(delayedTableReload) 
												 name:kDMEtasUpdated 
											   object:nil];

    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
 
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    self.navigationItem.rightBarButtonItem = self.editButtonItem;
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    // Return the number of sections.
    //  One section for each route (or "Loading...")
    //  Get all routes
    NSEntityDescription *entityDescription = [NSEntityDescription entityForName:@"Route"
                                                         inManagedObjectContext:self.managedObjectContext];
    NSFetchRequest *request = [[[NSFetchRequest alloc] init] autorelease];
    [request setEntity:entityDescription];
    
    NSError *error = nil;
    NSInteger numRoutes = [self.managedObjectContext countForFetchRequest:request error:&error];
    if (error != nil)
    {
        // Deal with error...
    } else {
        return numRoutes + 1;
    }
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    int rows = 0;
    // Return the number of rows in the section.
    if (section == 0) {
        //  Get all favorite stops
        NSEntityDescription *entityDescription = [NSEntityDescription entityForName:@"FavoriteStop" 
                                                             inManagedObjectContext:self.managedObjectContext];
        NSFetchRequest *request = [[[NSFetchRequest alloc] init] autorelease];
        [request setEntity:entityDescription];
        
        NSError *error = nil;
        NSInteger numStops = [self.managedObjectContext countForFetchRequest:request error:&error];
        if (error != nil)
        {
            // Deal with error...
        } else {
            rows = numStops;
        }
    } else {
        //  Get all stops for a route
        NSEntityDescription *entityDescription = [NSEntityDescription entityForName:@"Stop" 
                                                             inManagedObjectContext:self.managedObjectContext];
        NSFetchRequest *request = [[[NSFetchRequest alloc] init] autorelease];
        [request setEntity:entityDescription];
        
        // Set predicate and sort orderings...
        NSPredicate *predicate = [NSPredicate predicateWithFormat:@"ANY routes.routeId == %@", [NSNumber numberWithInt:section]];
        
        [request setPredicate:predicate];
        
        NSError *error = nil;
        NSInteger numStops = [self.managedObjectContext countForFetchRequest:request error:&error];
        if (error != nil)
        {
            // Deal with error...
        } else {
            rows = numStops;
        }
        
    }
    
    return rows;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *CellIdentifier = @"EtaCell";
    
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
		//	Init the cell such that it has main text, black and left aligned, and secondary text,
		//	blue and right aligned
        cell = [[[UITableViewCell alloc] initWithStyle:UITableViewCellStyleValue1 
									   reuseIdentifier:CellIdentifier] autorelease];
        cell.accessoryType = UITableViewCellAccessoryDetailDisclosureButton;
    }
    
    if (indexPath.section == 0) {
        //  Do something
    } else {
        //  Do something else
    }
    
    // Configure the cell...
    ETA *eta = nil;
    
    //  If the EtaWrapper was found, add the stop info and the ETA
    if (eta) {
		//	The main text label, left aligned and black in UITableViewCellStyleValue1
        cell.textLabel.text = eta.stopName;
		
		//	The secondary text label, right aligned and blue in UITableViewCellStyleValue1
        //  Show the ETA, if it is in the future.
        int minutesToEta = 0;
        minutesToEta = (int)([eta.eta timeIntervalSinceNow] / 60);
		if (minutesToEta > 0) {
            if (self.useRelativeTimes) {
                
                //  Grammar for one vs. more than one
                if (minutesToEta > 1) {
                    cell.detailTextLabel.text = [NSString stringWithFormat:@"%d minutes", minutesToEta];
                }
                else if (minutesToEta == 1) {
                    cell.detailTextLabel.text = [NSString stringWithFormat:@"%d minute", minutesToEta];
                }
                else if (minutesToEta < 1) {
                    //  Cover our ears and show imminent and past times the same way
                    cell.detailTextLabel.text = [NSString stringWithFormat:@"< 1 minute"];
                }
            } else {
                //  Show ETAs as timestamps
                cell.detailTextLabel.text = [timeDisplayFormatter stringFromDate:eta.eta];
            }
		} else {
			cell.detailTextLabel.text = @"————";
		}
    }
    
    return cell;
}


//	Use the short names of the routes, since they display better than the full names
- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section {
    if (section == 0) {
        return @"Favorites";
    }
    
    //  Get all routes
    NSEntityDescription *entityDescription = [NSEntityDescription entityForName:@"Route"
                                                         inManagedObjectContext:self.managedObjectContext];
    NSFetchRequest *request = [[[NSFetchRequest alloc] init] autorelease];
    [request setEntity:entityDescription];
    
    NSSortDescriptor *sortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"name" ascending:YES];
    [request setSortDescriptors:[NSArray arrayWithObject:sortDescriptor]];
    [sortDescriptor release];
    
    NSError *error = nil;
    NSArray *routes = [self.managedObjectContext executeFetchRequest:request error:&error];
    if (error != nil)
    {
        // Deal with error...
    } else {
        if (section <= [routes count]) {
            return [[routes objectAtIndex:section - 1] name];
        } else {
            return @"Loading...";
        }
    }
    
    return @"Loading...";
}


#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
	EtaWrapper *etaWrapped = nil;
    
    int counter = 0;
    
	NSArray *etas = [NSArray array];
	
    //  Search for the correct EtaWrapper based on route (route 1 == section 0, route 2 == section 1)
    for (EtaWrapper *eta in etas) {
		if (counter == indexPath.row) {
			etaWrapped = eta;
			break;
		}
		
		counter++;
    }
	
	LaterEtasViewController *levc = [[LaterEtasViewController alloc] initWithEta:etaWrapped];
	levc.dataManager = dataManager;
	levc.timeDisplayFormatter = timeDisplayFormatter;
	
	// Pass the selected object to the new view controller.
	[self.navigationController pushViewController:levc animated:YES];
	[levc release];
	
	[self.tableView deselectRowAtIndexPath:indexPath animated:YES];
}

- (void)tableView:(UITableView *)tableView accessoryButtonTappedForRowWithIndexPath:(NSIndexPath *)indexPath {
    [self tableView:tableView didSelectRowAtIndexPath:indexPath];
}

- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath 
{
    // Return NO if you do not want the specified item to be editable.
    return YES;
}

- (NSString *)tableView:(UITableView *)tableView titleForDeleteConfirmationButtonForRowAtIndexPath:(NSIndexPath *)indexPath
{
	if (indexPath.section == 0) {
		return @"Unfavorite";
	} else {
		return @"Favorite";
	}
}


- (UITableViewCellEditingStyle)tableView:(UITableView *)tableView editingStyleForRowAtIndexPath:(NSIndexPath *)indexPath {
	if (indexPath.section == 0) {
		return UITableViewCellEditingStyleDelete;
	} else {
		return UITableViewCellEditingStyleInsert;
	}
}

- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
{
	//	If the cell is taken action on, either using the Insert button or the
	//	delete button, tell the data manager and update the table.
	if (editingStyle == UITableViewCellEditingStyleDelete)
	{
		//	Remove a favorite stop: delete the row from the table.
        //  If it was the last row, delete the whole favorites section.
		
//		[dataManager toggleFavoriteEtaAtIndexPath:indexPath];
		
		//	If the last row in a section is going to be removed, just delete the section.
		//	Otherwise remove the row.
		if ([self.tableView numberOfRowsInSection:indexPath.section] == 1) {
			[self.tableView deleteSections:[NSIndexSet indexSetWithIndex:indexPath.section]
														withRowAnimation:UITableViewRowAnimationFade];
		} else {
			[self.tableView deleteRowsAtIndexPaths:[NSArray arrayWithObjects:indexPath, nil] 
								  withRowAnimation:UITableViewRowAnimationFade];
		}
	} else if (editingStyle == UITableViewCellEditingStyleInsert) {
		//	Add a favorite stop: reload the table.
		
//		[dataManager toggleFavoriteEtaAtIndexPath:indexPath];
		
		//	Reload the table
		[self unsafeDelayedTableReloadForced];
	}
}

#pragma mark - Fetched results controller

- (NSFetchedResultsController *)fetchedResultsController
{
    if (__fetchedResultsController != nil) {
        return __fetchedResultsController;
    }
    
    // Set up the fetched results controller.
    // Create the fetch request for the entity.
    NSFetchRequest *fetchRequest = [[[NSFetchRequest alloc] init] autorelease];
    // Edit the entity name as appropriate.
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"Event" inManagedObjectContext:self.managedObjectContext];
    [fetchRequest setEntity:entity];
    
    // Set the batch size to a suitable number.
    [fetchRequest setFetchBatchSize:20];
    
    // Edit the sort key as appropriate.
    NSSortDescriptor *sortDescriptor = [[[NSSortDescriptor alloc] initWithKey:@"timeStamp" ascending:NO] autorelease];
    NSArray *sortDescriptors = [NSArray arrayWithObjects:sortDescriptor, nil];
    
    [fetchRequest setSortDescriptors:sortDescriptors];
    
    // Edit the section name key path and cache name if appropriate.
    // nil for section name key path means "no sections".
    NSFetchedResultsController *aFetchedResultsController = [[[NSFetchedResultsController alloc] initWithFetchRequest:fetchRequest
managedObjectContext:self.managedObjectContext sectionNameKeyPath:nil cacheName:@"Master"] autorelease];
    aFetchedResultsController.delegate = self;
    self.fetchedResultsController = aFetchedResultsController;
    
	NSError *error = nil;
	if (![self.fetchedResultsController performFetch:&error]) {
	    /*
	     Replace this implementation with code to handle the error appropriately.
         
	     abort() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development. 
	     */
	    NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
	    abort();
	}
    
    return __fetchedResultsController;
}

- (void)controllerDidChangeContent:(NSFetchedResultsController *)controller
{
    // In the simplest, most efficient, case, reload the table view.
    [self.tableView reloadData];
}

//	Call unsafeDelayedTableReload on the main thread.  A threadsafe
//	way to call for a table reload.
- (void)delayedTableReload {
	[self performSelectorOnMainThread:@selector(unsafeDelayedTableReload) 
						   withObject:nil waitUntilDone:NO];
}


//	Reload the table on a short delay, usually for a data change.
//	Do not reload if the table is in editing mode.
- (void)unsafeDelayedTableReload {
	if ([self.tableView isEditing]) {
		return;
	} else {
		[NSTimer scheduledTimerWithTimeInterval:0.125f 
										 target:self.tableView 
									   selector:@selector(reloadData) 
									   userInfo:nil 
										repeats:NO];
	}
}

//	Reload the table on a short delay, usually for a data change.
//	Reload regardless of the current table mode.
- (void)unsafeDelayedTableReloadForced {
	[NSTimer scheduledTimerWithTimeInterval:0.125f 
									 target:self.tableView 
								   selector:@selector(reloadData) 
								   userInfo:nil 
									repeats:NO];
}

//	InAppSettingsKit sends out a notification whenever a setting is changed in the settings view inside the app.
//	settingChanged handles switching between absolute and relative times for updates and ETAs.
//	Other objects may also do something when a setting is changed.
- (void)settingChanged:(NSNotification *)notification {
	NSDictionary *info = [notification userInfo];
	
	//	Set the date format to 24 hour time if the user has set Use 24 Hour Time to true.
	if ([[notification object] isEqualToString:@"useRelativeTimes"]) {
		if ([[info objectForKey:@"useRelativeTimes"] boolValue]) {
            self.useRelativeTimes = YES;
		} else {
            self.useRelativeTimes = NO;
		}
	}
}

@end
