## 0.0.1

- Features: 
    - The plugin detects the android `Intent-Action` used for invoking the app.

    - Communicates with the native thread and recieves the intent information when the flutter engine is attached.

    - Provides methods to send back result images for app invoked via `ACTION_PICK` Intent.

    - Provides additional methods such as openWith and edit to open and proccess the image in other apps which can handle `ACTION_VIEW` and `ACTION_EDIT` Intent.
