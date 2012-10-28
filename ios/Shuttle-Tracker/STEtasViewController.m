//
//  STEtasViewController.m
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 2/20/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

//  Consider consolidating ETA display code from this file and LaterEtasViewController

#import "STEtasViewController.h"
#import "STDataManager.h"
#import "STExtraEtasViewController.h"
#import "STETA.h"
#import "STFavoriteStop.h"
#import "STRoute.h"
#import "STStop.h"

const BOOL makeLaunchImage = NO;

@interface STEtasViewController ()

@property (strong, nonatomic) NSMutableDictionary *routeStops;
@property (strong, nonatomic) NSTimer *freshTimer;

- (void)delayedTableReload;
- (void)unsafeDelayedTableReload;
- (void)unsafeDelayedTableReloadForced;
- (void)notifyStopsUpdated:(NSNotification *)notification;
- (void)stopsUpdated:(NSNotification *)notification;

@end

@implementation STEtasViewController

@synthesize managedObjectContext = __managedObjectContext;


- (id)initWithStyle:(UITableViewStyle)style
{
    if ((self = [super initWithStyle:style])) {
        self.title = NSLocalizedString(@"ETAs", @"ETAs");
        self.contentSizeForViewInPopover = CGSizeMake(320, 600);
        
        NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
        self.useRelativeTimes = [[defaults objectForKey:@"useRelativeTimes"] boolValue];
        
        self.routeStops = [[NSMutableDictionary alloc] init];
        
        //  Take notice when routes and stops are loaded.
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(notifyStopsUpdated:)
                                                     name:kDMRoutesandStopsLoaded
                                                   object:nil];
    }
    
    return self;
}

