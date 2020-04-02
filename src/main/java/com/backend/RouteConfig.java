package com.backend;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class RouteConfig {

    public Map<String, Set<String>> NO_AUTH_ROUTE = new HashMap<>();

    public RouteConfig(){
        NO_AUTH_ROUTE.put("GET",new HashSet<>());
        NO_AUTH_ROUTE.put("POST",new HashSet<>());
        NO_AUTH_ROUTE.put("PUT",new HashSet<>());
        NO_AUTH_ROUTE.put("DELETE",new HashSet<>());

        Set<String> POST = NO_AUTH_ROUTE.get("POST");
        Set<String> PUT = NO_AUTH_ROUTE.get("PUT");
        Set<String> GET = NO_AUTH_ROUTE.get("GET");
        Set<String> DELETE = NO_AUTH_ROUTE.get("DELETE");

        // Add Route below ... =>

        GET.add("/api/user/image/**");

        POST.add("/api/user/login");
        POST.add("/api/user/upload");

        PUT.add("/api/user/register");
    }

    // URL Base ...
    public static final String API_BASE = "/api";
    public static final String USER_BASE = API_BASE + "/user";
    public static final String MOVIE_BASE = API_BASE + "/movie";
    public static final String REVIEW_BASE = API_BASE + "/review";
    public static final String IMAGE_BASE = API_BASE + "/image";
    public static final String CATEGORY_BASE = API_BASE + "/category";
}
