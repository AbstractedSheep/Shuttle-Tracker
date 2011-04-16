//
//  EtasViewController.m
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 2/20/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import "EtasViewController.h"
#import "EtaWrapper.h"
#import "IASKSettingsReader.h"


@implementation EtasViewController


@synthesize dataManager;
@synthesize timeDisplayFormatter;


- (id)initWithStyle:(UITableViewStyle)style
{
    self = [super initWithStyle:style];
    if (self) {
        //	Take notice when a setting is changed
		//	Note that this is not the only object that takes notice.
		[[NSNotificationCenter defaultCenter] addObserver:self 
												 selector:@selector(settingChanged:) 
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

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
	
	[[NSNotificationCenter defaultCenter] addObserver:self.tableView 
											 selector:@selector(reloadData) 
												 name:kDMEtasUpdated 
											   object:nil];

    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
 
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
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
		return [dataManager numberRoutes];
	} else {
		return 0;
	}
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    // Return the number of rows in the section.
	return [dataManager numberEtasForRoute:section];
    
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
    
    //  Search for the correct EtaWrapper based on route (route 1 == section 0, route 2 == section 1)
    for (EtaWrapper *eta in [dataManager etasForRoute:indexPath.section + 1]) {
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
		cell.detailTextLabel.text = [timeDisplayFormatter stringFromDate:etaWrapped.eta];
    }

	//	The cell should not change in appearance when selected
    cell.selectionStyle = UITableViewCellSelectionStyleNone;
    
    return cell;
}


//	Use the short names of the routes, since they display better than the full names
- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section {
	NSArray *routeShortNames = dataManager.routeShortNames;
	
	if (routeShortNames && [routeShortNames count] > section) {
		return [routeShortNames objectAtIndex:section];
	} else {
		return @"Unknown";
	}
}


#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Navigation logic may go here. Create and push another view controller.
    /*
     <#DetailViewController#> *detailViewController = [[<#DetailViewController#> alloc] initWithNibName:@"<#Nib name#>" bundle:nil];
     // ...
     // Pass the selected object to the new view controller.
     [self.navigationController pushViewController:detailViewController animated:YES];
     [detailViewController release];
     */
}


//	Called by InAppSettingsKit whenever a setting is changed in the settings view inside the app.
//	Other objects may also do something when a setting is changed.
- (void)settingChanged:(NSNotification *)notification {
	if ([[notification object] isEqualToString:@"onlySoonestEtas"]) {
        [self.tableView reloadData];
    }
}


@end
