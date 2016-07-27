package com.AuroraByteSoftware.AuroraDMX.fixture;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.AuroraByteSoftware.AuroraDMX.MainActivity;
import com.AuroraByteSoftware.AuroraDMX.R;
import com.AuroraByteSoftware.AuroraDMX.ui.EditColumnMenu;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import yuku.ambilwarna.AmbilWarnaDialog;

public class RGBFixture extends Fixture implements OnClickListener {


    private LinearLayout viewGroup = null;
    private TextView tvVal = null;
    private int ChNum = 0;
    private View rgbSelectView;

    private double step[] = new double[3];
    private double stepIteram[] = new double[3];
    private final MainActivity context;
    private String chText = "";
    private String chValuePresets = "";
    private TextView tvChNum;
    private List<Integer> rgbLevel = new ArrayList<>(Collections.nCopies(3, 0));
    private AmbilWarnaDialog ambilWarnaDialog = null;
    private int defaultLvlTextColor = 0;
    private final static String RGB_REGEX = REGEX_255 + "," + REGEX_255 + "," + REGEX_255;
    private final static String RGB_HEX_REGEX = "[a-fA-F0-9]{6}";
    private final static String PRESET_VALUE_REGEX = "^(" + RGB_REGEX + "|" + RGB_HEX_REGEX + ")$";

    public RGBFixture(final MainActivity context, String channelName, String presets) {
        this.context = context;
        this.chText = channelName == null ? this.chText : channelName;
        this.viewGroup = new LinearLayout(context);
        this.chValuePresets = presets;
        init();
    }

    RGBFixture(final MainActivity context, String channelName, LinearLayout viewGroup) {
        this.context = context;
        this.chText = channelName == null ? this.chText : channelName;
        this.viewGroup = viewGroup;

        ambilWarnaDialog = new AmbilWarnaDialog(context, 0, this);
        ambilWarnaDialog.setChannelName(chText);
        rgbSelectView = ambilWarnaDialog.getView();
        viewGroup.addView(rgbSelectView, 2);

        viewGroup.getChildAt(3).setOnClickListener(this);

        tvChNum = ((TextView) viewGroup.getChildAt(0));
        tvVal = ((TextView) viewGroup.getChildAt(1));
        tvVal.setText("R:0 G:0 B:0");
        defaultLvlTextColor = new TextView(context).getTextColors().getDefaultColor();

        refreshValuePresetsHook();
    }

    @Override
    public void init() {
        viewGroup.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
        viewGroup.setOrientation(LinearLayout.VERTICAL);

        tvChNum = new TextView(context);
        tvChNum.setText(String.format("%1$s", ChNum));
        tvChNum.setTextSize((int) context.getResources().getDimension(R.dimen.font_size));
        tvChNum.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        viewGroup.addView(tvChNum);

        tvVal = new TextView(context);
        tvVal.setText("R:0 G:0 B:0");
        tvVal.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        tvVal.setTextSize((int) context.getResources().getDimension(R.dimen.font_size_sm));
        defaultLvlTextColor = tvVal.getTextColors().getDefaultColor();
        viewGroup.addView(tvVal);

        ambilWarnaDialog = new AmbilWarnaDialog(context, 0, this);
        rgbSelectView = ambilWarnaDialog.getView();
        viewGroup.addView(rgbSelectView);

        Button editButton = new Button(context);
        editButton.setOnClickListener(this);
        editButton.setText(R.string.edit);

        refreshValuePresetsHook();
        viewGroup.addView(editButton);
    }


    /**
     * Set color to indicate that there are presets and bind listener
     */
    private void refreshValuePresetsHook() {
        if (tvVal == null) {
            return;
        }
        final List<Pair<String, String>> presets = FixtureUtility.getParsedValuePresets(getValuePresets(), PRESET_VALUE_REGEX);
        if (presets != null) {
            tvVal.setTextColor(Color.parseColor(PRESET_TEXT_COLOR));
            tvVal.setOnClickListener(this);
        } else {
            tvVal.setTextColor(defaultLvlTextColor);
            tvVal.setOnClickListener(null);
        }
    }

    /**
     * Ask the user what preset they want to jump to
     * then jump there
     */
    private void openSelectPresetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // Add the buttons
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        final List<Pair<String, String>> presets = FixtureUtility.getParsedValuePresets(getValuePresets(), PRESET_VALUE_REGEX);
        if (presets == null)
            return;
        String[] presetArray = new String[presets.size()];
        int i = 0;
        for (Pair<String, String> v : presets) {
            presetArray[i] = v.getLeft() + " (" + v.getRight() + ")";
            i++;
        }

