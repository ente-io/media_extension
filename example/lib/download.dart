import 'dart:io';

import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';

class DownloadHelper {
  final Dio dio;

  DownloadHelper(this.dio);

  Future downloadToFile(String url, String savePath) async {
    try {
      Response response = await dio.get(
        url,
        //Received data with List<int>
        options: Options(
          responseType: ResponseType.bytes,
          followRedirects: false,
        ),
      );
      File file = File(savePath);
      var raf = file.openSync(mode: FileMode.write);
      // response.data is List<int> type
      raf.writeFromSync(response.data);
      await raf.close();
    } catch (e) {
      debugPrint(e.toString());
    }
  }
}
