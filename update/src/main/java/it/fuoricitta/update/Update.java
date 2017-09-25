package it.fuoricitta.update;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by alessandrorosa on 21/08/2017.
 */

public class Update {

    public int id;
    public App app;
    public String version;
    public String description;
    public String update;
    public String alert;

    public boolean isUpdate = true;


    public Update() {}

    public Update(String version, String description, App app) {
        this.version = version;
        this.description = description;
        this.app = app;
    }

    // MAPPING
    static void map(JSONObject JSON, final UpdateManager.UpdateCallback callback) {
        final Update update = new Update();

        try {
            update.version = JSON.getString("version");
            update.description = JSON.getString("description");
            update.update = JSON.getString("update");
            update.alert = JSON.getString("alert");

            App.map(JSON.getJSONObject("app"), new UpdateManager.UpdateCallback() {
                @Override
                public void block(Object object) {
                    update.app = (App) object;

                    callback.block(update);
                }
            });


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}