        builder.setTitle(R.string.pick_preset)
                .setItems(presetArray, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        final List<Integer> integers = splitLevels(presets.get(which).getRight());
                        if (integers != null) {
                            setChLevels(integers);
                        }
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Split the user input preset into RGB ints
     *
     * @param right ff0011 or 12,34,56
     * @return list of DMX rgb values
     */
    public static List<Integer> splitLevels(String right) {
        String value = right.trim();
        final List<Integer> integers = new ArrayList<>(3);
        if (value.matches(RGB_REGEX)) {
            final String[] split = value.split(",");
            integers.add(Integer.parseInt(split[0]));
            integers.add(Integer.parseInt(split[1]));
            integers.add(Integer.parseInt(split[2]));
            return integers;
        } else if (value.matches(RGB_HEX_REGEX)) {
            integers.add(Integer.valueOf(value.substring(0, 2), 16));
            integers.add(Integer.valueOf(value.substring(2, 4), 16));
            integers.add(Integer.valueOf(value.substring(4, 6), 16));
            return integers;
        } else {
            return null;
        }
    }

    /**
     * Get the LinearLayout that contains the text, slider, and button for one
     * channel
     *
     * @return the viewGroup
     */
    @Override
    public LinearLayout getViewGroup() {
        return viewGroup;
    }


    /**
     * Sets the level of the channel
     *
     * @param a_chLevel set the level
     */
    @Override
    public void setChLevels(List<Integer> a_chLevel) {
        for (int i = 0; i < a_chLevel.size(); i++) {
            rgbLevel.set(i, Math.min(MAX_LEVEL, a_chLevel.get(i)));
        }
        updateFixtureLevelText();
    }

    @Override
    protected void updateFixtureLevelText() {
        ambilWarnaDialog.setRGBLevel(rgbLevel);
    }

    /**
     * get the level of the channel
     */
    @Override
    public List<Integer> getChLevels() {
        return rgbLevel;
    }

    /**
     * Toggle button
     */
    @Override
    public void onClick(View v) {
        if (v == tvVal) {
            openSelectPresetDialog();
        } else {
            EditColumnMenu.createEditColumnMenu(viewGroup, context, this, chText, 0, chValuePresets);
        }
    }


    @Override
    public String toString() {
        return ("Ch: " + ChNum + "\tLvl: " + rgbLevel);
    }

    /**
     * Creates 255 steeps between current and endVal
     *
     * @param endVal value after fade
     * @param steps  number of steps to take to get to the final falue
     */
    @Override
    public void setupIncrementLevelFade(List<Integer> endVal, double steps) {
        step[0] = (endVal.get(0) - rgbLevel.get(0)) / steps;
        step[1] = (endVal.get(1) - rgbLevel.get(1)) / steps;
        step[2] = (endVal.get(2) - rgbLevel.get(2)) / steps;
        stepIteram[0] = rgbLevel.get(0);
        stepIteram[1] = rgbLevel.get(1);
        stepIteram[2] = rgbLevel.get(2);
    }

    /**
     * Adds one step Up to the current level
     */
    @Override
    public void incrementLevelUp() {
        if (step[0] > 0)
            stepIteram[0] += step[0];
        if (step[1] > 0)
            stepIteram[1] += step[1];
        if (step[2] > 0)
            stepIteram[2] += step[2];
        updateIncrementedLevel();
    }

    /**
     * Adds one step Down to the current level
     */
    @Override
    public void incrementLevelDown() {
        if (step[0] < 0)
            stepIteram[0] += step[0];
        if (step[1] < 0)
            stepIteram[1] += step[1];
        if (step[2] < 0)
            stepIteram[2] += step[2];
        updateIncrementedLevel();
    }

    private void updateIncrementedLevel() {
        rgbLevel.set(0, (int) Math.round(stepIteram[0]));
        rgbLevel.set(1, (int) Math.round(stepIteram[1]));
        rgbLevel.set(2, (int) Math.round(stepIteram[2]));
        ambilWarnaDialog.setRGBLevel(rgbLevel);
    }

    @Override
    public void setScrollColor(int scrollColor) {
        //RGB has its own color
    }

    public void setColumnText(String text) {
        this.chText = text;
        ambilWarnaDialog.setChannelName(text);
    }

    public void setValuePresets(String text) {
        this.chValuePresets = text;
        refreshValuePresetsHook();
    }

    @Override
    public String getChText() {
        return chText;
    }

    public void setChText(String chText) {
        tvVal.setText(chText);
    }

    public String getValuePresets() {
        return chValuePresets;
    }

    @Override
    public boolean isRGB() {
        return true;
    }

    @Override
    public void removeSelector() {
        viewGroup.removeView(rgbSelectView);
    }

    @Override
    public void setFixtureNumber(int currentFixtureNum) {
        tvChNum.setText(String.format("%1$s", currentFixtureNum));
    }

    public void refreshLayout() {
        ambilWarnaDialog.getView().post(new Runnable() {
            @Override
            public void run() {
                ambilWarnaDialog.getViewSatVal().invalidate();
            }
        });
    }

    public void setChLevelArray(List<Integer> chLevels) {
        rgbLevel = chLevels;
    }
}
