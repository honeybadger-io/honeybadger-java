package org.apache.http.client.fluent;

import org.apache.http.HttpResponse;

/**
 *
 */
public class FakeResponse extends Response {
    public FakeResponse(HttpResponse httpResponse) {
        super(httpResponse);
    }
}

