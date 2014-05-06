/*
  @Author: Scott Page
  @Company: StyleSeek
  @Brief: Catches any uncaught exceptions and sends them to honeybadger.
  You must set the following Env Vars
  HONEYBADGER_API_KEY ... Your honeybadger api key found in the settings page
  JAVA_ENV ... probably development or production
*/
package Honeybadger;
import java.util.concurrent.*;
import java.io.*;
import com.google.gson.*;
import com.google.gson.stream.*;
import java.net.*;
import javax.net.ssl.HttpsURLConnection;

public class Honeybadger implements Thread.UncaughtExceptionHandler{
  private final String HONEY_BADGER_URL = "https://api.honeybadger.io/v1/notices";
  private String HONEYBADGER_API_KEY;
  private String envName;

  public Honeybadger(){
    HONEYBADGER_API_KEY = System.getenv("HONEYBADGER_API_KEY"); //set this environmental variable to your api key
    envName = System.getenv("JAVA_ENV");            //set this env var to your environment...development or production
    if(HONEYBADGER_API_KEY==null || envName==null){
      System.out.println("ERROR: You did not set the HONEYBADGER_API_KEY or RACK_ENV environmental variables...closing");
      System.exit(1);
    }
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
    for(int retries = 0; retries < 3; retries++){
      try{
        int responseCode = sendToHoneyBadger(myGson.toJson(jsonError));
        if(responseCode!=201)
          System.err.println("ERROR: Honeybadger did not respond with the correct code. Response was = "+responseCode+" retry="+retries);
        else{
          System.err.println("Honeybadger logged error correctly:  "+error);
          break;
        }
      }catch(IOException e){
        System.out.println("ERROR: Honeybadger got an ioexception when trying to send the error retry="+retries);
      }
    }
  }

  /*
    Identify the notifier
  */
  private JsonObject makeNotifier(){
    JsonObject notifier = new JsonObject();
    notifier.addProperty("name", "Honeybadger-java Notifier");
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
  private int sendToHoneyBadger(String jsonError) throws IOException{
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
      con.setRequestProperty("X-API-Key", HONEYBADGER_API_KEY);
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
      System.err.println("ERROR: Bad url "+HONEY_BADGER_URL+" "+e);
      System.exit(1);
    }
    finally{
      try{
        if(in!=null)
          in.close();
        if(wr!=null)
          wr.close();
      }
      catch(Exception e){
        System.err.println("WARNING: Failure to close honey badger "+e);
      }
    }
    return responseCode;
  }
}