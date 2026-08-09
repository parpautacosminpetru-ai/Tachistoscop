package ro.tachistoscop.app;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private TachistoscopeView tachistoscopeView;
    private TextView counterText;
    private Button previousButton;
    private Button nextButton;
    private List<String> stimuli = new ArrayList<>();
    private int currentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(58, 36, 23));
        getWindow().setNavigationBarColor(Color.rgb(42, 26, 16));
        buildUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadContent();
    }

    @Override
    protected void onPause() {
        AppPrefs.setCurrentIndex(this, currentIndex);
        super.onPause();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(234, 220, 190));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(16), dp(10), dp(10), dp(8));
        header.setBackgroundColor(Color.rgb(58, 36, 23));

        TextView title = new TextView(this);
        title.setText("TACHISTOSCOP");
        title.setTextColor(Color.rgb(244, 225, 190));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.SERIF, android.graphics.Typeface.BOLD));
        header.addView(title, new LinearLayout.LayoutParams(0, dp(48), 1f));

        Button settings = new Button(this);
        settings.setText("SETĂRI");
        settings.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        settings.setTextColor(Color.WHITE);
        settings.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(118, 72, 42)));
        settings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        header.addView(settings, new LinearLayout.LayoutParams(dp(100), dp(44)));

        root.addView(header, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        tachistoscopeView = new TachistoscopeView(this);
        root.addView(tachistoscopeView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        LinearLayout footer = new LinearLayout(this);
        footer.setOrientation(LinearLayout.VERTICAL);
        footer.setPadding(dp(12), dp(8), dp(12), dp(12));
        footer.setBackgroundColor(Color.rgb(223, 204, 169));

        counterText = new TextView(this);
        counterText.setTextColor(Color.rgb(65, 43, 29));
        counterText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        counterText.setGravity(Gravity.CENTER);
        counterText.setPadding(0, 0, 0, dp(6));
        footer.addView(counterText, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);

        previousButton = makeWoodButton("◀  ÎNAPOI");
        previousButton.setOnClickListener(v -> previousStimulus());
        nav.addView(previousButton, new LinearLayout.LayoutParams(0, dp(52), 1f));

        TextView spacer = new TextView(this);
        nav.addView(spacer, new LinearLayout.LayoutParams(dp(10), dp(1)));

        nextButton = makeWoodButton("URMĂTORUL  ▶");
        nextButton.setOnClickListener(v -> nextStimulus());
        nav.addView(nextButton, new LinearLayout.LayoutParams(0, dp(52), 1f));

        footer.addView(nav, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(footer, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        setContentView(root);
    }

    private Button makeWoodButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(100, 60, 35)));
        return button;
    }

    private void reloadContent() {
        stimuli = AppPrefs.getStimuli(this);
        currentIndex = AppPrefs.getCurrentIndex(this);
        if (stimuli.isEmpty()) {
            currentIndex = 0;
        } else if (currentIndex >= stimuli.size()) {
            currentIndex = stimuli.size() - 1;
        }

        tachistoscopeView.setStimulusStyle(AppPrefs.getTypeface(this), AppPrefs.getFontSizeSp(this));
        showCurrentStimulus();
    }

    private void showCurrentStimulus() {
        if (stimuli.isEmpty()) {
            tachistoscopeView.setStimulus("");
            counterText.setText("Niciun element • deschide Setări și adaugă text");
            previousButton.setEnabled(false);
            nextButton.setEnabled(false);
            return;
        }

        previousButton.setEnabled(true);
        nextButton.setEnabled(true);
        tachistoscopeView.setStimulus(stimuli.get(currentIndex));
        counterText.setText("Element " + (currentIndex + 1) + " / " + stimuli.size() + " • schimbarea este manuală");
        AppPrefs.setCurrentIndex(this, currentIndex);
    }

    private void nextStimulus() {
        if (stimuli.isEmpty()) return;
        currentIndex = (currentIndex + 1) % stimuli.size();
        showCurrentStimulus();
    }

    private void previousStimulus() {
        if (stimuli.isEmpty()) return;
        currentIndex = (currentIndex - 1 + stimuli.size()) % stimuli.size();
        showCurrentStimulus();
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }
}
