package com.github.mkopylec.charon.core.interceptors;

import com.github.mkopylec.charon.core.HttpRequest;
import com.github.mkopylec.charon.core.HttpResponse;
import com.github.mkopylec.charon.core.RequestForwarder;
import com.github.mkopylec.charon.core.RequestForwarding;

public class RootPathResponseCookieRewriter implements RequestForwardingInterceptor {

    @Override
    public HttpResponse forward(HttpRequest request, RequestForwarding forwarding, RequestForwarder forwarder) {
        HttpResponse response = forwarder.forward(request, forwarding);
        // TODO Rewrite response cookies
        return response;
    }

    @Override
    public int getOrder() {
        return RESPONSE_COOKIE_REWRITER_ORDER;
    }
}