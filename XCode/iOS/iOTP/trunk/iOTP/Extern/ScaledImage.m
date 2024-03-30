//
//  UIImage.m
//  InfoCertID
//
//  Created by Giuseppe Cattaneo on 25/11/15.
//  Copyright © 2015 Giuseppe Cattaneo. All rights reserved.
//

#import "ScaledImage.h"

@implementation UIImage (scale)

/**
 * Scales an image to fit within a bounds with a size governed by
 * the passed size. Also keeps the aspect ratio.
 *
 * Switch MIN to MAX for aspect fill instead of fit.
 *
 * @param newSize the size of the bounds the image must fit within.
 * @return a new scaled image.
 */
- (UIImage *)scaleImageToSize:(CGSize)newSize {
    
    CGRect scaledImageRect = CGRectZero;
    
    CGFloat aspectWidth = newSize.width / self.size.width;
    CGFloat aspectHeight = newSize.height / self.size.height;
    CGFloat aspectRatio = MIN ( aspectWidth, aspectHeight );
    
    scaledImageRect.size.width = self.size.width * aspectRatio;
    scaledImageRect.size.height = self.size.height * aspectRatio;
    scaledImageRect.origin.x = (newSize.width - scaledImageRect.size.width) / 2.0f;
    scaledImageRect.origin.y = (newSize.height - scaledImageRect.size.height) / 2.0f;
    
    UIGraphicsBeginImageContextWithOptions( newSize, NO, 0 );
    [self drawInRect:scaledImageRect];
    UIImage* scaledImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    return scaledImage;
    
}

- (UIImage *)scaleImageToHeight:(float) height {
    
    CGSize newSize;
    CGRect scaledImageRect = CGRectZero;
    
    CGFloat aspectRatio = height / self.size.height;
    
    newSize.width = self.size.width * aspectRatio;
    newSize.height = height;
    
    scaledImageRect.size.width = self.size.width * aspectRatio;
    scaledImageRect.size.height = height;
    scaledImageRect.origin.x = (newSize.width - scaledImageRect.size.width) / 2.0f;
    // scaledImageRect.origin.y = (newSize.height - scaledImageRect.size.height) / 2.0f; // la y non si muove
    
    UIGraphicsBeginImageContextWithOptions( newSize, NO, 0 );
    [self drawInRect:scaledImageRect];
    UIImage* scaledImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    return scaledImage;
    
}

@end
