package io.ente.photos.media_extension

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.annotation.NonNull
import androidx.core.content.FileProvider
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import android.os.*
import java.io.*
import java.util.*
import android.graphics.*

/*
CREDITS:
Almost all the code in the plugin is pulled out from Aves Gallery
https://github.com/deckerst/aves/
 */
/** MediaExtensionPlugin */
class MediaExtensionPlugin : FlutterPlugin, MethodCallHandler, ActivityAware,
    PluginRegistry.ActivityResultListener {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private lateinit var context: Context
    private var activity: Activity? = null
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val LOG_TAG = "MediaExtensionPlugin";

    enum class IntentAction {
        MAIN,
        PICK,
        EDIT,
        VIEW,
        UNKNOWN
    }

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "media_extension")
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            val intentChecker = getIntentAction()
            channel.invokeMethod("getIntentAction", intentChecker)
        },0)
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "getPlatformVersion" -> {
                result.success("Android ${android.os.Build.VERSION.RELEASE}")
            }
            "setResult" -> {
                setResult(call,result);
            }
            "setAs" -> {
                setAs(call, result);
            }
            "edit" -> {
                edit(call, result);
            }
            "openWith" -> {
                openWith(call, result);
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    private fun getIntentAction() : String {
        val intent: Intent? = activity!!.intent
        var uri = ""
        var resAction = IntentAction.valueOf("MAIN")
        if(intent!=null){
                val data: Uri? = intent.data
                val action = intent?.getAction()
                when(action){
                    Intent.ACTION_PICK -> {
                        resAction = IntentAction.valueOf("PICK")
                    }
                    Intent.ACTION_EDIT -> {
                        resAction = IntentAction.valueOf("EDIT")
                        uri = data.toString()
                    }
                    Intent.ACTION_VIEW -> {
                        resAction = IntentAction.valueOf("VIEW")
                        uri = data.toString()
                    }
                    else -> {
                        resAction = IntentAction.valueOf("MAIN")
                    }
                }
            }
           val result = resAction.toString() + "!" + uri
           return result
    }

    private fun setResult(call: MethodCall, result:MethodChannel.Result){
        val arguments : Map<String,String>? = (call.arguments() as Map<String,String>?)
        val path = arguments!!["uri"]
        val uri = getPickedUri(context, Uri.parse(path))
        val uriString = uri!!.toString();
        val intent: Intent = Intent("io.ente.RESULT_ACTION")
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        activity!!.setResult(Activity.RESULT_OK, intent)
        activity!!.finish()     
    }

    private fun setAs(call: MethodCall, result: MethodChannel.Result) {
        val title = "Set as"
        val uri = call.argument<String>("uri")?.let { Uri.parse(it) }
        val mimeType = call.argument<String>("mimeType")
        if (uri == null) {
            result.error("setAs-args", "missing arguments", null)
            return
        }
        val intent = Intent(Intent.ACTION_ATTACH_DATA)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .putExtra("mimeType", mimeType)
            .setDataAndType(getShareableUri(activity!!.applicationContext, uri), mimeType)
        val started = safeStartActivityChooser(title, intent)
        result.success(started)
    }

    private fun openWith(call: MethodCall, result: MethodChannel.Result) {
        val title = call.argument<String>("title")
        val uri = call.argument<String>("uri")?.let { Uri.parse(it) }
        val mimeType = call.argument<String>("mimeType")
        if (uri == null) {
            result.error("open-args", "missing arguments", null)
            return
        }

        val intent = Intent(Intent.ACTION_VIEW)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .setDataAndType(getShareableUri(activity!!.applicationContext, uri), mimeType)
        val started = safeStartActivityChooser(title, intent)

        result.success(started)
    }

    private fun edit(call: MethodCall, result: MethodChannel.Result) {
        val title = call.argument<String>("title")
        val uri = call.argument<String>("uri")?.let { Uri.parse(it) }
        val mimeType = call.argument<String>("mimeType")
        if (uri == null) {
            result.error("edit-args", "missing arguments", null)
            return
        }

        val intent = Intent(Intent.ACTION_EDIT)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            .setDataAndType(getShareableUri(activity!!.applicationContext, uri), mimeType)
        val started = safeStartActivityChooser(title, intent)

        result.success(started)
    }

    private fun getPickedUri(context: Context, uri: Uri): Uri? {
        val bitmap: Bitmap
        val path : Uri = Uri.parse("file://${uri.toString()}")
        val source = ImageDecoder.createSource(activity!!.contentResolver,path)
        bitmap = ImageDecoder.decodeBitmap(source)
        val imagesFolder = File(activity!!.cacheDir,"images")
        var contentUri: Uri? = null
        imagesFolder.mkdirs()
        val file = File(imagesFolder,"shared.jpeg")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
        stream.flush()
        stream.close()
        contentUri = FileProvider.getUriForFile(context,"${context.packageName}.fileprovider",file)
        return contentUri
    }

    private fun getShareableUri(context: Context, uri: Uri): Uri? {
        /* https://developer.android.com/training/secure-file-sharing/setup-sharing.html
        https://developer.android.com/training/secure-file-sharing/setup-sharing.html
         */
        return when (uri.scheme?.lowercase(Locale.ROOT)) {
            ContentResolver.SCHEME_FILE -> {
                uri.path?.let { path ->
                    val authority = "${context.packageName}.file_provider"
                    FileProvider.getUriForFile(context, authority, File(path))
                }
            }
            else -> uri
        }
    }

    private fun safeStartActivityChooser(title: String?, intent: Intent): Boolean {
        if (activity?.let { intent.resolveActivity(it.packageManager) } == null) {
            Log.i(LOG_TAG, " intent=$intent resolved activity return null")
            //return false
        }
        try {
            activity?.startActivity(Intent.createChooser(intent, title))
            return true
        } catch (e: SecurityException) {
            if (intent.flags and Intent.FLAG_GRANT_WRITE_URI_PERMISSION != 0) {
                // in some environments, providing the write flag yields a `SecurityException`:
                // "UID XXXX does not have permission to content://XXXX"
                // so we retry without it
                Log.i(LOG_TAG, "retry intent=$intent without FLAG_GRANT_WRITE_URI_PERMISSION")
                intent.flags = intent.flags and Intent.FLAG_GRANT_WRITE_URI_PERMISSION.inv()
                return safeStartActivityChooser(title, intent)
            } else {
                Log.w(LOG_TAG, "failed to start activity chooser for intent=$intent", e)
            }
        }
        return false
    }


    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity;
        binding.addActivityResultListener(this);
    }

    override fun onDetachedFromActivityForConfigChanges() {
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    }

    override fun onDetachedFromActivity() {
        activity = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        return true
    }
}
