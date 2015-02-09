package com.example.hotyun_a.munich;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Const {
    public static final String CONSUMER_KEY = "QcL3KFeR5dNXd21gj1InExie1";
    public static final String CONSUMER_SECRET = "PIfhTUKxNwMeaZSTyVZ9JnEU7qsNd3SJgw8XoonupnHY7M30Kj";
    public static final String CALLBACK_URL = "http://digitalocean.onlinepc.com.ua:9000";
    public static final String IEXTRA_AUTH_URL = "auth_url";
    public static final String IEXTRA_OAUTH_VERIFIER = "oauth_verifier";
    public static final String IEXTRA_OAUTH_TOKEN = "oauth_token";
    public static final int FacebookId=100;
    public static final int VkId=101;
    public static final int TwitterId=102;
    public static final int GoogleId=103;
    public static final int LocalId=104;
    public static final HashMap<String, String> MOMENT_TYPES;
    public static final ArrayList<String> MOMENT_LIST;
    public static final String[] ACTIONS;
    static {
        MOMENT_TYPES = new HashMap<String, String>(9);
        MOMENT_TYPES.put("AddActivity","https://developers.google.com/+/plugins/snippet/examples/thing");
        MOMENT_TYPES.put("BuyActivity","https://developers.google.com/+/plugins/snippet/examples/a-book");
        MOMENT_TYPES.put("CheckInActivity","https://developers.google.com/+/plugins/snippet/examples/place");
        MOMENT_TYPES.put("CommentActivity","https://developers.google.com/+/plugins/snippet/examples/blog-entry");
        MOMENT_TYPES.put("CreateActivity","https://developers.google.com/+/plugins/snippet/examples/photo");
        MOMENT_TYPES.put("ListenActivity","https://developers.google.com/+/plugins/snippet/examples/song");
        MOMENT_TYPES.put("ReserveActivity","https://developers.google.com/+/plugins/snippet/examples/restaurant");
        MOMENT_TYPES.put("ReviewActivity","https://developers.google.com/+/plugins/snippet/examples/widget");
        MOMENT_LIST = new ArrayList<String>(Const.MOMENT_TYPES.keySet());
        Collections.sort(MOMENT_LIST);
        ACTIONS = MOMENT_TYPES.keySet().toArray(new String[0]);
        int count = ACTIONS.length;
        for (int i = 0; i < count; i++) {
            ACTIONS[i] = "http://schemas.google.com/" + ACTIONS[i];
        }
    }
}
