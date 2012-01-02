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
    String token = session.get("accessToken");
    String tokenSecret = session.get("accessTokenSecret");

    if (token != null && tokenSecret != null) {
      return new Rdio(ConsumerCredentials.consumer, new Rdio.Token(token, tokenSecret));
    } else {
      return new Rdio(ConsumerCredentials.consumer);
    }
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
    Rdio.AuthState authState = rdio.beginAuthentication(Router.getFullUrl("Application.callback"));
    session.put("requestToken", authState.requestToken.token);
    session.put("requestTokenSecret", authState.requestToken.secret);
    session.remove("accessToken", "accessTokenSecret");
    redirect(authState.url);
  }

  public static void callback(String oauth_verifier) throws IOException {
    if (oauth_verifier != null) {
      Rdio rdio = getRdio();
      Rdio.Token requestToken = new Rdio.Token(session.get("requestToken"), session.get("requestTokenSecret"));
      Rdio.Token accessToken = rdio.completeAuthentication(oauth_verifier, requestToken);
      session.put("accessToken", accessToken.token);
      session.put("accessTokenSecret", accessToken.secret);
    }
    session.remove("requestToken", "requestTokenSecret");
    redirect(Router.getFullUrl("Application.index"));
  }

  public static void logout() {
    session.remove("accessToken", "accessTokenSecret");
    redirect(Router.getFullUrl("Application.index"));
  }
}