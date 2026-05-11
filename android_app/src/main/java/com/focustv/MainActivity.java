package com.focustv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainActivity extends Activity {

    private LinearLayout root;
    private LinearLayout sidebar;
    private LinearLayout content;
    private TextView heroTitle;
    private TextView heroSubtitle;
    private TextView heroInfo;
    private TextView topStatus;

    private SharedPreferences prefs;
    private ArrayList<Source> sources = new ArrayList<Source>();
    private ArrayList<IptvItem> liveCache = new ArrayList<IptvItem>();
    private ArrayList<IptvItem> vodCache = new ArrayList<IptvItem>();
    private ArrayList<IptvItem> seriesCache = new ArrayList<IptvItem>();

    private final int bg = Color.rgb(5, 8, 22);
    private final int panel = Color.rgb(2, 6, 23);
    private final int card = Color.rgb(15, 23, 42);
    private final int card2 = Color.rgb(30, 41, 59);
    private final int blue = Color.rgb(37, 99, 235);
    private final int cyan = Color.rgb(14, 165, 233);
    private final int purple = Color.rgb(88, 28, 135);
    private final int green = Color.rgb(6, 95, 70);
    private final int orange = Color.rgb(154, 52, 18);
    private final int red = Color.rgb(127, 29, 29);
    private final int muted = Color.rgb(148, 163, 184);
    private final int soft = Color.rgb(203, 213, 225);
    private final int white = Color.WHITE;

    static class Source {
        String type = "";
        String name = "";
        String url = "";
        String server = "";
        String username = "";
        String password = "";
        String mac = "";
        String portal = "";
    }

    static class IptvItem {
        String type = "";
        String title = "";
        String url = "";
        String category = "";
        String source = "";
        String logo = "";

        IptvItem(String t, String name, String link, String cat, String src, String icon) {
            type = t;
            title = name;
            url = link;
            category = cat;
            source = src;
            logo = icon;
        }
    }

    private interface FormHandler {
        void onSubmit(String[] values);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        prefs = getSharedPreferences("FocusTVPrefs", MODE_PRIVATE);
        loadSources();
        openHome();
    }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        return d;
    }

    private GradientDrawable stroke(int color, int strokeColor, int radius, int width) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        d.setStroke(width, strokeColor);
        return d;
    }

    private TextView txt(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setIncludeFontPadding(true);
        if (bold) {
            t.setTypeface(Typeface.DEFAULT_BOLD);
        }
        return t;
    }

    private View space(int w, int h) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(w, h));
        return v;
    }

    private String safe(String v) {
        return v == null ? "" : v;
    }

    private String enc(String v) {
        try {
            return URLEncoder.encode(safe(v), "UTF-8");
        } catch (Exception e) {
            return safe(v);
        }
    }

    private String cleanServer(String server) {
        String s = safe(server).trim();
        while (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    private Button tvButton(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(17);
        b.setTextColor(white);
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        b.setPadding(22, 0, 14, 0);
        b.setFocusable(true);
        b.setBackground(stroke(card, Color.rgb(51, 65, 85), 18, 2));
        b.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean focus) {
                if (focus) {
                    v.setScaleX(1.05f);
                    v.setScaleY(1.05f);
                    v.setBackground(stroke(blue, Color.rgb(147, 197, 253), 18, 4));
                } else {
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);
                    v.setBackground(stroke(card, Color.rgb(51, 65, 85), 18, 2));
                }
            }
        });
        return b;
    }

    private TextView pill(String value) {
        TextView p = txt(value, 15, white, true);
        p.setGravity(Gravity.CENTER);
        p.setPadding(18, 8, 18, 8);
        p.setBackground(stroke(card, Color.rgb(51, 65, 85), 30, 2));
        return p;
    }

    private void buildShell(String active) {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setBackgroundColor(bg);

        sidebar = new LinearLayout(this);
        sidebar.setOrientation(LinearLayout.VERTICAL);
        sidebar.setPadding(26, 34, 26, 26);
        sidebar.setBackgroundColor(panel);
        root.addView(sidebar, new LinearLayout.LayoutParams(330, LinearLayout.LayoutParams.MATCH_PARENT));

        TextView logo = txt("FocusTV", 34, white, true);
        logo.setPadding(0, 0, 0, 24);
        sidebar.addView(logo);

        addMenuButton("Accueil", active, new View.OnClickListener() { public void onClick(View v) { openHome(); } });
        addMenuButton("Sources", active, new View.OnClickListener() { public void onClick(View v) { openSources(); } });
        addMenuButton("Live TV", active, new View.OnClickListener() { public void onClick(View v) { openContent("live"); } });
        addMenuButton("Films", active, new View.OnClickListener() { public void onClick(View v) { openContent("vod"); } });
        addMenuButton("Séries", active, new View.OnClickListener() { public void onClick(View v) { openContent("series"); } });
        addMenuButton("Favoris", active, new View.OnClickListener() { public void onClick(View v) { openFavorites(); } });
        addMenuButton("Historique", active, new View.OnClickListener() { public void onClick(View v) { openHistory(); } });
        addMenuButton("Paramètres", active, new View.OnClickListener() { public void onClick(View v) { openSettings(); } });

        sidebar.addView(space(1, 1), new LinearLayout.LayoutParams(1, 0, 1));
        TextView version = txt("v15 • IPTV TV Engine", 14, muted, false);
        version.setPadding(0, 18, 0, 0);
        sidebar.addView(version);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(46, 38, 46, 42);
        content.setBackgroundColor(bg);
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1));

        setContentView(root);
    }

    private void addMenuButton(String label, String active, View.OnClickListener listener) {
        Button b = tvButton(label);
        b.setOnClickListener(listener);
        if (label.equals(active)) {
            b.setBackground(stroke(Color.rgb(30, 64, 175), Color.rgb(147, 197, 253), 18, 3));
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 64);
        params.setMargins(0, 0, 0, 11);
        sidebar.addView(b, params);
    }

    private void addHero(String title, String subtitle, String info) {
        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(42, 34, 42, 34);
        hero.setBackground(stroke(Color.rgb(8, 13, 34), blue, 28, 3));

        LinearLayout line = new LinearLayout(this);
        line.setOrientation(LinearLayout.HORIZONTAL);
        line.addView(pill("FOCUSTV IPTV TV"));
        line.addView(space(12, 1));
        line.addView(pill("M3U"));
        line.addView(space(12, 1));
        line.addView(pill("XTREAM"));
        line.addView(space(12, 1));
        line.addView(pill("MAC"));
        hero.addView(line);

        heroTitle = txt(title, 54, white, true);
        heroTitle.setPadding(0, 18, 0, 0);
        hero.addView(heroTitle);

        heroSubtitle = txt(subtitle, 22, soft, false);
        heroSubtitle.setPadding(0, 8, 0, 8);
        hero.addView(heroSubtitle);

        heroInfo = txt(info, 17, muted, false);
        hero.addView(heroInfo);

        content.addView(hero, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    private TextView title(String s) {
        TextView t = txt(s, 28, white, true);
        t.setPadding(0, 32, 0, 16);
        return t;
    }

    private View cardView(String title, String subtitle, int color, View.OnClickListener click) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(22, 20, 22, 20);
        c.setFocusable(true);
        c.setBackground(stroke(color, Color.rgb(51, 65, 85), 24, 2));
        c.setOnClickListener(click);
        c.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean focus) {
                if (focus) {
                    v.setScaleX(1.05f);
                    v.setScaleY(1.05f);
                    v.setBackground(stroke(blue, Color.rgb(147, 197, 253), 24, 4));
                } else {
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);
                    v.setBackground(stroke(color, Color.rgb(51, 65, 85), 24, 2));
                }
            }
        });

        c.addView(txt(title, 22, white, true));
        TextView sub = txt(subtitle, 15, soft, false);
        sub.setPadding(0, 8, 0, 0);
        c.addView(sub);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(330, 150);
        p.setMargins(0, 0, 22, 0);
        c.setLayoutParams(p);
        return c;
    }

    private void addHorizontalCards(View[] views) {
        HorizontalScrollView hs = new HorizontalScrollView(this);
        hs.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < views.length; i++) {
            row.addView(views[i]);
        }
        hs.addView(row);
        content.addView(hs);
    }

    private void openHome() {
        buildShell("Accueil");
        addHero("FocusTV", "Application IPTV Android TV / Firestick complète : sources, live, VOD, séries et lecteur.", "Navigation télécommande • Sauvegarde locale • GitHub APK automatique");
        content.addView(title("Accès rapide"));
        addHorizontalCards(new View[] {
                cardView("Sources IPTV", sources.size() + " source(s) enregistrée(s)", orange, new View.OnClickListener() { public void onClick(View v) { openSources(); } }),
                cardView("Live TV", "Charger les chaînes", blue, new View.OnClickListener() { public void onClick(View v) { openContent("live"); } }),
                cardView("Films", "VOD depuis Xtream", purple, new View.OnClickListener() { public void onClick(View v) { openContent("vod"); } }),
                cardView("Séries", "Séries depuis Xtream", green, new View.OnClickListener() { public void onClick(View v) { openContent("series"); } })
        });

        content.addView(title("Continuer"));
        String last = prefs.getString("last_title", "");
        String lastUrl = prefs.getString("last_url", "");
        if (last.length() > 0 && lastUrl.length() > 0) {
            final IptvItem item = new IptvItem("history", last, lastUrl, "Historique", "FocusTV", "");
            addHorizontalCards(new View[] {
                    cardView(last, "Reprendre la lecture", red, new View.OnClickListener() { public void onClick(View v) { play(item); } }),
                    cardView("Historique", "Voir les derniers contenus", card2, new View.OnClickListener() { public void onClick(View v) { openHistory(); } })
            });
        } else {
            content.addView(infoBox("Aucun historique pour l’instant. Ajoute une source IPTV puis lance un flux."));
        }
    }

    private View infoBox(String message) {
        TextView t = txt(message, 18, soft, false);
        t.setPadding(24, 20, 24, 20);
        t.setBackground(stroke(card, Color.rgb(51, 65, 85), 20, 2));
        return t;
    }

    private void openSources() {
        buildShell("Sources");
        addHero("Sources IPTV", "Ajoute et gère tes accès M3U, Xtream Codes et MAC/Stalker.", "Les sources sont sauvegardées localement sur l’appareil.");

        content.addView(title("Ajouter une source"));
        addHorizontalCards(new View[] {
                cardView("Ajouter M3U", "URL playlist .m3u/.m3u8", blue, new View.OnClickListener() { public void onClick(View v) { showM3UForm(); } }),
                cardView("Ajouter Xtream", "Serveur + identifiants", green, new View.OnClickListener() { public void onClick(View v) { showXtreamForm(); } }),
                cardView("Ajouter MAC", "Portal + adresse MAC", orange, new View.OnClickListener() { public void onClick(View v) { showMacForm(); } }),
                cardView("Recharger", "Relire les sources", card2, new View.OnClickListener() { public void onClick(View v) { loadSources(); openSources(); } })
        });

        content.addView(title("Sources enregistrées"));

        if (sources.size() == 0) {
            content.addView(infoBox("Aucune source enregistrée. Ajoute une source M3U ou Xtream pour charger du contenu réel."));
        } else {
            for (int i = 0; i < sources.size(); i++) {
                final int index = i;
                Source s = sources.get(i);
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setPadding(0, 0, 0, 14);

                row.addView(cardView(s.name, s.type + " • " + displaySourceInfo(s), card2, new View.OnClickListener() {
                    public void onClick(View v) {
                        openContent("live");
                    }
                }));

                row.addView(cardView("Supprimer", "Effacer cette source", red, new View.OnClickListener() {
                    public void onClick(View v) {
                        confirmDeleteSource(index);
                    }
                }));

                content.addView(row);
            }
        }
    }

    private String displaySourceInfo(Source s) {
        if ("M3U".equals(s.type)) {
            return s.url;
        }
        if ("XTREAM".equals(s.type)) {
            return s.server;
        }
        if ("MAC".equals(s.type)) {
            return s.portal + " • " + s.mac;
        }
        return "";
    }

    private void showM3UForm() {
        showForm("Ajouter M3U", new String[] {"Nom de la source", "URL M3U"}, new FormHandler() {
            public void onSubmit(String[] v) {
                Source s = new Source();
                s.type = "M3U";
                s.name = v[0].length() == 0 ? "M3U" : v[0];
                s.url = v[1];
                sources.add(s);
                saveSources();
                Toast.makeText(MainActivity.this, "Source M3U ajoutée", Toast.LENGTH_SHORT).show();
                openSources();
            }
        });
    }

    private void showXtreamForm() {
        showForm("Ajouter Xtream Codes", new String[] {"Nom", "Serveur http://...", "Utilisateur", "Mot de passe"}, new FormHandler() {
            public void onSubmit(String[] v) {
                Source s = new Source();
                s.type = "XTREAM";
                s.name = v[0].length() == 0 ? "Xtream" : v[0];
                s.server = v[1];
                s.username = v[2];
                s.password = v[3];
                sources.add(s);
                saveSources();
                Toast.makeText(MainActivity.this, "Source Xtream ajoutée", Toast.LENGTH_SHORT).show();
                openSources();
            }
        });
    }

    private void showMacForm() {
        showForm("Ajouter MAC/Stalker", new String[] {"Nom", "Portal URL", "Adresse MAC"}, new FormHandler() {
            public void onSubmit(String[] v) {
                Source s = new Source();
                s.type = "MAC";
                s.name = v[0].length() == 0 ? "MAC/Stalker" : v[0];
                s.portal = v[1];
                s.mac = v[2];
                sources.add(s);
                saveSources();
                Toast.makeText(MainActivity.this, "Source MAC préparée", Toast.LENGTH_SHORT).show();
                openSources();
            }
        });
    }

    private void showForm(String title, String[] labels, final FormHandler handler) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(18, 10, 18, 0);

        final EditText[] inputs = new EditText[labels.length];

        for (int i = 0; i < labels.length; i++) {
            EditText e = new EditText(this);
            e.setHint(labels[i]);
            e.setSingleLine(true);
            e.setTextColor(Color.BLACK);
            e.setHintTextColor(Color.DKGRAY);
            inputs[i] = e;
            box.addView(e);
        }

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(box)
                .setPositiveButton("Enregistrer", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String[] values = new String[inputs.length];
                        for (int i = 0; i < inputs.length; i++) {
                            values[i] = inputs[i].getText().toString().trim();
                        }
                        handler.onSubmit(values);
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void confirmDeleteSource(final int index) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer la source ?")
                .setMessage("Cette action efface la source de l’appareil.")
                .setPositiveButton("Supprimer", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (index >= 0 && index < sources.size()) {
                            sources.remove(index);
                            saveSources();
                            openSources();
                        }
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void saveSources() {
        try {
            JSONArray arr = new JSONArray();
            for (int i = 0; i < sources.size(); i++) {
                Source s = sources.get(i);
                JSONObject o = new JSONObject();
                o.put("type", s.type);
                o.put("name", s.name);
                o.put("url", s.url);
                o.put("server", s.server);
                o.put("username", s.username);
                o.put("password", s.password);
                o.put("mac", s.mac);
                o.put("portal", s.portal);
                arr.put(o);
            }
            prefs.edit().putString("sources", arr.toString()).apply();
        } catch (Exception e) {
            Toast.makeText(this, "Erreur sauvegarde sources", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSources() {
        sources.clear();
        try {
            String raw = prefs.getString("sources", "[]");
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                Source s = new Source();
                s.type = o.optString("type", "");
                s.name = o.optString("name", "");
                s.url = o.optString("url", "");
                s.server = o.optString("server", "");
                s.username = o.optString("username", "");
                s.password = o.optString("password", "");
                s.mac = o.optString("mac", "");
                s.portal = o.optString("portal", "");
                sources.add(s);
            }
        } catch (Exception e) {
            sources.clear();
        }
    }

    private void openContent(final String type) {
        String active = "Live TV";
        String label = "Live TV";
        String sub = "Chargement des chaînes depuis les sources enregistrées.";
        if ("vod".equals(type)) {
            active = "Films";
            label = "Films";
            sub = "Chargement VOD depuis Xtream Codes.";
        } else if ("series".equals(type)) {
            active = "Séries";
            label = "Séries";
            sub = "Chargement séries depuis Xtream Codes.";
        }

        buildShell(active);
        addHero(label, sub, "Sources actives : " + sources.size());
        topStatus = txt("Chargement en cours...", 18, soft, false);
        topStatus.setPadding(0, 28, 0, 0);
        content.addView(topStatus);

        loadContent(type);
    }

    private void loadContent(final String type) {
        new Thread(new Runnable() {
            public void run() {
                final ArrayList<IptvItem> result = new ArrayList<IptvItem>();
                final StringBuilder log = new StringBuilder();

                for (int i = 0; i < sources.size(); i++) {
                    Source s = sources.get(i);
                    try {
                        if ("M3U".equals(s.type) && "live".equals(type)) {
                            String body = httpGet(s.url);
                            result.addAll(parseM3U(body, s.name));
                            log.append(s.name).append(" OK • ");
                        } else if ("XTREAM".equals(s.type)) {
                            if ("live".equals(type)) {
                                result.addAll(loadXtreamLive(s));
                            } else if ("vod".equals(type)) {
                                result.addAll(loadXtreamVod(s));
                            } else if ("series".equals(type)) {
                                result.addAll(loadXtreamSeries(s));
                            }
                            log.append(s.name).append(" OK • ");
                        } else if ("MAC".equals(s.type)) {
                            log.append(s.name).append(" MAC préparé • ");
                        }
                    } catch (Exception e) {
                        log.append(s.name).append(" erreur • ");
                    }
                }

                runOnUiThread(new Runnable() {
                    public void run() {
                        if ("live".equals(type)) {
                            liveCache = result;
                        } else if ("vod".equals(type)) {
                            vodCache = result;
                        } else if ("series".equals(type)) {
                            seriesCache = result;
                        }
                        renderItems(type, result, log.toString());
                    }
                });
            }
        }).start();
    }

    private String httpGet(String link) throws Exception {
        URL url = new URL(link);
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setConnectTimeout(15000);
        c.setReadTimeout(25000);
        c.setRequestProperty("User-Agent", "FocusTV/15 AndroidTV");
        c.setRequestProperty("Accept", "*/*");

        int code = c.getResponseCode();
        BufferedReader br = new BufferedReader(new InputStreamReader(
                code >= 200 && code < 400 ? c.getInputStream() : c.getErrorStream()
        ));

        StringBuilder out = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            out.append(line).append("\n");
        }
        br.close();

        if (code < 200 || code >= 400) {
            throw new Exception("HTTP " + code);
        }

        return out.toString();
    }

    private String attr(String line, String key) {
        String needle = key + "=\"";
        int start = line.indexOf(needle);
        if (start < 0) {
            return "";
        }
        start += needle.length();
        int end = line.indexOf("\"", start);
        if (end < 0) {
            return "";
        }
        return line.substring(start, end);
    }

    private ArrayList<IptvItem> parseM3U(String body, String sourceName) {
        ArrayList<IptvItem> items = new ArrayList<IptvItem>();
        String[] lines = body.split("\\r?\\n");
        String name = "";
        String group = "Live";
        String logo = "";

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            if (line.startsWith("#EXTINF")) {
                int comma = line.lastIndexOf(",");
                name = comma >= 0 ? line.substring(comma + 1).trim() : "Chaîne";
                group = attr(line, "group-title");
                logo = attr(line, "tvg-logo");
                if (group.length() == 0) {
                    group = "Live";
                }
            } else if ((line.startsWith("http://") || line.startsWith("https://")) && name.length() > 0) {
                items.add(new IptvItem("live", name, line, group, sourceName, logo));
                name = "";
                group = "Live";
                logo = "";
            }
        }

        return items;
    }

    private ArrayList<IptvItem> loadXtreamLive(Source s) throws Exception {
        String url = cleanServer(s.server) + "/player_api.php?username=" + enc(s.username) + "&password=" + enc(s.password) + "&action=get_live_streams";
        JSONArray arr = new JSONArray(httpGet(url));
        ArrayList<IptvItem> items = new ArrayList<IptvItem>();

        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            String name = o.optString("name", "Chaîne");
            String id = o.optString("stream_id", "");
            String icon = o.optString("stream_icon", "");
            String cat = o.optString("category_id", "Live");
            String link = cleanServer(s.server) + "/live/" + enc(s.username) + "/" + enc(s.password) + "/" + id + ".ts";
            items.add(new IptvItem("live", name, link, "Catégorie " + cat, s.name, icon));
        }

        return items;
    }

    private ArrayList<IptvItem> loadXtreamVod(Source s) throws Exception {
        String url = cleanServer(s.server) + "/player_api.php?username=" + enc(s.username) + "&password=" + enc(s.password) + "&action=get_vod_streams";
        JSONArray arr = new JSONArray(httpGet(url));
        ArrayList<IptvItem> items = new ArrayList<IptvItem>();

        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            String name = o.optString("name", "Film");
            String id = o.optString("stream_id", "");
            String icon = o.optString("stream_icon", "");
            String ext = o.optString("container_extension", "mp4");
            String cat = o.optString("category_id", "VOD");
            String link = cleanServer(s.server) + "/movie/" + enc(s.username) + "/" + enc(s.password) + "/" + id + "." + ext;
            items.add(new IptvItem("vod", name, link, "Catégorie " + cat, s.name, icon));
        }

        return items;
    }

    private ArrayList<IptvItem> loadXtreamSeries(Source s) throws Exception {
        String url = cleanServer(s.server) + "/player_api.php?username=" + enc(s.username) + "&password=" + enc(s.password) + "&action=get_series";
        JSONArray arr = new JSONArray(httpGet(url));
        ArrayList<IptvItem> items = new ArrayList<IptvItem>();

        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            String name = o.optString("name", "Série");
            String icon = o.optString("cover", "");
            String cat = o.optString("category_id", "Séries");
            items.add(new IptvItem("series", name, "", "Catégorie " + cat, s.name, icon));
        }

        return items;
    }

    private void renderItems(final String type, ArrayList<IptvItem> items, String log) {
        String active = "Live TV";
        String title = "Live TV";
        if ("vod".equals(type)) {
            active = "Films";
            title = "Films";
        } else if ("series".equals(type)) {
            active = "Séries";
            title = "Séries";
        }

        buildShell(active);
        addHero(title, items.size() + " élément(s) chargé(s).", log.length() == 0 ? "Aucune source chargée." : log);

        if (items.size() == 0) {
            content.addView(title("Aucun contenu"));
            content.addView(infoBox("Ajoute une source dans Sources IPTV. Pour VOD/Séries, une source Xtream est nécessaire."));
            return;
        }

        content.addView(title("Catégories"));
        addCategoryRow(type, items);

        content.addView(title("Liste"));
        addItemGrid(items);
    }

    private void addCategoryRow(final String type, final ArrayList<IptvItem> items) {
        LinkedHashMap<String, Integer> cats = new LinkedHashMap<String, Integer>();
        for (int i = 0; i < items.size(); i++) {
            String c = items.get(i).category;
            if (c == null || c.length() == 0) {
                c = "Autres";
            }
            Integer count = cats.get(c);
            cats.put(c, count == null ? 1 : count + 1);
        }

        HorizontalScrollView hs = new HorizontalScrollView(this);
        hs.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        row.addView(cardView("Toutes", items.size() + " éléments", blue, new View.OnClickListener() {
            public void onClick(View v) { renderItems(type, items, "Toutes les catégories"); }
        }));

        for (final Map.Entry<String, Integer> e : cats.entrySet()) {
            row.addView(cardView(e.getKey(), e.getValue() + " éléments", card2, new View.OnClickListener() {
                public void onClick(View v) {
                    ArrayList<IptvItem> filtered = new ArrayList<IptvItem>();
                    for (int i = 0; i < items.size(); i++) {
                        if (e.getKey().equals(items.get(i).category)) {
                            filtered.add(items.get(i));
                        }
                    }
                    renderItems(type, filtered, "Filtre : " + e.getKey());
                }
            }));
        }

        hs.addView(row);
        content.addView(hs);
    }

    private void addItemGrid(ArrayList<IptvItem> items) {
        LinearLayout row = null;

        for (int i = 0; i < items.size(); i++) {
            if (i % 3 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setPadding(0, 0, 0, 18);
                content.addView(row);
            }

            final IptvItem item = items.get(i);
            View card = cardView(item.title, item.category + " • " + item.source, itemColor(item.type, i), new View.OnClickListener() {
                public void onClick(View v) {
                    if (item.url.length() > 0) {
                        play(item);
                    } else {
                        Toast.makeText(MainActivity.this, "Série détectée. Épisodes à ajouter dans la prochaine version.", Toast.LENGTH_LONG).show();
                    }
                }
            });

            if (row != null) {
                row.addView(card);
            }
        }
    }

    private int itemColor(String type, int i) {
        if ("live".equals(type)) {
            return i % 2 == 0 ? Color.rgb(30, 64, 175) : Color.rgb(15, 23, 42);
        }
        if ("vod".equals(type)) {
            return i % 2 == 0 ? Color.rgb(88, 28, 135) : Color.rgb(15, 23, 42);
        }
        if ("series".equals(type)) {
            return i % 2 == 0 ? Color.rgb(6, 95, 70) : Color.rgb(15, 23, 42);
        }
        return card2;
    }

    private void play(final IptvItem item) {
        prefs.edit()
                .putString("last_title", item.title)
                .putString("last_url", item.url)
                .apply();
        addHistory(item);

        FrameLayout frame = new FrameLayout(this);
        frame.setBackgroundColor(Color.BLACK);

        final VideoView video = new VideoView(this);
        video.setVideoPath(item.url);
        MediaController controller = new MediaController(this);
        controller.setAnchorView(video);
        video.setMediaController(controller);

        TextView overlay = txt(item.title + "  •  Retour pour quitter", 18, white, true);
        overlay.setPadding(24, 18, 24, 18);
        overlay.setBackgroundColor(Color.argb(160, 0, 0, 0));

        frame.addView(video, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        FrameLayout.LayoutParams overlayParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        overlayParams.gravity = Gravity.TOP;
        frame.addView(overlay, overlayParams);

        setContentView(frame);

        video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                video.start();
            }
        });

        video.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(MainActivity.this, "Erreur lecture du flux", Toast.LENGTH_LONG).show();
                openHome();
                return true;
            }
        });

        video.requestFocus();
    }

    private void addHistory(IptvItem item) {
        try {
            JSONArray arr = new JSONArray(prefs.getString("history", "[]"));
            JSONArray out = new JSONArray();

            JSONObject first = new JSONObject();
            first.put("title", item.title);
            first.put("url", item.url);
            first.put("type", item.type);
            first.put("category", item.category);
            first.put("source", item.source);
            out.put(first);

            for (int i = 0; i < arr.length() && i < 29; i++) {
                out.put(arr.getJSONObject(i));
            }

            prefs.edit().putString("history", out.toString()).apply();
        } catch (Exception e) {
        }
    }

    private void openHistory() {
        buildShell("Historique");
        addHero("Historique", "Derniers contenus lancés sur FocusTV.", "Stockage local uniquement.");

        try {
            JSONArray arr = new JSONArray(prefs.getString("history", "[]"));
            if (arr.length() == 0) {
                content.addView(infoBox("Aucun historique pour l’instant."));
                return;
            }

            ArrayList<IptvItem> items = new ArrayList<IptvItem>();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                items.add(new IptvItem(
                        o.optString("type", "history"),
                        o.optString("title", ""),
                        o.optString("url", ""),
                        o.optString("category", "Historique"),
                        o.optString("source", "FocusTV"),
                        ""
                ));
            }
            addItemGrid(items);
        } catch (Exception e) {
            content.addView(infoBox("Historique illisible."));
        }
    }

    private void openFavorites() {
        buildShell("Favoris");
        addHero("Favoris", "Zone prête pour les chaînes et contenus favoris.", "La sélection favoris sera branchée sur les listes réelles.");
        content.addView(infoBox("Les favoris sont préparés dans l’interface. Prochaine étape : bouton Ajouter/Retirer favori sur chaque chaîne."));
    }

    private void openSettings() {
        buildShell("Paramètres");
        addHero("Paramètres", "Réglages FocusTV : cache, DNS, lecteur, EPG, thème.", "v15 — moteur IPTV Android TV.");

        content.addView(title("Maintenance"));
        addHorizontalCards(new View[] {
                cardView("Recharger sources", "Relire la configuration locale", blue, new View.OnClickListener() { public void onClick(View v) { loadSources(); openSettings(); } }),
                cardView("Effacer historique", "Supprimer les derniers contenus", orange, new View.OnClickListener() { public void onClick(View v) { prefs.edit().remove("history").remove("last_title").remove("last_url").apply(); openSettings(); } }),
                cardView("Effacer sources", "Remettre à zéro", red, new View.OnClickListener() { public void onClick(View v) { confirmClearSources(); } })
        });

        content.addView(title("État"));
        content.addView(infoBox("Sources : " + sources.size() + "\nLive cache : " + liveCache.size() + "\nVOD cache : " + vodCache.size() + "\nSéries cache : " + seriesCache.size()));
    }

    private void confirmClearSources() {
        new AlertDialog.Builder(this)
                .setTitle("Effacer toutes les sources ?")
                .setMessage("Toutes les sources IPTV enregistrées seront supprimées.")
                .setPositiveButton("Effacer", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        sources.clear();
                        saveSources();
                        openSettings();
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        openHome();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event);
    }
}
