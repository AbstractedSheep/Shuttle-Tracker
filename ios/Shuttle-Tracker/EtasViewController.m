//
//  EtasViewController.m
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 2/20/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import "EtasViewController.h"
#import "EtaWrapper.h"
#import "DataManager.h"
#import "LaterEtasViewController.h"


@interface EtasViewController ()

- (void)delayedTableReload;
- (void)unsafeDelayedTableReload;

@end

@implementation EtasViewController


@synthesize dataManager;
@synthesize timeDisplayFormatter;


- (id)initWithStyle:(UITableViewStyle)style
{
    if ((self = [super initWithStyle:style])) {
		self.title = @"ETAs";
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
    //  One section for each route
	if (dataManager) {
		return [dataManager numberSections];
	} else {
		return 0;
	}
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    // Return the number of rows in the section.
	return [dataManager numberEtasForSection:section];
    
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
    }
    
    // Configure the cell...
    EtaWrapper *etaWrapped = nil;
    
    int counter = 0;
    
	NSArray *etas = [dataManager etasForSection:indexPath.section];
	
    //  Search for the correct EtaWrapper based on route (route 1 == section 0, route 2 == section 1)
    for (EtaWrapper *eta in etas) {
		if (counter == indexPath.row) {
			etaWrapped = eta;
			break;
		}
            
		counter++;
    }
    
    //  If the EtaWrapper was found, add the stop info and the ETA
    if (etaWrapped) {
		//	The main text label, left aligned and black in UITableViewCellStyleValue1
        cell.textLabel.text = etaWrapped.stopName;
		
		//	The secondary text label, right aligned and blue in UITableViewCellStyleValue1
		if (etaWrapped.eta) {
			cell.detailTextLabel.text = [timeDisplayFormatter stringFromDate:etaWrapped.eta];
		} else {
			cell.detailTextLabel.text = @"————";
		}
    }
    
    return cell;
}


//	Use the short names of the routes, since they display better than the full names
- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section {
	NSArray *sectionHeaders = [dataManager sectionHeaders];;
	
	if (sectionHeaders && [sectionHeaders count] > section) {
		return [sectionHeaders objectAtIndex:section];
	} else {
		return @"Unknown";
	}
}


#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
	EtaWrapper *etaWrapped = nil;
    
    int counter = 0;
    
	NSArray *etas = [dataManager etasForSection:indexPath.section];
	
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

- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath 
{
    // Return NO if you do not want the specified item to be editable.
    return YES;
}

- (NSString *)tableView:(UITableView *)tableView titleForDeleteConfirmationButtonForRowAtIndexPath:(NSIndexPath *)indexPath
{
	if ([dataManager isFavoritesSection:indexPath.section]) {
		return @"Unfavorite";
	} else {
		return @"Favorite";
	}
}


- (UITableViewCellEditingStyle)tableView:(UITableView *)tableView editingStyleForRowAtIndexPath:(NSIndexPath *)indexPath {
	if ([dataManager isFavoritesSection:indexPath.section]) {
		return UITableViewCellEditingStyleDelete;
	} else {
		return UITableViewCellEditingStyleInsert;
	}
}

- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
{
	//	If the cell's delete button, which is named "favorite" or "unfavorite",
	//	is pressed, then tell the data manager
	if (editingStyle == UITableViewCellEditingStyleDelete)
	{
		[dataManager toggleFavoriteEtaAtIndexPath:indexPath];
		
		//	If the last row in a section is going to be removed, just delete the section.
		//	Otherwise remove the row.
		if ([self.tableView numberOfRowsInSection:indexPath.section] == 1) {
			[self.tableView deleteSections:[NSIndexSet indexSetWithIndex:indexPath.section]
														withRowAnimation:UITableViewRowAnimationFade];
		} else {
			[self.tableView deleteRowsAtIndexPaths:[NSArray arrayWithObjects:indexPath, nil] 
								  withRowAnimation:UITableViewRowAnimationFade];
		}
		
		//	Reload the table
		[self delayedTableReload];
	} else if (editingStyle == UITableViewCellEditingStyleInsert) {
		[dataManager toggleFavoriteEtaAtIndexPath:indexPath];
		//	Reload the table
		[self delayedTableReload];
	}
}


//	Call unsafeDelayedTableReload on the main thread
- (void)delayedTableReload {
	[self performSelectorOnMainThread:@selector(unsafeDelayedTableReload) 
						   withObject:nil waitUntilDone:NO];
}


//	Reload the table on a short delay, usually for a data change
- (void)unsafeDelayedTableReload {
	[NSTimer scheduledTimerWithTimeInterval:0.125f 
									 target:self.tableView 
								   selector:@selector(reloadData) 
								   userInfo:nil 
									repeats:NO];
}


@end
