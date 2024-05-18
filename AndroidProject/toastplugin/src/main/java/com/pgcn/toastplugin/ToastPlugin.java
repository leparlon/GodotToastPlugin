package com.pgcn.toastplugin;

import android.app.Activity;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.UsedByGodot;
import org.godotengine.godot.plugin.SignalInfo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class ToastPlugin extends GodotPlugin {

    public ToastPlugin(Godot godot) {
        super(godot);
    }

    @Override
    public String getPluginName() {
        return "ToastPlugin";
    }

    @Override
    public Set<SignalInfo> getPluginSignals() {
        // Define the signals this plugin will emit
        Set<SignalInfo> signals = new HashSet<>();
        signals.add(new SignalInfo("toast_shown"));
        signals.add(new SignalInfo("toast_hidden"));
        signals.add(new SignalInfo("toast_callback", String.class)); // Expect a String argument for callback
        return signals;
    }

    @UsedByGodot
    public void showToast(final String message, final int duration, final int gravity, final int xOffset, final int yOffset) {
        final Activity activity = getActivity();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(activity.getApplicationContext(), message, duration);
                toast.setGravity(gravity, xOffset, yOffset);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // Use the callback for API 30 and above
                    toast.addCallback(new Toast.Callback() {
                        @Override
                        public void onToastShown() {
                            emitSignal("toast_shown");
                        }

                        @Override
                        public void onToastHidden() {
                            emitSignal("toast_hidden");
                        }
                    });
                } else {
                    // Use handler for below API 30
                    emitSignal("toast_shown");
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            emitSignal("toast_hidden");
                        }
                    }, duration == Toast.LENGTH_SHORT ? 2000 : 3500);
                }

                toast.show();
            }
        });
    }

    @UsedByGodot
    public void showToastWithCallback(final String message, final int duration, final int gravity, final int xOffset, final int yOffset, final String callbackMethod) {
        final Activity activity = getActivity();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(activity.getApplicationContext(), message, duration);
                toast.setGravity(gravity, xOffset, yOffset);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // Use the callback for API 30 and above
                    toast.addCallback(new Toast.Callback() {
                        @Override
                        public void onToastShown() {
                            emitSignal("toast_shown");
                        }

                        @Override
                        public void onToastHidden() {
                            emitSignal("toast_hidden");
                            emitSignal("toast_callback", callbackMethod);
                        }
                    });
                } else {
                    // Use handler for below API 30
                    emitSignal("toast_shown");
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            emitSignal("toast_hidden");
                            emitSignal("toast_callback", callbackMethod);
                        }
                    }, duration == Toast.LENGTH_SHORT ? 2000 : 3500);
                }

                toast.show();
            }
        });
    }

    private void runOnMainThread(Runnable runnable) {
        getGodot().runOnUiThread(runnable);
    }

    @Override
    public List<String> getPluginMethods() {
        return Arrays.asList("showToast", "showToastWithCallback");
    }
}
