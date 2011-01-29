//
//  TokenManager.h
//  CloudMadeApi
//
//  Created by Dmytro Golub on 11/5/09.
//  Copyright 2010 CloudMade. All rights reserved.
//

#import <Foundation/Foundation.h>

//! Class provides interface for mobile users' authentication  \note { Token will be requested only once after that it will be saved on the disk and used next time }
@interface TokenManager : NSObject
{
	NSString* _accessToken;
	NSString* _apikey;
}

//! Token which is required for mobile devices. 
@property (nonatomic,readonly) NSString* accessToken;
//!! CloudMade apikey \warning { apikey must have 'Token Based Authentication' otherwise 403 HTTP code will be returned }
@property (nonatomic,readonly) NSString* accessKey;
/**
 *  Initializes class 
 *  @param apikey CloudMade apikey \sa http://www.cloudmade.com/faq#api_keys
 */
-(id) initWithApikey:(NSString*) apikey;
/**
 *   Requests token from authentication server. If server doesn't return HTTP 200 response <b>"RMCloudMadeAccessTokenRequestFailed"</b> notification will be sent   
 */
-(void) requestToken;
/**
 * Extend URL by token  
 * @param  URL which has to be extended by token
 */
-(NSString*) appendRequestWithToken:(NSString*) url;
+ (NSString*)pathForSavedAccessToken;

@end

