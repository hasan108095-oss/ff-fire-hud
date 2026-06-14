package com.example;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class MainActivity extends AppCompatActivity {

    private static final int OVERLAY_PERMISSION_REQ_CODE = 5469;

    // Preferences keys
    private static final String PREFS_NAME = "FF_Fire_HUD_Prefs";
    private static final String KEY_FIRE_SIZE = "fire_size";
    private static final String KEY_JOYSTICK_SIZE = "joystick_size";
    private static final String KEY_OPACITY = "opacity";

    // Constraints
    private static final int MIN_FIRE_SIZE = 50;
    private static final int MAX_FIRE_SIZE = 100;
    private static final int MIN_JOYSTICK_SIZE = 80;
    private static final int MAX_JOYSTICK_SIZE = 160;
    private static final int MIN_OPACITY = 30;
    private static final int MAX_OPACITY = 95;

    // Presets
    private static final int PRESET_DEFAULT_FIRE = 70;
    private static final int PRESET_DEFAULT_JOY = 120;
    private static final int PRESET_DEFAULT_OPA = 84;

    private static final int PRESET_COMPACT_FIRE = 55;
    private static final int PRESET_COMPACT_JOY = 90;
    private static final int PRESET_COMPACT_OPA = 50;

    private static final int PRESET_LARGE_FIRE = 90;
    private static final int PRESET_LARGE_JOY = 145;
    private static final int PRESET_LARGE_OPA = 95;

    // State variables
    private int currentFireSize = PRESET_DEFAULT_FIRE;
    private int currentJoystickSize = PRESET_DEFAULT_JOY;
    private int currentOpacity = PRESET_DEFAULT_OPA;

    // UI elements
    private TextView tvStatFireSize, tvStatJoystickSize, tvStatOpacity;
    private TextView tvFireVal, tvJoystickVal, tvOpacityVal;
    private ProgressBar progressFire, progressJoystick;
    private SeekBar seekbarOpacity;

    private MaterialButton btnMinusFire, btnPlusFire;
    private MaterialButton btnMinusJoystick, btnPlusJoystick;

    private MaterialCardView cardPresetDefault, cardPresetCompact, cardPresetLarge;

    private MaterialButton btnStartCrosshair, btnStopCrosshair;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        loadSettings();
        setupListeners();
        updateUI();
    }

    private void initializeViews() {
        // Find TextViews
        tvStatFireSize = findViewById(R.id.tv_stat_fire_size);
        tvStatJoystickSize = findViewById(R.id.tv_stat_joystick_size);
        tvStatOpacity = findViewById(R.id.tv_stat_opacity);

        tvFireVal = findViewById(R.id.tv_fire_val);
        tvJoystickVal = findViewById(R.id.tv_joystick_val);
        tvOpacityVal = findViewById(R.id.tv_opacity_val);

        // ProgressBars & Seekbar
        progressFire = findViewById(R.id.progress_fire);
        progressJoystick = findViewById(R.id.progress_joystick);
        seekbarOpacity = findViewById(R.id.seekbar_opacity);

        // Buttons
        btnMinusFire = findViewById(R.id.btn_minus_fire);
        btnPlusFire = findViewById(R.id.btn_plus_fire);
        btnMinusJoystick = findViewById(R.id.btn_minus_joystick);
        btnPlusJoystick = findViewById(R.id.btn_plus_joystick);

        // Presets Cards
        cardPresetDefault = findViewById(R.id.card_preset_default);
        cardPresetCompact = findViewById(R.id.card_preset_compact);
        cardPresetLarge = findViewById(R.id.card_preset_large);

        // Start/Stop Action Buttons
        btnStartCrosshair = findViewById(R.id.btn_start_crosshair);
        btnStopCrosshair = findViewById(R.id.btn_stop_crosshair);

        // Configure min/max for bounds maps
        progressFire.setMax(MAX_FIRE_SIZE - MIN_FIRE_SIZE);
        progressJoystick.setMax(MAX_JOYSTICK_SIZE - MIN_JOYSTICK_SIZE);
        
        // Seekbar max matches MAX_OPACITY
        seekbarOpacity.setMax(MAX_OPACITY);
    }

    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        currentFireSize = prefs.getInt(KEY_FIRE_SIZE, PRESET_DEFAULT_FIRE);
        currentJoystickSize = prefs.getInt(KEY_JOYSTICK_SIZE, PRESET_DEFAULT_JOY);
        currentOpacity = prefs.getInt(KEY_OPACITY, PRESET_DEFAULT_OPA);
    }

    private void saveSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_FIRE_SIZE, currentFireSize);
        editor.putInt(KEY_JOYSTICK_SIZE, currentJoystickSize);
        editor.putInt(KEY_OPACITY, currentOpacity);
        editor.apply();
    }

    private void setupListeners() {
        // Fire Button decrement/increment
        btnMinusFire.setOnClickListener(v -> {
            if (currentFireSize > MIN_FIRE_SIZE) {
                currentFireSize -= 5;
                if (currentFireSize < MIN_FIRE_SIZE) currentFireSize = MIN_FIRE_SIZE;
                saveSettings();
                updateUI();
                updateRunningService();
            }
        });

        btnPlusFire.setOnClickListener(v -> {
            if (currentFireSize < MAX_FIRE_SIZE) {
                currentFireSize += 5;
                if (currentFireSize > MAX_FIRE_SIZE) currentFireSize = MAX_FIRE_SIZE;
                saveSettings();
                updateUI();
                updateRunningService();
            }
        });

        // Joystick decrement/increment
        btnMinusJoystick.setOnClickListener(v -> {
            if (currentJoystickSize > MIN_JOYSTICK_SIZE) {
                currentJoystickSize -= 5;
                if (currentJoystickSize < MIN_JOYSTICK_SIZE) currentJoystickSize = MIN_JOYSTICK_SIZE;
                saveSettings();
                updateUI();
                updateRunningService();
            }
        });

        btnPlusJoystick.setOnClickListener(v -> {
            if (currentJoystickSize < MAX_JOYSTICK_SIZE) {
                currentJoystickSize += 5;
                if (currentJoystickSize > MAX_JOYSTICK_SIZE) currentJoystickSize = MAX_JOYSTICK_SIZE;
                saveSettings();
                updateUI();
                updateRunningService();
            }
        });

        // Opacity SeekBar listener
        seekbarOpacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < MIN_OPACITY) {
                    currentOpacity = MIN_OPACITY;
                } else {
                    currentOpacity = progress;
                }
                tvOpacityVal.setText(currentOpacity + "%");
                tvStatOpacity.setText(currentOpacity + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                saveSettings();
                updateUI();
                updateRunningService();
            }
        });

        // Presets Selectors
        cardPresetDefault.setOnClickListener(v -> {
            applyPreset(PRESET_DEFAULT_FIRE, PRESET_DEFAULT_JOY, PRESET_DEFAULT_OPA);
        });

        cardPresetCompact.setOnClickListener(v -> {
            applyPreset(PRESET_COMPACT_FIRE, PRESET_COMPACT_JOY, PRESET_COMPACT_OPA);
        });

        cardPresetLarge.setOnClickListener(v -> {
            applyPreset(PRESET_LARGE_FIRE, PRESET_LARGE_JOY, PRESET_LARGE_OPA);
        });

        // Start Crosshair Overlay button
        btnStartCrosshair.setOnClickListener(v -> {
            if (checkOverlayPermission()) {
                startFloatingHUDService();
            } else {
                requestOverlayPermission();
            }
        });

        // Stop Crosshair Overlay button
        btnStopCrosshair.setOnClickListener(v -> {
            stopFloatingHUDService();
        });
    }

    private void applyPreset(int fireSize, int joystickSize, int opacity) {
        currentFireSize = fireSize;
        currentJoystickSize = joystickSize;
        currentOpacity = opacity;
        saveSettings();
        updateUI();
        updateRunningService();
        Toast.makeText(this, "Preset applied!", Toast.LENGTH_SHORT).show();
    }

    private void updateUI() {
        // Text stats
        tvStatFireSize.setText(currentFireSize + "px");
        tvStatJoystickSize.setText(currentJoystickSize + "px");
        tvStatOpacity.setText(currentOpacity + "%");

        tvFireVal.setText(currentFireSize + "px");
        tvJoystickVal.setText(currentJoystickSize + "px");
        tvOpacityVal.setText(currentOpacity + "%");

        // Progress bars and seekbar
        progressFire.setProgress(currentFireSize - MIN_FIRE_SIZE);
        progressJoystick.setProgress(currentJoystickSize - MIN_JOYSTICK_SIZE);
        
        // Ensure seekbar progress matches currentOpacity
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            seekbarOpacity.setProgress(currentOpacity, true);
        } else {
            seekbarOpacity.setProgress(currentOpacity);
        }

        // Highlight preset cards
        highlightPresetCard(cardPresetDefault, currentFireSize == PRESET_DEFAULT_FIRE && currentJoystickSize == PRESET_DEFAULT_JOY && currentOpacity == PRESET_DEFAULT_OPA);
        highlightPresetCard(cardPresetCompact, currentFireSize == PRESET_COMPACT_FIRE && currentJoystickSize == PRESET_COMPACT_JOY && currentOpacity == PRESET_COMPACT_OPA);
        highlightPresetCard(cardPresetLarge, currentFireSize == PRESET_LARGE_FIRE && currentJoystickSize == PRESET_LARGE_JOY && currentOpacity == PRESET_LARGE_OPA);
    }

    private void highlightPresetCard(MaterialCardView card, boolean isSelected) {
        if (isSelected) {
            card.setStrokeColor(Color.parseColor("#FF5722"));
            card.setStrokeWidth(dpToPx(2));
        } else {
            card.setStrokeColor(Color.parseColor("#2C2C2C"));
            card.setStrokeWidth(dpToPx(1));
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private boolean checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        }
        return true;
    }

    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Toast.makeText(this, "Overlay permission required. Please enable overlay for FF Fire HUD.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (checkOverlayPermission()) {
                startFloatingHUDService();
                Toast.makeText(this, "Permission granted! Overlay HUD active.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Overlay permission was denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startFloatingHUDService() {
        Intent intent = new Intent(this, FloatingHUDService.class);
        intent.putExtra("fire_size", currentFireSize);
        intent.putExtra("joystick_size", currentJoystickSize);
        intent.putExtra("opacity", currentOpacity);
        startService(intent);
        Toast.makeText(this, "Floating HUD Crosshair Started!", Toast.LENGTH_SHORT).show();
    }

    private void updateRunningService() {
        // If the service is active, send an update intent so the custom sizes/opacities apply instantly on screen
        if (checkOverlayPermission()) {
            Intent intent = new Intent(this, FloatingHUDService.class);
            intent.putExtra("update", true);
            intent.putExtra("fire_size", currentFireSize);
            intent.putExtra("joystick_size", currentJoystickSize);
            intent.putExtra("opacity", currentOpacity);
            startService(intent);
        }
    }

    private void stopFloatingHUDService() {
        Intent intent = new Intent(this, FloatingHUDService.class);
        stopService(intent);
        Toast.makeText(this, "Floating HUD Crosshair Stopped!", Toast.LENGTH_SHORT).show();
    }
}
