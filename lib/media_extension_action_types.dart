/// The Enum represents the IntentAction which as invoked the app.
///
/// * main: invoked normally by user
/// * pick: invoked by other app with ACTION_PICK Intent
/// * edit: invoked by other app with ACTION_EDIT Intent
/// * view: invoked by other app with ACTION_VIEW Intent
/// * unknown: when unexpected case as occured
enum IntentAction { main, pick, edit, view, unknown }

/// Method which extracts the value of the IntentAction as a string
///
/// Example:
/// ```dart
///  IntentAction action = IntentAction.main
///  print(action.toShortString());
/// ```
/// Output:
/// ```bash
///  pick
/// ```
extension ParseToString on IntentAction {
  String toShortString() => toString().split('.').last;
}

/// * Method which converts the string value to the IntentAction
/// * Used for converting the string from native code to dart type
///
/// Example:
///  ```dart
///   String actionString = 'MAIN'
///   IntentAction action = actionParser(actionString);
///   print(action);
///   ```
/// Output:
/// ```bash
///  intentAction.main
/// ```
IntentAction actionParser(String actionString) {
  IntentAction? action;

  switch (actionString) {
    case 'PICK':
      action = IntentAction.pick;
      break;
    case 'EDIT':
      action = IntentAction.edit;
      break;
    case 'VIEW':
      action = IntentAction.view;
      break;
    default:
      action = IntentAction.main;
  }

  return action;
}

/// MediaExtensionAction is custom data class
/// * IntentAction action: Intent Action Which Invoked the app.
/// * String uri: uri passed via the Intent.
class MediaExtentionAction {
  final IntentAction? action;
  final String? data;
  final MediaType? type;
  final String? extension;
  final String? name;

  MediaExtentionAction({
    this.name,
    this.type,
    this.extension,
    this.action,
    this.data,
  });
}

/// The Enum represents the MediaType of uri which as invoked the app.
///
/// * video
/// * image
enum MediaType {
  video,
  image,
}

/// * Method which converts the string value to the MediaType
/// * Used for converting the string from native code to dart type
///
/// Example:
///  ```dart
///   String actionString = 'video'
///   MediaType type = mediaParser(actionString);
///   print(type);
///   ```
/// Output:
/// ```bash
///  MediaType.video
/// ```
MediaType? mediaParser(String? mediaString) {
  MediaType? type;

  switch (mediaString) {
    case 'video':
      type = MediaType.video;
      break;
    case 'image':
      type = MediaType.image;
      break;
  }
  return type;
}
