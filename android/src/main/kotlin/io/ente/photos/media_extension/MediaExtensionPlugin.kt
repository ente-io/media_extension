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
import java.io.File
import java.util.*

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

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "media_extension")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "getPlatformVersion" -> {
                result.success("Android ${android.os.Build.VERSION.RELEASE}")
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
    }
}
