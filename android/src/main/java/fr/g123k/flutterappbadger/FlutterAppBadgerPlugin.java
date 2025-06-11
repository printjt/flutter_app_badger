package fr.g123k.flutterappbadger;

import android.content.Context;
import androidx.annotation.NonNull;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.BinaryMessenger; // Add this import
import io.flutter.plugin.common.MethodCodec; // Add this import
import me.leolin.shortcutbadger.ShortcutBadger;

public class FlutterAppBadgerPlugin implements FlutterPlugin, MethodCallHandler {
    private static final String CHANNEL_NAME = "g123k/flutter_app_badger";
    
    private Context applicationContext;
    private MethodChannel channel;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        setupChannel(
            flutterPluginBinding.getBinaryMessenger(),
            flutterPluginBinding.getApplicationContext()
        );
    }

    private void setupChannel(
        @NonNull BinaryMessenger messenger, // Changed from MethodCodec to BinaryMessenger
        @NonNull Context context
    ) {
        this.applicationContext = context;
        this.channel = new MethodChannel(messenger, CHANNEL_NAME);
        this.channel.setMethodCallHandler(this);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        teardownChannel();
    }

    private void teardownChannel() {
        if (channel != null) {
            channel.setMethodCallHandler(null);
            channel = null;
        }
        applicationContext = null;
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        try {
            switch (call.method) {
                case "updateBadgeCount":
                    handleUpdateBadgeCount(call, result);
                    break;
                case "removeBadge":
                    handleRemoveBadge(result);
                    break;
                case "isAppBadgeSupported":
                    handleIsAppBadgeSupported(result);
                    break;
                default:
                    result.notImplemented();
                    break;
            }
        } catch (Exception e) {
            result.error(
                "BADGE_ERROR",
                "Error while executing " + call.method,
                e.getMessage()
            );
        }
    }

    private void handleUpdateBadgeCount(@NonNull MethodCall call, @NonNull Result result) {
        if (call.argument("count") == null) {
            result.error(
                "INVALID_ARGUMENT",
                "Count argument is required",
                null
            );
            return;
        }

        int count = ((Number) call.argument("count")).intValue();
        boolean success = ShortcutBadger.applyCount(applicationContext, count);
        
        if (success) {
            result.success(null);
        } else {
            result.error(
                "UPDATE_FAILED",
                "Failed to update badge count",
                null
            );
        }
    }

    private void handleRemoveBadge(@NonNull Result result) {
        boolean success = ShortcutBadger.removeCount(applicationContext);
        
        if (success) {
            result.success(null);
        } else {
            result.error(
                "REMOVE_FAILED",
                "Failed to remove badge",
                null
            );
        }
    }

    private void handleIsAppBadgeSupported(@NonNull Result result) {
        boolean isSupported = ShortcutBadger.isBadgeCounterSupported(applicationContext);
        result.success(isSupported);
    }
}