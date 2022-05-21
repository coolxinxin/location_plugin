#import <Flutter/Flutter.h>

#import <CoreLocation/CoreLocation.h>

@interface LocationPlugin : NSObject<FlutterPlugin>
@property (nonatomic, strong) CLGeocoder *geocoder;
@end
