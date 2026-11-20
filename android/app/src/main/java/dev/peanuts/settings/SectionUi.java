package dev.peanuts.settings;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import dev.nxnative.android.R;
import dev.nxnative.android.Tokens;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Upstream's look for the PEANUTS sections of a game's settings page (CompanionSection, ModsSection), through reflection (Ui is package-private): Ui.section(Context,
 * CharSequence), new Ui.Setting(Context, String emoji, String title, OnClickListener).value(CharSequence) with .chevron(Context) or .toggle(Context, boolean,
 * OnCheckedChangeListener), its .view, and Ui.button(Context, CharSequence, Ui.Kind, OnClickListener). Any failure falls back to plain views shaped like them.
 */
final class SectionUi {
    static final String PRIMARY = "PRIMARY", DANGER = "DANGER", QUIET = "QUIET";
    private static final String TAG = "peanuts";
    final Context c;
    private Class<?> ui, setting, kind;

    SectionUi(Context c) {
        this.c = c;
        try {
            ui = Class.forName("dev.nxnative.android.Ui");
            setting = Class.forName("dev.nxnative.android.Ui$Setting");
            kind = Class.forName("dev.nxnative.android.Ui$Kind");
        } catch (ClassNotFoundException e) {
            ui = null;
        }
    }

    static LinearLayout.LayoutParams lp(int bottomDp, int topDp) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.topMargin = dp(topDp); p.bottomMargin = dp(bottomDp);
        return p;
    }

    static int dp(int v) { return Math.round(v * android.content.res.Resources.getSystem().getDisplayMetrics().density); }

    /** The row of buttons under a section's row: {@link #add} puts each button in at an equal share of the width */
    static LinearLayout buttons(Context c) {
        LinearLayout buttons = new LinearLayout(c);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setPadding(dp(Tokens.SPACE_2), 0, dp(Tokens.SPACE_3), dp(Tokens.SPACE_2));
        return buttons;
    }

    static void add(LinearLayout row, View b, boolean enabled) {
        b.setEnabled(enabled);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        p.rightMargin = dp(Tokens.SPACE_2);
        row.addView(b, p);
    }

    View section(String s) {
        try {
            Method m = ui.getDeclaredMethod("section", Context.class, CharSequence.class);
            m.setAccessible(true);
            return (View) m.invoke(null, c, s);
        } catch (ReflectiveOperationException | RuntimeException e) {
            TextView t = new TextView(c);
            t.setText(s);
            t.setAllCaps(true);
            t.setLetterSpacing(0.08f);
            t.setTextSize(TypedValue.COMPLEX_UNIT_SP, Tokens.TYPE_CAPTION);
            t.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, 700, false));
            t.setPadding(dp(Tokens.SPACE_1), 0, 0, 0);
            t.setAccessibilityHeading(true);   // the reflection path's Ui.section sets it; the fallback must too
            return t;
        }
    }

    /** A row with its value line; l null: the row does nothing (no chevron) */
    View setting(String emoji, String title, String value, View.OnClickListener l) { return row(emoji, title, value, l, false, false, null); }

    /** A row with a switch at its end (a tap anywhere on the row flips it) */
    View toggle(String emoji, String title, String value, boolean on, CompoundButton.OnCheckedChangeListener l) { return row(emoji, title, value, null, true, on, l); }

    private View row(String emoji, String title, String value, View.OnClickListener l, boolean toggle, boolean on, CompoundButton.OnCheckedChangeListener tl) {
        try {
            Constructor<?> ctor = setting.getDeclaredConstructor(Context.class, String.class, String.class, View.OnClickListener.class);
            ctor.setAccessible(true);
            Object s = ctor.newInstance(c, emoji, title, l);
            Method v = setting.getDeclaredMethod("value", CharSequence.class);
            v.setAccessible(true);
            v.invoke(s, value);
            if (toggle) { Method t = setting.getDeclaredMethod("toggle", Context.class, boolean.class, CompoundButton.OnCheckedChangeListener.class); t.setAccessible(true); t.invoke(s, c, on, tl); }
            else if (l != null) { Method ch = setting.getDeclaredMethod("chevron", Context.class); ch.setAccessible(true); ch.invoke(s, c); }
            Field f = setting.getDeclaredField("view");
            f.setAccessible(true);
            return (View) f.get(s);
        } catch (ReflectiveOperationException | RuntimeException e) {
            Log.w(TAG, "Ui.Setting: " + e);
            LinearLayout row = new LinearLayout(c);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(Tokens.SPACE_2), dp(Tokens.SPACE_2), dp(Tokens.SPACE_3), dp(Tokens.SPACE_2));
            row.setMinimumHeight(dp(Tokens.ROW_DP));
            row.setOnClickListener(l);
            TextView tile = new TextView(c);
            tile.setText(emoji);
            tile.setGravity(Gravity.CENTER);
            tile.setTextSize(TypedValue.COMPLEX_UNIT_SP, Tokens.TYPE_HEADING);
            row.addView(tile, new LinearLayout.LayoutParams(dp(Tokens.TILE_DP), dp(Tokens.TILE_DP)));
            LinearLayout col = new LinearLayout(c);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setPadding(dp(Tokens.SPACE_3), 0, dp(Tokens.SPACE_2), 0);
            TextView t = new TextView(c);
            t.setText(title);
            t.setTextSize(TypedValue.COMPLEX_UNIT_SP, Tokens.TYPE_BODY);
            t.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, 600, false));
            t.setSingleLine(true);
            t.setEllipsize(TextUtils.TruncateAt.END);
            col.addView(t);
            TextView v = new TextView(c);
            v.setText(value);
            v.setTextSize(TypedValue.COMPLEX_UNIT_SP, Tokens.TYPE_SMALL);
            v.setMaxLines(2);
            v.setEllipsize(TextUtils.TruncateAt.END);
            v.setVisibility(value == null || value.isEmpty() ? View.GONE : View.VISIBLE);
            col.addView(v);
            row.addView(col, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            if (toggle) {
                Switch sw = new Switch(c);
                sw.setChecked(on);
                sw.setOnCheckedChangeListener(tl);
                int switchId = View.generateViewId();   // as Ui.Setting: the title names the switch for a screen reader
                sw.setId(switchId);
                t.setLabelFor(switchId);
                row.addView(sw);
            } else if (l != null) {
                TextView ch = new TextView(c);
                ch.setText("›");
                ch.setTextSize(TypedValue.COMPLEX_UNIT_SP, Tokens.TYPE_HEADING);
                ch.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                row.addView(ch, new LinearLayout.LayoutParams(dp(Tokens.CHEVRON_DP), dp(Tokens.CHEVRON_DP)));
            }
            return row;
        }
    }

    View button(String label, String kindName, View.OnClickListener l) {
        try {
            @SuppressWarnings({"unchecked", "rawtypes"})
            Object k = Enum.valueOf((Class) kind, kindName);
            Method m = ui.getDeclaredMethod("button", Context.class, CharSequence.class, kind, View.OnClickListener.class);
            m.setAccessible(true);
            return (View) m.invoke(null, c, label, k, l);
        } catch (ReflectiveOperationException | RuntimeException e) {
            Button b = new Button(c);
            b.setText(label);
            b.setAllCaps(false);
            b.setOnClickListener(l);
            if (kindName.equals(DANGER)) b.setTextColor(c.getColor(R.color.bn_danger));
            return b;
        }
    }
}
