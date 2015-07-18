package io.honeybadger.reporter.dto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;

public class ReportedErrorTest {
    Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    @Test
    public void serializesToJson() {
        Exception origin = new RuntimeException("This is the cause");
        Exception e = new RuntimeException("Test exception", origin);
        ReportedError error = new ReportedError()
                .setError(new ErrorDetails(e));
        String value = gson.toJson(error).toString();
        System.out.println(value);
    }
}
