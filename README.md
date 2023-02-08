# media_extension

A Gallery Extension plugin by ente.io team.

* The plugin a app would need to implement a complete gallery app.
* Allows other apps to pick images from your gallery app.

* ##### About us
    * ente.io was born out of a need for a private storage solution for personal photos and videos. 
    * There was nothing out there that ticked all the boxes, so we started building one.

## Features
* Allows the app to identify and handle case of our app's invocation by other app via what kind of intent-action.
* By invoking `getIntentAction` method, the following intentActions can be identified.
    - `PICK`
    - `EDIT`
    - `VIEW`
* Provides `setResult` method to pass the uri of the selected image to the requesting app by creating a temporary copy of that image in the content-provider path.

## Implementation
<p align="center">
  <img src="https://user-images.githubusercontent.com/63253383/217319703-73916653-7222-4143-b2cf-6192a3671c61.png" />
</p>

* Native code invokes flutter code by invoking the flutter method from `onAttachedEngineMethod` in native code which returns the mode asynchronously and we use completer in dart code to get the status and set the mode of the app.

* When it is Gallery-Mode the Uri of the chosen image from the applications document storage directory and another method call is made to native code with the uri as the parameter.

* The received Uri we create another temp file in our cache-directory and granting other applications to read from that directory and once the `content://uri` (content provider) is created the result is sent to the requested activity via the intent.

## DataTypes Descriptions

  Types                |   Fields            | 
--------------------   | ------------------  | 
`IntentAction`  | enumeration of **main**, **pick**, **edit**, **view** |
`MediaExtentionAction`  | <p>**IntentAction** `action` [type of intent-action which invoked the app]</p> **String?** `uri` [uri of the image sent by other app with the intent]  |



## Method Descriptions

  Methods                |   Parameters                       |   Return
-----------------------  | -------------------------------    | ---------------
`getIntentAction`          |                                    |  **MediaExtentionAction**                                      | 
`setResult`  | <p>**String** `uri` [path of the file selected]</p>  | **void** 
`setAs`  | <p>**String** `uri` [path of the file selected]</p> **String** `mimeType` [mimeType of the file selected]   | **void**
`openWith`  | <p>**String** `uri` [path of the file selected]</p>**String** `mimeType` [mimeType of the file selected]   | **void**
`edit`  | <p>**String** `uri` [path of the file selected]</p>**String** `mimeType` [mimeType of the file selected]   | **void**

## Getting started
To use this plugin:
* Run the following command in terminal
  * ```
    flutter pub get media_extension
    ```
    Or
* Add the following in pubspec.yaml file
  * ```yml
    dependencies:
        flutter:
            sdk: flutter
        media_extension:   
     ```

* Add the following in android/src/main/res/xml/provider_paths.xml 
  ```xml
   <?xml version="1.0" encoding="utf-8"?>
   <paths>
        <external-path
            name="external_files"
            path="." />

        <cache-path
            name="embedded"
            path="." />
    </paths>
    ```

* Add the following in android/src/main/AndroidManifest.xml
  ```xml
    <application>
    
          <activity
            android:name=".MainActivity">

            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <data android:mimeType="image/*" />
            </intent-filter>

            <intent-filter>
                <action android:name="android.intent.action.PICK" />
                <category android:name="android.intent.category.DEFAULT" />
                <data android:mimeType="image/*" />
            </intent-filter>

            <intent-filter>
                <action android:name="android.intent.action.GET_CONTENT" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.OPENABLE" />
                <data android:mimeType="image/*" />
            </intent-filter>
        </activity>

    </application>
    ```
## Example
```dart
class MyAppState extends State<MyApp> {
  final imgUrl = "https://cdn-chat.sstatic.net/chat/img/404_funny_hats.jpg";
  IntentAction _intentAction = IntentAction.main;
  final _mediaExtensionPlugin = MediaExtension();
  final _downloadHelper = DownloadHelper(Dio());

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    IntentAction intentAction = IntentAction.main;
    try {
      final actionResult = await _mediaExtensionPlugin.getIntentAction();
      intentAction = actionResult.action!;
    } on PlatformException {
      intentAction = IntentAction.unknown;
    }
    if (!mounted) return;

    setState(() {
      _intentAction = intentAction;
    });
  }

  Future<String> _getLocalFile(String filename) async {
    var tempDir = await getTemporaryDirectory();
    String fullPath = "${tempDir.path}/image.jpg";
    await _downloadHelper.downloadToFile(imgUrl, fullPath);
    return fullPath;
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text('Intent Action is: $_intentAction\n'),
              FutureBuilder(
                  future: _getLocalFile("image.jpg"),
                  builder:
                      (BuildContext context, AsyncSnapshot<String> snapshot) {
                    return snapshot.data != null
                        ? GestureDetector(
                            child: Image.file(File(snapshot.data!)),
                            onTap: () async {
                              if (_intentAction == IntentAction.pick) {
                                _mediaExtensionPlugin
                                    .setResult("file://${snapshot.data!}");
                              }
                            },
                          )
                        : Container();
                  }),
              GestureDetector(
                child: Text(
                  'Set as',
                  style: Theme.of(context).textTheme.headline4,
                ),
                onTap: () async {
                  var tempDir = await getTemporaryDirectory();
                  String fullPath = "${tempDir.path}/image.jpg'";
                  await _downloadHelper.downloadToFile(imgUrl, fullPath);
                  try {
                    final isDOne = await _mediaExtensionPlugin.setAs(
                        "file://$fullPath", "image/*");
                    debugPrint("Is done $isDOne");
                  } on PlatformException {
                    debugPrint("Some error");
                  }
                },
              ),
              GestureDetector(
                child: Text(
                  'Edit',
                  style: Theme.of(context).textTheme.headline4,
                ),
                onTap: () async {
                  var tempDir = await getTemporaryDirectory();
                  String fullPath = "${tempDir.path}/image.jpg'";
                  await _downloadHelper.downloadToFile(imgUrl, fullPath);
                  try {
                    final isDOne = await _mediaExtensionPlugin.edit(
                        "file://$fullPath", "image/*");
                    debugPrint("Is done $isDOne");
                  } on PlatformException {
                    debugPrint("Some error");
                  }
                },
              ),
              GestureDetector(
                child: Text(
                  'Open With',
                  style: Theme.of(context).textTheme.headline4,
                ),
                onTap: () async {
                  var tempDir = await getTemporaryDirectory();
                  String fullPath = "${tempDir.path}/image.jpg'";
                  await _downloadHelper.downloadToFile(imgUrl, fullPath);
                  try {
                    final result = await _mediaExtensionPlugin.openWith(
                        "file://$fullPath", "image/*");
                    debugPrint("Is done $result");
                  } on PlatformException {
                    debugPrint("Some error");
                  }
                },
              ),
            ],
          ),
        ),
      ),
    );
  }
}          

```
## Issues/Upcoming Changes
* Implement `setResults` method for multiple selection of images for `ACTION_PICK` intent.

## Credits

This plugin wouldn't be possible without the following:
* [aves](https://github.com/deckerst/aves/) : Most of the methods in the plugin are inspired from this repository

## License
 * [MIT]('./LICENSE')

