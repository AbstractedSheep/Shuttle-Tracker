//
//  ShuttleMarker.m
//  Shuttle Tracker
//
//  Created by Brendon Justin on 1/27/11.
//  Copyright 2011 Naga Softworks, LLC. All rights reserved.
//

#import "ShuttleMarker.h"


@implementation ShuttleMarker


- (id)initWithFrame:(CGRect)frame {
    
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code.
		self.image = [UIImage imageNamed:@"shuttle_icon.png"];
    }
    return self;
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code.
}
*/

- (void)dealloc {
    [super dealloc];
}


@end