- (id)init {
    self = [self initWithStyle:UITableViewStyleGrouped];
    
    return self;
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
    //  Take notice when the ETAs are updated
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(delayedTableReload)
                                                 name:kDMEtasUpdated
                                               object:nil];
    
    self.navigationItem.rightBarButtonItem = self.editButtonItem;
    [super viewDidLoad];
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (void)viewWillAppear:(BOOL)animated {
    self.freshTimer = [NSTimer scheduledTimerWithTimeInterval:10.0f
                                                       target:self
                                                     selector:@selector(delayedTableReload)
                                                     userInfo:nil
                                                      repeats:YES];
    
    [super viewWillAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated {
    [self.freshTimer invalidate];
    
    [super viewWillDisappear:animated];
}

//  A notification is sent by DataManager whenever stops are loaded.
//  Call the work function stopsUpdated on the main thread.
- (void)notifyStopsUpdated:(NSNotification *)notification {
    [self performSelectorOnMainThread:@selector(stopsUpdated:) withObject:notification waitUntilDone:NO];
}

//  A notification is sent by DataManager when the stops are loaded.
- (void)stopsUpdated:(NSNotification *)notification {
    //  Get all routes
    NSEntityDescription *entityDescription = [NSEntityDescription entityForName:NSStringFromClass([STRoute class])
                                                         inManagedObjectContext:self.managedObjectContext];
    NSFetchRequest *request = [[NSFetchRequest alloc] init];
    [request setEntity:entityDescription];
    
    NSError *error = nil;
    NSArray *dbRoutes = [self.managedObjectContext executeFetchRequest:request error:&error];
    if (dbRoutes == nil)
    {
        // Deal with error...
    } else {
        for (STRoute *route in dbRoutes) {
            //  Get all stops for that route
            NSSortDescriptor *sortDescriptor = [NSSortDescriptor sortDescriptorWithKey:@"stopNum" ascending:YES];
            
            [self.routeStops setValue:[[route.stops allObjects] sortedArrayUsingDescriptors:@[sortDescriptor]]
                               forKey:[route.routeId stringValue]];
        }
    }
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    // Return the number of sections.
    //  One section for each route (or "Loading...")
    if (makeLaunchImage) {
        return 1;
    }
    
    return [self.routeStops count] + 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    if (makeLaunchImage) {
        return 0;
    }
    
    int rows = 0;
    // Return the number of rows in the section.
    if (section == 0) {
        //  Get all favorite stops
        NSEntityDescription *entityDescription = [NSEntityDescription entityForName:NSStringFromClass([STFavoriteStop class])
                                                             inManagedObjectContext:self.managedObjectContext];
        NSFetchRequest *request = [[NSFetchRequest alloc] init];
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
        NSArray *stops = [self.routeStops objectForKey:[NSString stringWithFormat:@"%d", section]];
        
        if (stops != nil) {
            rows = [stops count];
        }
    }
    
    return rows;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *CellIdentifier = @"EtaCell";
    STETA *eta = nil;
    STStop *stop = nil;
    NSArray *favStops = nil, *etas = nil, *stopsArray = nil;
    NSError *error = nil;
    NSEntityDescription *entityDescription = nil;
    NSPredicate *predicate = nil;
    NSFetchRequest *request = nil;
    NSSortDescriptor *sortDescriptor = nil;
    int minutesToEta = 0;
    
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        //  Init the cell such that it has main text, black and left aligned, and secondary text,
        //  blue and right aligned
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleValue1
                                      reuseIdentifier:CellIdentifier];
        cell.accessoryType = UITableViewCellAccessoryDetailDisclosureButton;
    }
    
    if (indexPath.section == 0) {
        STFavoriteStop *favStop = nil;
        entityDescription = [NSEntityDescription entityForName:NSStringFromClass([STFavoriteStop class])
                                        inManagedObjectContext:self.managedObjectContext];
        request = [[NSFetchRequest alloc] init];
        [request setEntity:entityDescription];
        
        sortDescriptor = [NSSortDescriptor sortDescriptorWithKey:@"stop.name" ascending:NO];
        [request setSortDescriptors:@[sortDescriptor]];
        
        error = nil;
        favStops = [self.managedObjectContext executeFetchRequest:request error:&error];
        if ([favStops count] > indexPath.row) {
            favStop = [favStops objectAtIndex:indexPath.row];
            stop = favStop.stop;
            
            entityDescription = [NSEntityDescription entityForName:NSStringFromClass([STETA class])
                                            inManagedObjectContext:self.managedObjectContext];
            request = [[NSFetchRequest alloc] init];
            [request setEntity:entityDescription];
            
            sortDescriptor = [NSSortDescriptor sortDescriptorWithKey:@"eta" ascending:NO];
            [request setSortDescriptors:@[sortDescriptor]];
            
            [request setFetchLimit:1];
            
            predicate = [NSPredicate predicateWithFormat:@"(route.routeId == %@) AND (stop.idTag == %@)",
                         favStop.route.routeId, favStop.stop.idTag];
            [request setPredicate:predicate];
            
            error = nil;
            etas = [self.managedObjectContext executeFetchRequest:request error:&error];
            
            //  Get the next ETA that is in the future
            for (STETA *currentEta in etas) {
                minutesToEta = (int)([currentEta.eta timeIntervalSinceNow] / 60.0f);
                if (minutesToEta > -1) {
                    eta = currentEta;
                    break;
                }
            }
        }
    } else {
        stopsArray = [self.routeStops objectForKey:[NSString stringWithFormat:@"%d", indexPath.section]];
        
        if (stopsArray != nil && [stopsArray count] > indexPath.row) {
            stop = [stopsArray objectAtIndex:indexPath.row];
            entityDescription = [NSEntityDescription entityForName:NSStringFromClass([STETA class])
                                            inManagedObjectContext:self.managedObjectContext];
            request = [[NSFetchRequest alloc] init];
            [request setEntity:entityDescription];
            
            sortDescriptor = [NSSortDescriptor sortDescriptorWithKey:@"eta" ascending:NO];
            [request setSortDescriptors:@[sortDescriptor]];
            
            [request setFetchLimit:1];
            
            predicate = [NSPredicate predicateWithFormat:@"(route.routeId == %@) AND (stop.idTag == %@)",
                         @(indexPath.section), stop.idTag];
            [request setPredicate:predicate];
            
            error = nil;
            etas = [self.managedObjectContext executeFetchRequest:request error:&error];
            
            //  Get the next ETA that is in the future
            for (STETA *currentEta in etas) {
                minutesToEta = (int)([currentEta.eta timeIntervalSinceNow] / 60.0f);
                if (minutesToEta > -1) {
                    eta = currentEta;
                    break;
                }
            }
        }
    }
    
    // Configure the cell...
    
    //  If the ETA was found, add the stop info and the ETA
    if (eta) {
        //  The main text label, left aligned and black in UITableViewCellStyleValue1
        if (eta.stop != nil) {
            cell.textLabel.text = eta.stop.shortName;
        } else if (stop != nil) {
            cell.textLabel.text = stop.shortName;
        }
        
        //  The secondary text label, right aligned and blue in UITableViewCellStyleValue1
        //  The ETA is recently passed or still in the future, so show it to the user
        if (self.useRelativeTimes) {
            if (minutesToEta < 2) {
                //  If an ETA is recently passed, let it show as imminent
                cell.detailTextLabel.text = [NSString stringWithFormat:@"< 1 min."];
            } else {
                cell.detailTextLabel.text = [NSString stringWithFormat:@"%d min.", minutesToEta];
            }
        } else {
            //  Show ETAs as timestamps
            cell.detailTextLabel.text = [self.timeDisplayFormatter stringFromDate:eta.eta];
        }
    } else {
        if (stop != nil) {
            cell.textLabel.text = stop.shortName;
        }
        cell.detailTextLabel.text = @"————";
    }
    
    return cell;
}


