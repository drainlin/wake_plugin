package com.example.external_app_launcher

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

class ExternalAppLauncherPlugin : FlutterPlugin, MethodCallHandler {
    private lateinit var channel: MethodChannel
    private lateinit var applicationContext: Context

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        applicationContext = binding.applicationContext
        channel = MethodChannel(binding.binaryMessenger, "external_app_launcher")
        channel.setMethodCallHandler(this)
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "canOpen" -> handleCanOpen(call, result)
            "open" -> handleOpen(call, result)
            else -> result.notImplemented()
        }
    }

    private fun handleCanOpen(call: MethodCall, result: Result) {
        val url = call.argument<String>("url")
        val uri = parseUriOrNull(url)

        if (uri == null) {
            result.success(false)
            return
        }

        val intent = buildViewIntent(uri)
        result.success(canResolve(intent))
    }

    private fun handleOpen(call: MethodCall, result: Result) {
        val url = call.argument<String>("url")
        val uri = try {
            parseUri(url)
        } catch (error: IllegalArgumentException) {
            result.error("INVALID_URL", error.message, null)
            return
        }

        val useChooser = call.argument<Boolean>("useChooser") ?: false
        val chooserTitle = call.argument<String>("chooserTitle") ?: "选择应用"
        val viewIntent = buildViewIntent(uri)

        if (!canResolve(viewIntent)) {
            result.error(
                "NO_ACTIVITY",
                "No installed application can handle this URL: $url",
                null
            )
            return
        }

        val launchIntent = if (useChooser) {
            Intent.createChooser(viewIntent, chooserTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            viewIntent
        }

        try {
            applicationContext.startActivity(launchIntent)
            result.success(null)
        } catch (error: ActivityNotFoundException) {
            result.error(
                "ACTIVITY_NOT_FOUND",
                "No activity found to handle this URL: $url",
                error.message
            )
        } catch (error: IllegalArgumentException) {
            result.error("ILLEGAL_ARGUMENT", error.message, null)
        }
    }

    private fun parseUriOrNull(url: String?): Uri? {
        return try {
            parseUri(url)
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private fun parseUri(url: String?): Uri {
        val trimmedUrl = url?.trim()
        if (trimmedUrl.isNullOrEmpty()) {
            throw IllegalArgumentException("URL must not be empty.")
        }

        return try {
            Uri.parse(trimmedUrl)
        } catch (error: Exception) {
            throw IllegalArgumentException("Invalid URL: ${error.message}", error)
        }
    }

    private fun buildViewIntent(uri: Uri): Intent {
        return Intent(Intent.ACTION_VIEW, uri).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    private fun canResolve(intent: Intent): Boolean {
        val packageManager = applicationContext.packageManager
        val flags = PackageManager.MATCH_DEFAULT_ONLY
        return packageManager.queryIntentActivities(intent, flags).isNotEmpty()
    }
}
