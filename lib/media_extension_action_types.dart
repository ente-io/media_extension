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
  IntentAction action = IntentAction.pick;

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
    case 'MAIN':
      action = IntentAction.main;
      break;
    default:
      action = IntentAction.unknown;
  }
  return action;
}

/// MediaExtensionAction is custom data class
/// * IntentAction action: Intent Action Which Invoked the app.
/// * String uri: uri passed via the Intent.
class MediaExtentionAction {
  final IntentAction? action;
  final String? uri;
  MediaExtentionAction({
    this.action,
    this.uri,
  });
}
