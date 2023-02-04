enum IntentAction { main, pick, edit, view, unknown }

extension ParseToString on IntentAction {
  String toShortString() {
    return toString().split('.').last;
  }
}

IntentAction actionStringify(String actionString) {
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

class MediaExtentionAction {
  final IntentAction? action;
  final String? uri;
  MediaExtentionAction({
    this.action,
    this.uri,
  });
}