//  Use the short names of the routes, since they display better than the full names
- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section {
    if (makeLaunchImage) {
        return @"Loading...";
    }
    
    if (section == 0) {
        return @"Favorites";
    }
    
    //  Get all routes
    NSEntityDescription *entityDescription = [NSEntityDescription entityForName:NSStringFromClass([STRoute class])
                                                         inManagedObjectContext:self.managedObjectContext];
    NSFetchRequest *request = [[NSFetchRequest alloc] init];
    [request setEntity:entityDescription];
    
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"routeId == %@",
                              @(section)];
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
    STStop *stop = nil;
    STExtraEtasViewController *levc = nil;
    
    //  Do nothing for favorites, for now
    if (indexPath.section == 0) {
        STFavoriteStop *favStop = nil;
        
        //  Get favorite stops
        NSEntityDescription *entityDescription = [NSEntityDescription entityForName:NSStringFromClass([STFavoriteStop class])
                                                             inManagedObjectContext:self.managedObjectContext];
        NSFetchRequest *request = [[NSFetchRequest alloc] init];
        [request setEntity:entityDescription];
        
        NSSortDescriptor *sortDescriptor = [NSSortDescriptor sortDescriptorWithKey:@"stop.name" ascending:NO];
        [request setSortDescriptors:@[sortDescriptor]];
        
        NSError *error = nil;
        NSArray *stops = [self.managedObjectContext executeFetchRequest:request error:&error];
        if ([stops count] > 0)
        {
            favStop = [stops objectAtIndex:indexPath.row];
            stop = favStop.stop;
            
            levc = [[STExtraEtasViewController alloc] initWithStop:stop forRouteNumber:favStop.route.routeId];
        }
    } else {
        NSArray *stops = [self.routeStops objectForKey:[NSString stringWithFormat:@"%d", indexPath.section]];
        
        if (stops != nil && [stops count] > indexPath.row) {
            stop = [stops objectAtIndex:indexPath.row];
            
            levc = [[STExtraEtasViewController alloc] initWithStop:stop
                                                    forRouteNumber:@(indexPath.section)];
        }
    }
    
    levc.managedObjectContext = self.managedObjectContext;
    levc.dataManager = self.dataManager;
    levc.timeDisplayFormatter = self.timeDisplayFormatter;
    
    // Pass the selected object to the new view controller.
    [self.navigationController pushViewController:levc animated:YES];
    
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
    //  If the cell is taken action on, either using the Insert button or the
    //  delete button, tell the data manager and update the table.
    if (editingStyle == UITableViewCellEditingStyleDelete)
    {
        //  Remove a favorite stop and delete the row from the table.
        
        //  First, get the favorite stop from the database, then delete it.
        STFavoriteStop*favStop = nil;
        
        NSEntityDescription *entityDescription = [NSEntityDescription entityForName:NSStringFromClass([STFavoriteStop class])
                                                             inManagedObjectContext:self.managedObjectContext];
        NSFetchRequest *request = [[NSFetchRequest alloc] init];
        [request setEntity:entityDescription];
        
        NSSortDescriptor *sortDescriptor = [NSSortDescriptor sortDescriptorWithKey:@"stop.name" ascending:NO];
        [request setSortDescriptors:@[sortDescriptor]];
        
        NSError *error = nil;
        NSArray *stops = [self.managedObjectContext executeFetchRequest:request error:&error];
        if ([stops count] > indexPath.row)
        {
            favStop = [stops objectAtIndex:indexPath.row];
            [self.managedObjectContext deleteObject:favStop];
        }
        
        //  Now remove the row from display
        [self.tableView deleteRowsAtIndexPaths:@[indexPath]
                              withRowAnimation:UITableViewRowAnimationFade];
    } else if (editingStyle == UITableViewCellEditingStyleInsert) {
        //  Add a favorite stop then reload the table.
        STFavoriteStop *favStop = nil;
        NSArray *stopsArray = [self.routeStops objectForKey:[NSString stringWithFormat:@"%d", indexPath.section]];
        
        if (stopsArray != nil && [stopsArray count] > indexPath.row) {
            NSEntityDescription *entityDescription = [NSEntityDescription entityForName:NSStringFromClass([STFavoriteStop class])
                                                                 inManagedObjectContext:self.managedObjectContext];
            NSFetchRequest *request = [[NSFetchRequest alloc] init];
            [request setEntity:entityDescription];
            
            NSPredicate *predicate = [NSPredicate predicateWithFormat:@"(stop.name == %@) AND (route.routeId == %@)",
                                      [[stopsArray objectAtIndex:indexPath.row] name],
                                      @(indexPath.section)];
            [request setPredicate:predicate];
            
            NSError *error = nil;
            int favStops = [self.managedObjectContext countForFetchRequest:request error:&error];
            if (favStops > 0) {
                //  The stop is already a favorite, so do nothing.
                return;
            } else {
                //  The stop is a new favorite, so add it.
                favStop = (STFavoriteStop *)[NSEntityDescription insertNewObjectForEntityForName:NSStringFromClass([STFavoriteStop class])
                                                                          inManagedObjectContext:self.managedObjectContext];
                
                entityDescription = [NSEntityDescription entityForName:NSStringFromClass([STRoute class])
                                                inManagedObjectContext:self.managedObjectContext];
                request = [[NSFetchRequest alloc] init];
                [request setEntity:entityDescription];
                
                [request setFetchLimit:1];
                
                predicate = [NSPredicate predicateWithFormat:@"(routeId == %@)", @(indexPath.section)];
                [request setPredicate:predicate];
                
                error = nil;
                NSArray *routes = [self.managedObjectContext executeFetchRequest:request error:&error];
                if ([routes count] > 0) {
                    favStop.route = [routes objectAtIndex:0];
                    favStop.stop = [stopsArray objectAtIndex:indexPath.row];
                    
                    NSLog(@"%@", favStop.stop);
                    // Save the context.
                    error = nil;
                    if (![self.managedObjectContext save:&error]) {
                        /*
                         Replace this implementation with code to handle the error appropriately.
                         
                         abort() causes the application to generate a crash log and terminate.
                         You should not use this function in a shipping application, although
                         it may be useful during development.
                         */
                        NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
                        
                        abort();
                    }
                }
            }
        }
        
        //  Reload the table
        [self unsafeDelayedTableReloadForced];
    }
}


//  Call unsafeDelayedTableReload on the main thread.  A threadsafe
//  way to call for a table reload.
- (void)delayedTableReload {
    [self performSelectorOnMainThread:@selector(unsafeDelayedTableReload)
                           withObject:nil
                        waitUntilDone:NO];
}


//  Reload the table on a short delay, usually for a data change.
//  Do not reload if the table is in editing mode.
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

//  Reload the table on a short delay, usually for a data change.
//  Reload regardless of the current table mode.
- (void)unsafeDelayedTableReloadForced {
    [NSTimer scheduledTimerWithTimeInterval:0.125f
                                     target:self.tableView
                                   selector:@selector(reloadData)
                                   userInfo:nil
                                    repeats:NO];
}

@end
