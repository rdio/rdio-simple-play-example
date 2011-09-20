package controllers;

import com.rdio.simple.ConsumerCredentials;
import org.json.JSONArray;
import org.json.JSONException;
import play.mvc.*;

import java.io.IOException;
import java.util.*;

import org.json.JSONObject;
import com.rdio.simple.Rdio;

public class Application extends Controller {
    private static Rdio getRdio() {
        String token = session.get("token");
        String tokenSecret = session.get("tokenSecret");
        Rdio rdio = new Rdio(ConsumerCredentials.RDIO_CONSUMER_KEY, ConsumerCredentials.RDIO_CONSUMER_SECRET);
        if (token != null && tokenSecret != null) {
            rdio.token = token;
            rdio.tokenSecret = tokenSecret;
        }
        return rdio;
    }
    public static void index() throws java.io.IOException, JSONException {
        Rdio rdio = getRdio();
        JSONObject currentUser;
        List<JSONObject> playlists = new ArrayList<JSONObject>();
        boolean loggedIn;
        try {
            currentUser = (new JSONObject(rdio.call("currentUser"))).getJSONObject("result");
            JSONArray playlist_array = new JSONObject(rdio.call("getPlaylists")).getJSONObject("result").getJSONArray("owned");
            for (int i=0; i<playlist_array.length(); i++) {
                playlists.add(playlist_array.getJSONObject(i));
            }
            loggedIn = true;
        } catch(IOException e) {
            currentUser = null;
            loggedIn = false;
        }
        render(loggedIn, currentUser, playlists);
    }

    public static void login() throws IOException {
        Rdio rdio = getRdio();
        rdio.token = null;
        rdio.tokenSecret = null;
        String url = rdio.beginAuthentication(Router.getFullUrl("Application.callback"));
        session.put("token", rdio.token);
        session.put("tokenSecret", rdio.tokenSecret);
        redirect(url);
    }

    public static void callback(String oauth_verifier) throws IOException {
        Rdio rdio = getRdio();
        rdio.completeAuthentication(oauth_verifier);
        session.put("token", rdio.token);
        session.put("tokenSecret", rdio.tokenSecret);
        redirect(Router.getFullUrl("Application.index"));
    }

    public static void logout() {
        session.remove("token", "tokenSecret");
        redirect(Router.getFullUrl("Application.index"));
    }
}