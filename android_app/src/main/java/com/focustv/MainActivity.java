package com.focustv;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;

public class MainActivity extends Activity {

    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setBackgroundColor(Color.rgb(5, 8, 22));
        root.setPadding(40, 40, 40, 40);

        LinearLayout menu = new LinearLayout(this);
        menu.setOrientation(LinearLayout.VERTICAL);
        menu.setBackgroundColor(Color.rgb(2, 6, 23));
        menu.setPadding(24, 24, 24, 24);

        LinearLayout.LayoutParams menuParams = new LinearLayout.LayoutParams(320, LinearLayout.LayoutParams.MATCH_PARENT);
        root.addView(menu, menuParams);

        TextView logo = new TextView(this);
        logo.setText("FocusTV");
        logo.setTextColor(Color.WHITE);
        logo.setTextSize(32);
        logo.setTypeface(Typeface.DEFAULT_BOLD);
        logo.setPadding(0, 0, 0, 30);
        menu.addView(logo);

        String[] items = new String[] {"Live TV", "Films", "Séries", "Sources IPTV", "Paramètres"};
        for (String item : items) {
            Button button = new Button(this);
            button.setText(item);
            button.setTextSize(18);
            button.setAllCaps(false);
            button.setFocusable(true);
            button.setPadding(12, 12, 12, 12);
            button.setOnClickListener(v -> status.setText("Section sélectionnée : " + ((Button) v).getText()));
            menu.addView(button, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    72
            ));
        }

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_VERTICAL);
        content.setPadding(60, 0, 0, 0);

        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        root.addView(content, contentParams);

        TextView title = new TextView(this);
        title.setText("FocusTV");
        title.setTextColor(Color.WHITE);
        title.setTextSize(56);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        content.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("APK Android TV / Firestick généré automatiquement. Navigation télécommande prête.");
        subtitle.setTextColor(Color.rgb(203, 213, 225));
        subtitle.setTextSize(22);
        subtitle.setPadding(0, 20, 0, 20);
        content.addView(subtitle);

        status = new TextView(this);
        status.setText("Utilise la télécommande : haut, bas, gauche, droite, OK et retour.");
        status.setTextColor(Color.rgb(148, 163, 184));
        status.setTextSize(18);
        content.addView(status);

        setContentView(root);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event);
    }
}
