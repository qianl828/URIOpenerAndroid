package com.github.webee.uriopener.core;

import android.net.Uri;
import android.os.Bundle;

import java.util.List;

/**
 * Created by webee on 17/2/15.
 */

public class Request {
    public static final String EXTRA_PATH_PARAMS = Request.class.getName() + ".path_params";
    public static final String EXTRA_QUERY_PARAMS = Request.class.getName() + ".query_params";

    public final Uri uri;
    public List<Param> pathParams;
    public Bundle data = new Bundle();

    public Request(Uri uri, List<Param> pathParams) {
        this.uri = uri;
        this.pathParams = pathParams;
    }

    @Override
    public String toString() {
        return "Request{" +
                "uri=" + uri +
                ", pathParams=" + pathParams +
                ", data=" + data +
                '}';
    }
}
