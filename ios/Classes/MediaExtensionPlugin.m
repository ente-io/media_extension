#import "MediaExtensionPlugin.h"
#if __has_include(<media_extension/media_extension-Swift.h>)
#import <media_extension/media_extension-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "media_extension-Swift.h"
#endif

@implementation MediaExtensionPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftMediaExtensionPlugin registerWithRegistrar:registrar];
}
@end
