//
//  LaterEtasViewController.m
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 4/30/11.
//  Copyright 2011 Naga Softworks, LLC. All rights reserved.
//

#import "LaterEtasViewController.h"
#import "DataUrls.h"
#import "EtaWrapper.h"
#import "JSONParser.h"


@interface LaterEtasViewController ()

- (void)getExtraEtas;
- (void)delayedTableReload;
- (void)unsafeDelayedTableReload;

@end


@implementation LaterEtasViewController

@synthesize wrappedEta;
@synthesize timeDisplayFormatter;

- (id)initWithEta:(EtaWrapper *)eta {
	if ((self = [self init])) {
		self.wrappedEta = eta;
		self.title = eta.stopName;
		
		etasUrl = [[NSURL alloc] initWithString:[NSString stringWithFormat:@"%@&rt=%d&st=%@", 
				   kLEExtraEtasUrl, eta.route, eta.stopId]];
		
		extraEtasParser = [[JSONParser alloc] initWithUrl:etasUrl];
	}
	
	return self;
}

- (id)initWithStyle:(UITableViewStyle)style
{
    self = [super initWithStyle:style];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)dealloc
{
	[wrappedEta release];
	[etasUrl release];
	[extraEtasParser release];
    [super dealloc];
}

- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}


- (void)getExtraEtas {
	dispatch_queue_t extraEtasQueue = dispatch_queue_create("com.abstractedsheep.extraetasqueue", NULL);
	dispatch_async(extraEtasQueue, ^{
        [extraEtasParser parseExtraEtas];
		[self performSelectorOnMainThread:@selector(delayedTableReload) withObject:nil waitUntilDone:NO];
	});
	
	dispatch_release(extraEtasQueue);
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];

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

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
	
	updateTimer = [NSTimer timerWithTimeInterval:15.0f target:self 
										selector:@selector(getExtraEtas) 
										userInfo:nil 
										 repeats:YES];
	[updateTimer retain];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
	
	[updateTimer invalidate];
	[updateTimer release];
}

- (void)viewDidDisappear:(BOOL)animated
{
    [super viewDidDisappear:animated];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    //	Return the number of sections.
	//	Only one section
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    // Return the number of rows in the section.
    return [etas count];
}


//	Use the name of the stop as the section header
- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section {
	return wrappedEta.stopName;
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
	static NSString *CellIdentifier = @"ExtraEtaCell";
    int row = indexPath.row;
	
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
		//	Init the cell such that it has main text, black and left aligned, and secondary text,
		//	blue and right aligned
        cell = [[[UITableViewCell alloc] initWithStyle:UITableViewCellStyleValue1 
									   reuseIdentifier:CellIdentifier] autorelease];
    }
    
    // Configure the cell...
    EtaWrapper *etaWrapped = nil;
    
	
	if (row < [etas count]) {
		etaWrapped = [etas objectAtIndex:row];
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
	
	cell.selectionStyle = UITableViewCellSelectionStyleNone;
	
	return cell;
}


#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    //	Navigation logic may go here. Create and push another view controller.
    //	Do nothing
}


//	Call unsafeDelayedTableReload on the main thread
- (void)delayedTableReload {
	[self performSelectorOnMainThread:@selector(unsafeDelayedTableReload) 
						   withObject:nil waitUntilDone:NO];
}


//	Reload the table on a short delay, usually for a data change
- (void)unsafeDelayedTableReload {
	if (etas) {
		[etas release];
	}
	
	etas = [extraEtasParser extraEtas];
	[etas retain];
	
	[NSTimer scheduledTimerWithTimeInterval:0.125f 
									 target:self.tableView 
								   selector:@selector(reloadData) 
								   userInfo:nil 
									repeats:NO];
}


@end
