package io.ente.photos.media_extension

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.content.FileProvider
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import java.io.*
import java.util.*
import kotlin.collections.HashMap


/// The Class which implements Activity Aware FlutterPlugin
class MediaExtensionPlugin : FlutterPlugin, MethodCallHandler, ActivityAware,
    PluginRegistry.ActivityResultListener {

    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var methodChannel: MethodChannel
    private lateinit var context: Context
    private var activity: Activity? = null
    private val logTag = "MediaExtensionPlugin"

    ///ENUM of all the possible IntentAction for a gallery app.
    enum class IntentAction {
        MAIN,
        PICK,
        EDIT,
        VIEW
    }

    /// The Method invoked when FlutterEngine is attached to the app
    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        ///Application context is assigned to variable context 
        context = flutterPluginBinding.applicationContext

        ///Method Channel instance is created for channel [media_extension]
        methodChannel = MethodChannel(flutterPluginBinding.binaryMessenger, "media_extension")

        ///To Trigger events in mainThread
        Handler(Looper.getMainLooper()).postDelayed({

            /// `getIntentAction` method is invoked 
            /// to send data from android to flutter thread
            val intentChecker = getIntentAction()
            methodChannel.invokeMethod("getIntentAction", intentChecker)
        }, 0)

        /// Method Channel handler which handles all the methods
        /// invoked from flutter thread
        methodChannel.setMethodCallHandler(this)
    }

    /// The Method invoked when a methodCall is executed from flutter thread
    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "getPlatformVersion" -> {
                result.success("Android ${Build.VERSION.RELEASE}")
            }
            "setResult" -> {
                setResult(call)
            }
            "setAs" -> {
                setAs(call, result)
            }
            "edit" -> {
                edit(call, result)
            }
            "openWith" -> {
                openWith(call, result)
            }
            else -> {
                result.notImplemented()
            }
        }
    }


    /// The Method is triggered by the Flutter thread with arguments containing
    /// and [uri] of the received image of type content://xyz
    /// and returns the base64EncodedString of it.
    private fun getResolvedContent(
        contentUri: Uri,
        contentType: String,
        resolvedContent: HashMap<String, String>
    ) {
        val resolver = context.contentResolver
        val contentStream = resolver.openInputStream(contentUri)
        val cursor: Cursor = resolver.query(contentUri, null, null, null, null)!!
        val nameIndex: Int = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        resolvedContent["name"] = cursor.getString(nameIndex)
        val fileType = contentType.split("/")
        resolvedContent["type"] = fileType[0]
        resolvedContent["extension"] = fileType[1]
        if (contentType.startsWith("video")) {
            resolvedContent["data"] = contentUri.toString()
        } else if (contentType.startsWith("image")) {
            resolvedContent["data"] = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Base64.getEncoder().encodeToString(contentStream?.readBytes())
            } else {
                android.util.Base64.encodeToString(
                    contentStream?.readBytes(),
                    android.util.Base64.DEFAULT
                )
            }
        }
        cursor.close()
        contentStream?.close()
    }

    /// The Method is triggered when the app is opened and it sends the [intent-action]
    /// and [uri] information in a HashMap Structure to the Flutter thread.
    private fun getIntentAction(): HashMap<String, String> {
        val intent: Intent? = activity?.intent
        val result = HashMap<String, String>()
        var resAction = IntentAction.valueOf("MAIN")
        if (intent != null) {
            val data: Uri? = intent.data
            val type: String? = intent.type
            when (intent.action) {
                Intent.ACTION_PICK -> {
                    resAction = IntentAction.valueOf("PICK")
                }
                Intent.ACTION_GET_CONTENT -> {
                    resAction = IntentAction.valueOf("PICK")
                }
                Intent.ACTION_EDIT -> {
                    resAction = IntentAction.valueOf("EDIT")
                }
                Intent.ACTION_VIEW -> {
                    if(data != null) {
                        result["uri"] = data.toString()
                    }
                    resAction = IntentAction.valueOf("VIEW")
                    getResolvedContent(data!!, type!!, result)
                }
                else -> {
                    resAction = IntentAction.valueOf("MAIN")
                }
            }
        }
        result["action"] = resAction.toString()
        return result
    }

    /// The Method is triggered by the Flutter thread with arguments containing
    /// and [uri] of the selected image and sends the image to the requested app
    /// via RESULT_ACTION Intent using Content Provider
    private fun setResult(call: MethodCall) {
        val arguments: Map<String, String>? = (call.arguments() as Map<String, String>?)
        val path = arguments!!["uri"]
        val uri = getShareableUri(context, Uri.parse(path))
        val intent = Intent("io.ente.RESULT_ACTION")
        intent.data = uri
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        activity!!.setResult(Activity.RESULT_OK, intent)
        activity!!.finish()
    }


    /// The Method is triggered by the Flutter thread with arguments containing
    /// and [uri] of the selected image and sends the image to the chosen app
    /// which can handle the `ACTION_ATTACH_DATA` Intent
    private fun setAs(call: MethodCall, result: Result) {
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

    /// The Method is triggered by the Flutter thread with arguments containing
    /// and [uri] of the selected image and sends the image to the chosen app
    /// which can handle the `ACTION_VIEW` Intent
    private fun openWith(call: MethodCall, result: Result) {
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

    /// The Method is triggered by the Flutter thread with arguments containing
    /// and [uri] of the selected image and sends the image to the chosen app
    /// which can handle the `ACTION_EDIT` Intent
    private fun edit(call: MethodCall, result: Result) {
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

    /// The Method is creates content of the file which needs to be shared to
    /// other app using content resolver.
    private fun getShareableUri(context: Context, uri: Uri): Uri? {
        /* https://developer.android.com/training/secure-file-sharing/setup-sharing.html
        https://developer.android.com/training/secure-file-sharing/setup-sharing.html
         */
        return when (uri.scheme?.lowercase(Locale.ROOT)) {
            ContentResolver.SCHEME_FILE -> {
                uri.path?.let { path ->
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.file_provider",
                        File(path)
                    )
                }
            }
            else -> uri
        }
    }

    /// The Method is used to list out all the available apps 
    ///  which can handle the Supplied Intent Action.
    private fun safeStartActivityChooser(title: String?, intent: Intent): Boolean {
        if (activity?.let { intent.resolveActivity(it.packageManager) } == null) {
            Log.i(logTag, " intent=$intent resolved activity return null")
            //return false
        }
        try {
            activity?.startActivity(Intent.createChooser(intent, title))
            return true
        } catch (e: SecurityException) {
            if (intent.flags and Intent.FLAG_GRANT_WRITE_URI_PERMISSION != 0) {
                // in some environments, providing the write flag yields a `SecurityException`:
                // "UID `xyz` does not have permission to `content://xyz`"
                // so we retry without it
                Log.i(logTag, "retry intent=$intent without FLAG_GRANT_WRITE_URI_PERMISSION")
                intent.flags = intent.flags and Intent.FLAG_GRANT_WRITE_URI_PERMISSION.inv()
                return safeStartActivityChooser(title, intent)
            } else {
                Log.w(logTag, "failed to start activity chooser for intent=$intent", e)
            }
        }
        return false
    }


    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        methodChannel.setMethodCallHandler(null)
    }

    /// The Method Invoked after the Plugin is attached to Flutter engine
    /// Provides the activity context of the application
    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        binding.addActivityResultListener(this)
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
