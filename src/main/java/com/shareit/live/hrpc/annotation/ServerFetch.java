package com.shareit.live.hrpc.annotation;

public interface ServerFetch {
    /**
     * this interface used for dynamic config server address,
     * you can implement ServerFetch and override fetch
     * to implements your custom loadBalance policy;
     *
     * @param name the server name
     * @return the server address
     * @required the class who implement ServerFetch must be registered as a spring bean;
     */
    String fetch(String name);
}
