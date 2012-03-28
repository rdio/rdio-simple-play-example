package controllers;

import com.rdio.simple.RdioClient;
import com.rdio.simple.RdioCoreClient;
import org.json.JSONArray;
import org.json.JSONException;
import play.mvc.*;

import java.io.IOException;
import java.util.*;

import org.json.JSONObject;

public class Application extends Controller {
  private static RdioClient getRdio() {
    String token = session.get("accessToken");
    String tokenSecret = session.get("accessTokenSecret");
    
    RdioClient.Consumer consumer = new RdioClient.Consumer(System.getenv("RDIO_CONSUMER_KEY"),
            System.getenv("RDIO_CONSUMER_SECRET"));

    if (token != null && tokenSecret != null) {
      return new RdioCoreClient(consumer, new RdioClient.Token(token, tokenSecret));
    } else {
      return new RdioCoreClient(consumer);
    }
  }
  
  public static void index() throws java.io.IOException, JSONException {
    RdioClient rdio = getRdio();
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
    } catch(Exception e) {
      currentUser = null;
      loggedIn = false;
    }
    render(loggedIn, currentUser, playlists);
  }

  public static void login() throws IOException {
    RdioClient rdio = getRdio();
    RdioClient.AuthState authState;
    try {
      authState = rdio.beginAuthentication(Router.getFullUrl("Application.callback"));
    } catch (RdioClient.RdioException e) {
      redirect(Router.getFullUrl("Application.logout"));
      return;
    }
    session.put("requestToken", authState.requestToken.token);
    session.put("requestTokenSecret", authState.requestToken.secret);
    session.remove("accessToken", "accessTokenSecret");
    redirect(authState.url);
  }

  public static void callback(String oauth_verifier) throws IOException {
    if (oauth_verifier != null) {
      RdioClient rdio = getRdio();
      RdioClient.Token requestToken = new RdioClient.Token(session.get("requestToken"), session.get("requestTokenSecret"));
      RdioClient.Token accessToken;
      try {
        accessToken = rdio.completeAuthentication(oauth_verifier, requestToken);
      } catch (RdioClient.RdioException e) {
        session.remove("requestToken", "requestTokenSecret");
        redirect(Router.getFullUrl("Application.logout"));
        return;
      }
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