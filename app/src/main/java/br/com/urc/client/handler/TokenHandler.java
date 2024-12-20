package br.com.urc.client.handler;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenHandler {

    private final String PREFERENCES_NAME = "Tokens";

    private Context context;

    public TokenHandler(Context context) {
        this.context = context;
    }

    public void save(String id, String token) {
        SharedPreferences.Editor editor = getEditor();
        editor.putString(id, token);
        editor.commit();
    }

    public void remove(String id) {
        getEditor().remove(id);
    }

    public String get(String id) {
        return getSharedPreferences().getString(id, null);
    }

    private SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }


    private SharedPreferences.Editor getEditor() {
        return getSharedPreferences().edit();
    }

}
