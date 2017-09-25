package it.fuoricitta.update;

/**
 * Created by alessandrorosa on 21/08/2017.
 */

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.Date;
import java.util.Objects;


public class UpdateManager {

    public static UpdateManager instance = new UpdateManager();
    private static UpdateCallback updateCallback = null;

    public enum AlertType {
        UPDATE,
        ALERT
    }

    public enum UpdateType {
        NONE {
            public String toString() {
                return "none";
            }
        },

        REQUIRED {
            public String toString() {
                return "required";
            }
        },

        OPTIONAL {
            public String toString() {
                return "optional";
            }
        }
    }

    private static Context ctx;

    private static String BaseUrl = "http://app.getupdate.it/";

    static String Endpoint = "api/v1/updates/";

    static String token;
    String DeviceId;
    String version;


    public void setup(Context ctx, String token) {
        UpdateManager.ctx = ctx;
        UpdateManager.token = token;

        DeviceId = Settings.Secure.getString(UpdateManager.ctx.getContentResolver(), Settings.Secure.ANDROID_ID);

        PackageManager manager = ctx.getPackageManager();
        PackageInfo info = null;

        try {
            info = manager.getPackageInfo(ctx.getPackageName(), 0);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();

        }

        assert info != null;
        version = info.versionName;
    }


    public void askForUpdate(final Activity activity, UpdateCallback updateCallback) {
        this.updateCallback = updateCallback;

        RequestQueue queue = Volley.newRequestQueue(ctx);
        String url = BaseUrl + Endpoint + token + "/" + DeviceId + "/" + version + "/";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject updateObject;
                try {
                    updateObject = new JSONObject(response);

                    try {
                        Update.map(updateObject.getJSONObject("update"), new UpdateCallback() {
                            @Override
                            public void block(Object object) {
                                Update update = (Update) object;
                                update.isUpdate = true;

                                UpdateManager.instance.showAlert(activity, activity, update, AlertType.UPDATE);
                            }
                        });
                    } catch (Exception ignored) {
                    }

                    try {
                        Update.map(updateObject.getJSONObject("alert"), new UpdateCallback() {
                            @Override
                            public void block(Object object) {
                                Update alert = (Update) object;
                                alert.isUpdate = false;

                                UpdateManager.instance.showAlert(activity, activity, alert, AlertType.ALERT);
                            }
                        });
                    } catch (Exception ignored) {
                    }

                } catch (Exception e) {
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        queue.add(stringRequest);
    }


    // GOOGLE PLAY STORE

    private static void launchStore(Activity activity) {
        Uri uri = Uri.parse("market://details?id=" + activity.getPackageName());
        Intent linkToStore = new Intent(Intent.ACTION_VIEW, uri);
        try {
            activity.startActivity(linkToStore);
        } catch (ActivityNotFoundException e) {
            Log.e("E", e.getLocalizedMessage());
        }
    }


    // Callbacks

    public interface UpdateCallback<T> {
        void block(T object);
    }


    private void showAlert(final Context context, final Activity activity, Update update, UpdateManager.AlertType alertType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);

        switch (alertType) {
            case UPDATE:

                if (!Objects.equals(update.update, UpdateType.OPTIONAL.toString()) || Objects.equals(update.update, UpdateType.OPTIONAL.toString()) && canShowGetUpdate(getMuteFrom())) {

                    String TitleUpdate = "New version " + update.version + " available";
                    String MessageUpdate = "A new version of " + update.app.name + " is available on the Google Play Store. Get the latest features to bla bla bla..";
                    String ActionAskLaterTitle = "Ask me later";
                    String ActionUpdateTitle = "Update";

                    builder.setTitle(TitleUpdate);
                    builder.setMessage(MessageUpdate);

                    if (Objects.equals(update.update, UpdateManager.UpdateType.OPTIONAL.toString())) {
                        builder.setNegativeButton(ActionAskLaterTitle, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                UpdateManager.setMuteFrom();

                                if (updateCallback != null) {
                                    updateCallback.block(null);
                                }
                            }
                        });
                    }

                    builder.setPositiveButton(ActionUpdateTitle, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            UpdateManager.launchStore(activity);

                            if (updateCallback != null) {
                                updateCallback.block(null);
                            }
                        }
                    });

                    builder.show();
                }

                break;

            case ALERT:
                String TitleAlert = "Your App is up to date!";
                String MessageAlert = "\nWhat's new in this version:\n\n" + update.description;
                String ActionOkTitle = "Got it";

                builder.setTitle(TitleAlert);
                builder.setMessage(MessageAlert);
                builder.setNegativeButton(ActionOkTitle, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                builder.show();
        }

    }


    // ASK LATER UTILS

    private static Date now = new Date();
    private static long day = 60 * 60 * 24; //60 * 60 * 24;

    private static long dateToMillis(Date date) {
        return date.getTime() / 1000;
    }

    private static boolean canShowGetUpdate(long before) {
        return (dateToMillis(now) - before) > day;
    }

    private static void setMuteFrom() {
        SharedPreferences sharedPref = UpdateManager.ctx.getSharedPreferences("GET_UPDATE", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong("GET_UPDATE_MUTE", UpdateManager.dateToMillis(new Date()));
        editor.apply();
    }

    private static long getMuteFrom() {
        SharedPreferences sharedPref = UpdateManager.ctx.getSharedPreferences("GET_UPDATE", Context.MODE_PRIVATE);
        return sharedPref.getLong("GET_UPDATE_MUTE", 0);
    }

}