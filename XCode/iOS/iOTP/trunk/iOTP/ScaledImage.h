//
//  UIImage.h
//  InfoCertID
//
//  Created by Giuseppe Cattaneo on 25/11/15.
//  Copyright © 2015 Giuseppe Cattaneo. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface UIImage (scale)

- (UIImage *)scaleImageToSize:(CGSize)newSize;

- (UIImage *)scaleImageToHeight:(float) height;

@end
