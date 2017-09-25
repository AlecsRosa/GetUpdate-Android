package it.fuoricitta.update;

import org.json.JSONException;
import org.json.JSONObject;

public class App {

    public int id;
    public String token;
    public String url;
    public String name;
    public String description;
    public String image;
    public int storeType;
    public String storeId;
    public int pushKey;
    public int team;


    private App() {}


    // MAPPING
    static void map(JSONObject JSON, UpdateManager.UpdateCallback callback) {
        App app = new App();

        try {
            app.name = JSON.getString("name");
            app.storeId = JSON.getString("store_id");

            callback.block(app);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
