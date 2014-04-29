/*
  @Author: Scott Page
  @Company: StyleSeek
  @Brief: Catches any uncaught exceptions and sends them to honeybadger.
*/
package Honeybadger;
import java.util.concurrent.*;

import java.io.*;
import com.google.gson.*;
import com.google.gson.stream.*;

import java.net.*;

import javax.net.ssl.HttpsURLConnection;

class Honeybadger implements Thread.UncaughtExceptionHandler{
  private final String HONEY_BADGER_URL = "https://api.honeybadger.io/v1/notices";
  private String apiKey;
  private String envName;

  public Honeybadger(){
    apiKey = System.getenv("HONEYBADGER_API_KEY"); //set this environmental variable to your api key
    envName = System.getenv("RACK_ENV");            //set this env var to your environment...development or production
    Thread.setDefaultUncaughtExceptionHandler(this);//report all uncaught exceptions to honey badger
  }

  /*
    In your main function add the following 2 lines

    HoneyBadger honeyBadger = new HoneyBadger();
  */
  public void uncaughtException(Thread thread, Throwable error) {
    reportErrorToHoneyBadger(error);
  }

  /*
    You can send a throwable to honeybadger
  */
  public void reportErrorToHoneyBadger(Throwable error){
    Gson myGson = new Gson();
    JsonObject jsonError = new JsonObject();
    jsonError.add("notifier", makeNotifier());
    jsonError.add("error", makeError(error));
    /*
      If you need to add more information to your errors add it here
    */
    jsonError.add("server", makeServer());

    int responseCode = sendToHoneyBadger(myGson.toJson(jsonError));
    if(responseCode!=201)
      System.err.println("ERROR: Honeybadger did not respond with the correct code. Response was = "+responseCode);
    else
      System.err.println("Honeybadger logged error:  "+error);
  }

  /*
    Identify the notifier
  */
  private JsonObject makeNotifier(){
    JsonObject notifier = new JsonObject();
    notifier.addProperty("name", "Honeybadger-java Notifier");
    notifier.addProperty("url", "www.mytastebud.com");
    notifier.addProperty("version", "1.3.0");
    return notifier;
  }

  /*
    Format the throwable into a json object
  */
  private JsonObject makeError(Throwable error){
    JsonObject jsonError = new JsonObject();
    jsonError.addProperty("class", error.toString());

    JsonArray backTrace = new JsonArray();
    for(StackTraceElement trace : error.getStackTrace()){
      JsonObject jsonTraceElement = new JsonObject();
      jsonTraceElement.addProperty("number", trace.getLineNumber());
      jsonTraceElement.addProperty("file", trace.getFileName());
      jsonTraceElement.addProperty("method", trace.getMethodName());
      backTrace.add(jsonTraceElement);
    }
    jsonError.add("backtrace", backTrace);

    return jsonError;
  }

  /*
    Establish the environment
  */
  private JsonObject makeServer(){
    JsonObject jsonServer = new JsonObject();
    jsonServer.addProperty("environment_name", envName);
    return jsonServer;
  }

  /*
    Send the json string error to honeybadger
  */
  private int sendToHoneyBadger(String jsonError){
    URL obj = null;
    HttpsURLConnection con = null;
    DataOutputStream wr = null;
    BufferedReader in = null;
    int responseCode = -1;
    try{
      obj = new URL(HONEY_BADGER_URL);
      con = (HttpsURLConnection) obj.openConnection();
      //add request header
      con.setRequestMethod("POST");
      con.setRequestProperty("X-API-Key", apiKey);
      con.setRequestProperty("Content-Type", "application/json");
      con.setRequestProperty("Accept", "application/json");
   
      // Send post request
      con.setDoOutput(true);
      wr = new DataOutputStream(con.getOutputStream());
      wr.writeBytes(jsonError);
      wr.flush();
   
      responseCode = con.getResponseCode();
   
      in = new BufferedReader(
              new InputStreamReader(con.getInputStream()));
      String inputLine;
      StringBuffer response = new StringBuffer();
   
      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
    }
    catch(MalformedURLException e){
      System.err.println("Bad url "+HONEY_BADGER_URL+" "+e);
    }
    catch(IOException e){
      System.err.println("Bad io "+HONEY_BADGER_URL+" "+e);
    }
    finally{
      try{
        if(in!=null)
          in.close();
        if(wr!=null)
          wr.close();
      }
      catch(Exception e){
        System.err.println("Failure to close honey badger "+e);
      }
    }
    return responseCode;
  }
}