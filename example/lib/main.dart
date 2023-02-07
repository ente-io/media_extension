import 'dart:async';
import 'dart:io';

import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:media_extension_example/download.dart';
import 'package:path_provider/path_provider.dart';
import 'package:media_extension/media_extension_action_types.dart';

import 'package:media_extension/media_extension.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
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
