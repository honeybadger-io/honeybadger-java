package io.honeybadger.reporter.dto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.honeybadger.reporter.servlet.FakeHttpServletRequest;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class ReportedErrorTest {
    Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    @Test
    public void serializesToJson() {
        Exception origin = new RuntimeException("This is the cause");
        Exception e = new RuntimeException("Test exception", origin);
        HttpServletRequest request = new FakeHttpServletRequest();

        ReportedError error = new ReportedError()
                .setError(new ErrorDetails(e))
                .setRequest(new Request(request));
        String value = gson.toJson(error).toString();
        System.out.println(value);
    }
}
