package com.github.webee.uriopener.handlers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.github.webee.uriopener.core.Data;
import com.github.webee.uriopener.core.Handler;
import com.github.webee.uriopener.core.Param;
import com.github.webee.uriopener.core.Request;
import com.github.webee.uriopener.core.Route;
import com.github.webee.uriopener.core.RouteContext;
import com.github.webee.uriopener.core.Router;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by webee on 17/2/17.
 */

public class ActivityHandler implements Handler {
    public static final String QUERY_PARAM_NAME_ACTIVITY_REQUEST_CODE = "__ACTIVITY_REQUEST_CODE";
    public static final String ARBITRATION_PROXY_PATH = ActivityHandler.class.getName() + ".arbitration_proxy";
    public static final String DATA_OPTIONS = ActivityHandler.class.getName() + ".options";
    public static final String DATA_REQUEST_CODE = ActivityHandler.class.getName() + ".request_code";
    public static final String DATA_FLAGS = ActivityHandler.class.getName() + ".flags";
    public static final String DATA_INTENT_PROCESSOR = ActivityHandler.class.getName() + ".intent_processor";
    private static final Map<Class<? extends Activity>, Handler> handlers = new HashMap<>();
    private final Class<? extends Activity> cls;

    public static void initRoutes(Router router) {
        router.add(ARBITRATION_PROXY_PATH, create(ArbitrationProxyActivity.class));
    }

    public static Handler create(Class<? extends Activity> cls) {
        Handler handler = handlers.get(cls);
        if (handler == null) {
            handler = new ActivityHandler(cls);
            handlers.put(cls, handler);
        }
        return handler;
    }

    public static Route route(Class<? extends Activity> cls, Param ...pathParams) {
        return Route.create(create(cls), pathParams);
    }

    private ActivityHandler(Class<? extends Activity> cls) {
        this.cls = cls;
    }

    public static CtxDataBuilder ctxData() {
        return new CtxDataBuilder();
    }

    public static CtxDataBuilder ctxData(Data data) {
        return new CtxDataBuilder(data);
    }

    @Override
    public void handle(RouteContext ctx) {
        android.content.Context context = ctx.context;
        Intent intent = new Intent(context, cls);

        Bundle options = ctx.data.get(DATA_OPTIONS);

        Request request = ctx.request;
        intent.setData(request.uri);
        intent.putExtras(request.data);

        if (ctx.data.containsKey(DATA_FLAGS)) {
            int flags = ctx.data.get(DATA_FLAGS);
            intent.setFlags(flags);
        }

        IntentProcessor intentProcessor = ctx.data.get(DATA_INTENT_PROCESSOR);
        if (intentProcessor != null) {
            intent = intentProcessor.process(intent);
        }

        if (ctx.data.containsKey(DATA_REQUEST_CODE)) {
            // request for result.
            int requestCode = ctx.data.get(DATA_REQUEST_CODE);

            ((Activity)context).startActivityForResult(intent, requestCode, options);
        } else {
            context.startActivity(intent, options);
        }
    }

    public static boolean isRequestForResult(Data ctxData) {
        return ctxData.containsKey(DATA_REQUEST_CODE);
    }

    public static int tryGetRequestCode(Data ctxData) {
        if (ctxData.containsKey(DATA_REQUEST_CODE)) {
            return ctxData.get(DATA_REQUEST_CODE);
        }
        return -1;
    }

    public static class CtxDataBuilder {
        Data data = null;

        Bundle options;
        Integer requestCode;
        int flags = 0;
        IntentProcessor intentProcessor;

        public CtxDataBuilder() {
            data = new Data();
        }

        public CtxDataBuilder(Data srcData) {
            data = srcData;
            if (data != null) {
                options = data.get(DATA_OPTIONS);
                data.remove(DATA_OPTIONS);

                requestCode = data.get(DATA_REQUEST_CODE);
                data.remove(DATA_REQUEST_CODE);

                if (data.containsKey(DATA_FLAGS)) {
                    flags = data.get(DATA_FLAGS);
                    data.remove(DATA_FLAGS);
                }

                intentProcessor = data.get(DATA_INTENT_PROCESSOR);
                data.remove(DATA_INTENT_PROCESSOR);
            } else {
                data = new Data();
            }
        }

        public CtxDataBuilder withOptions(Bundle data) {
            if (data != null) {
                if (options == null) {
                    options = new Bundle();
                }
                options.putAll(data);
            }
            return this;
        }

        public CtxDataBuilder withRequestCode(int code) {
            requestCode = code;
            return this;
        }

        public CtxDataBuilder withFlags(int ...flags) {
            for (int f : flags) {
                this.flags |= f;
            }
            return this;
        }

        public CtxDataBuilder withoutFlags(int ...flags) {
            for (int f : flags) {
                this.flags &= ~f;
            }
            return this;
        }

        public CtxDataBuilder withIntentProcessor(IntentProcessor processor) {
            intentProcessor = processor;
            return this;
        }

        public Data build() {
            if (options != null) {
                data.bundle.putBundle(DATA_OPTIONS, options);
            }

            if (requestCode != null) {
                data.bundle.putInt(DATA_REQUEST_CODE, requestCode);
            }

            if (flags > 0) {
                data.bundle.putInt(DATA_FLAGS, flags);
            }

            if (intentProcessor != null) {
                data.put(DATA_INTENT_PROCESSOR, intentProcessor);
            }

            return data;
        }
    }

    @Override
    public String toString() {
        return "ActivityHandler{" + cls + '}';
    }
}
