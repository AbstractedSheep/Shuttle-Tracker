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
#import "ExtraEtasViewController.h"
#import "IASKSettingsReader.h"
#import "ETA.h"
#import "Route.h"
#import "Stop.h"


@interface EtasViewController ()

- (void)delayedTableReload;
- (void)unsafeDelayedTableReload;
- (void)unsafeDelayedTableReloadForced;
- (void)settingChanged:(NSNotification *)notification;
- (void)notifyStopsUpdated:(NSNotification *)notification;
- (void)stopsUpdated:(NSNotification *)notification;

@end

@implementation EtasViewController


@synthesize dataManager;
@synthesize timeDisplayFormatter;
@synthesize useRelativeTimes;
@synthesize managedObjectContext = __managedObjectContext;


- (id)initWithStyle:(UITableViewStyle)style
{
    if ((self = [super initWithStyle:style])) {
        self.title = NSLocalizedString(@"ETAs", @"ETAs");
        self.contentSizeForViewInPopover = CGSizeMake(320, 600);
        
        NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
        self.useRelativeTimes = [[defaults objectForKey:@"useRelativeTimes"] boolValue];
        
        routeStops = [[NSMutableDictionary alloc] init];
        
        //	Take notice when routes and stops are loaded.
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(notifyStopsUpdated:)
                                                     name:kDMRoutesandStopsLoaded
                                                   object:nil];
        
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

//	A notification is sent by DataManager whenever stops are loaded.
//	Call the work function stopsUpdated on the main thread.
- (void)notifyStopsUpdated:(NSNotification *)notification {
	[self performSelectorOnMainThread:@selector(stopsUpdated:) withObject:notification waitUntilDone:NO];
}

//	A notification is sent by DataManager when the stops are loaded.
- (void)stopsUpdated:(NSNotification *)notification {
    //  Get all routes
    NSEntityDescription *entityDescription = [NSEntityDescription entityForName:@"Route"
                                                         inManagedObjectContext:self.managedObjectContext];
    NSFetchRequest *request = [[[NSFetchRequest alloc] init] autorelease];
    [request setEntity:entityDescription];
    
    NSError *error = nil;
    NSArray *dbRoutes = [self.managedObjectContext executeFetchRequest:request error:&error];
    if (dbRoutes == nil)
    {
        // Deal with error...
    } else {
        for (Route *route in dbRoutes) {
            //  Get all stops for that route
            NSSortDescriptor *sortDescriptor = [NSSortDescriptor sortDescriptorWithKey:@"stopNum" ascending:YES];
            
            [routeStops setValue:[[route.stops allObjects] sortedArrayUsingDescriptors:[NSArray arrayWithObject:sortDescriptor]] 
                          forKey:[route.routeId stringValue]];
        }
    }
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    // Return the number of sections.
    //  One section for each route (or "Loading...")
    
    return [routeStops count] + 1;
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
        NSArray *stopsArray = [routeStops objectForKey:[NSString stringWithFormat:@"%d", section]];
        
        if (stopsArray != nil) {
            rows = [stopsArray count]; 
        }
    }
    
    return rows;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *CellIdentifier = @"EtaCell";
    ETA *eta = nil;
    Stop *stop = nil;
    
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
        NSArray *stopsArray = [routeStops objectForKey:[NSString stringWithFormat:@"%d", indexPath.section]];
        
        if (stopsArray != nil && [stopsArray count] > indexPath.row) {
            stop = [stopsArray objectAtIndex:indexPath.row];
            NSEntityDescription *entityDescription = [NSEntityDescription entityForName:@"ETA"
                                                                 inManagedObjectContext:self.managedObjectContext];
            NSFetchRequest *request = [[[NSFetchRequest alloc] init] autorelease];
            [request setEntity:entityDescription];
            
            NSSortDescriptor *sortDescriptor = [NSSortDescriptor sortDescriptorWithKey:@"eta" ascending:NO];
            [request setSortDescriptors:[NSArray arrayWithObject:sortDescriptor]];
            
            [request setFetchLimit:1];
            
            NSPredicate *predicate = [NSPredicate predicateWithFormat:@"(routeId == %@) AND (stopId == %@)", [NSNumber numberWithInt:indexPath.section], stop.idTag];
            [request setPredicate:predicate];
            
            NSError *error = nil;
            NSArray *etas = [self.managedObjectContext executeFetchRequest:request error:&error];
            if ([etas count] > 0) {
                eta = [etas objectAtIndex:0];
            }
        }
    }
    
    // Configure the cell...
    
    //  If the EtaWrapper was found, add the stop info and the ETA
    if (eta) {
		//	The main text label, left aligned and black in UITableViewCellStyleValue1
        if (eta.stop != nil) {
            cell.textLabel.text = eta.stop.shortName;
        } else if (stop != nil) {
            cell.textLabel.text = stop.shortName;
        }
		
		//	The secondary text label, right aligned and blue in UITableViewCellStyleValue1
        //  Show the ETA, if it is in the future.
        int minutesToEta = 0;
        minutesToEta = (int)([eta.eta timeIntervalSinceNow] / 60);
        if (self.useRelativeTimes) {
            
            //  Grammar for one vs. more than one
            if (minutesToEta >= 1) {
                cell.detailTextLabel.text = [NSString stringWithFormat:@"%d min.", minutesToEta];
            }
            else if (minutesToEta < 1) {
                //  Cover our ears and show imminent and past times the same way
                cell.detailTextLabel.text = [NSString stringWithFormat:@"< 1 min."];
            }
        } else {
            //  Show ETAs as timestamps
            cell.detailTextLabel.text = [timeDisplayFormatter stringFromDate:eta.eta];
        }
    } else {
        if (stop != nil) {
            cell.textLabel.text = stop.shortName;
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
    
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"routeId == %@", [NSNumber numberWithInt:section]];
    [request setPredicate:predicate];
    
    NSError *error = nil;
    NSArray *routes = [self.managedObjectContext executeFetchRequest:request error:&error];
    if (error != nil)
    {
        // Deal with error...
    } else {
        return [[routes objectAtIndex:0] name];
    }
    
    return @"Loading...";
}


#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    Stop *stop = nil;
 
    //  Do nothing for favorites, for now
    if (indexPath.section == 0) {
        return;
    }
    
    NSArray *stopsArray = [routeStops objectForKey:[NSString stringWithFormat:@"%d", indexPath.section]];
    
    if (stopsArray != nil && [stopsArray count] > indexPath.row) {
        stop = [stopsArray objectAtIndex:indexPath.row];
    }
	
	ExtraEtasViewController *levc = [[ExtraEtasViewController alloc] initWithStop:stop forRouteNumber:[NSNumber numberWithInt:indexPath.section]];
    levc.managedObjectContext = self.managedObjectContext;
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
