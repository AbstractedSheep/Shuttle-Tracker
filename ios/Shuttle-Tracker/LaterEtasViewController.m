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
#import "DataManager.h"
#import "IASKSettingsReader.h"


@interface LaterEtasViewController ()

- (void)getExtraEtas;
- (void)delayedTableReload;
- (void)unsafeDelayedTableReload;
- (void)settingChanged:(NSNotification *)notification;

@end


@implementation LaterEtasViewController

@synthesize wrappedEta;
@synthesize dataManager;
@synthesize timeDisplayFormatter;
@synthesize useRelativeTimes;

- (id)initWithEta:(EtaWrapper *)eta {
	if ((self = [self initWithNibName:@"LaterEtasViewController" bundle:[NSBundle mainBundle]])) {
		self.wrappedEta = eta;
		self.title = eta.stopName;
		
		etasUrl = [[NSURL alloc] initWithString:[NSString stringWithFormat:@"%@&rt=%d&st=%@", 
				   kLEExtraEtasUrl, eta.route, eta.stopId]];
		
		extraEtasParser = [[JSONParser alloc] initWithUrl:etasUrl];
        
        NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
        self.useRelativeTimes = [[defaults objectForKey:@"useRelativeTimes"] boolValue];
        
        //	Set the size of the view when it is in a popover
        //  600 takes up about the whole vertical space when an iPad is horizontal
		CGSize size = {320, 600};
		self.contentSizeForViewInPopover = size;
        
        favoriteButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd
                                                                           target:self 
                                                                           action:@selector(toggleFavorite:)];
        self.navigationItem.rightBarButtonItem = favoriteButton;
        [favoriteButton release];
        
        //	Take notice when a setting is changed.
        //	Note that this is not the only object that takes notice.
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(settingChanged:) name:kIASKAppSettingChanged object:nil];
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
    
    //  Set the favorite/unfavorite button appearance
    //  based on the status of the view's associated stop.
    if ([dataManager isFavorite:wrappedEta]) {
        favoriteButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemStop 
                                                                       target:self 
                                                                       action:@selector(toggleFavorite:)];
    } else {
        favoriteButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd
                                                                       target:self 
                                                                       action:@selector(toggleFavorite:)];
    }
    
    self.navigationItem.rightBarButtonItem = favoriteButton;
    [favoriteButton release];
    
	updateTimer = [NSTimer timerWithTimeInterval:10.0f target:self 
										selector:@selector(getExtraEtas) 
										userInfo:nil 
										 repeats:YES];
	[updateTimer retain];
	
	[self getExtraEtas];

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

//	Handle the "Favorite/Unfavorite" button
- (void)toggleFavorite:(id)sender {
    if ([dataManager toggleFavoriteStopWithEta:wrappedEta]) {
        favoriteButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemStop 
                                                                       target:self 
                                                                       action:@selector(toggleFavorite:)];
    } else {
        favoriteButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd
                                                                       target:self 
                                                                       action:@selector(toggleFavorite:)];
    }
    
    self.navigationItem.rightBarButtonItem = favoriteButton;
    [favoriteButton release];
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
	//	If there are no etas, then return a Loading... cell
    return [etas count] ? ([etas count] > 6 ? 6 : [etas count]) : 1;
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
    
	//	If there are no etas, then return a Loading... cell
	
	if ([etas count] == 0  && indexPath.section == 0 && indexPath.row == 0) {
		//	The main text label, left aligned and black in UITableViewCellStyleValue1
		if (wrappedEta.eta == nil) {
			cell.textLabel.text = @"————";
		} else {
			cell.textLabel.text = @"Loading...";
		}
	} else if (row < [etas count]) {
		etaWrapped = [etas objectAtIndex:row];
		
		//  If the EtaWrapper was found, add the stop info and the ETA
		if (etaWrapped) {
			//	The main text label, left aligned and black in UITableViewCellStyleValue1
			cell.textLabel.text = [NSString stringWithFormat:@"ETA #: %i", indexPath.row + 1];
			
			//	The secondary text label, right aligned and blue in UITableViewCellStyleValue1
			if (etaWrapped.eta) {
                if (self.useRelativeTimes) {
                    int minutesToEta = 0;
                    minutesToEta = (int)([etaWrapped.eta timeIntervalSinceNow] / 60);
                    
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
                    cell.detailTextLabel.text = [timeDisplayFormatter stringFromDate:etaWrapped.eta];
                }
			} else {
				cell.detailTextLabel.text = @"————";
			}
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
