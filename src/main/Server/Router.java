// src/main/java/server/Router.java
package main.Server;
import java.util.HashMap;
import java.util.Map;


public class Router {
    private Map<String, RequestHandler> routes = new HashMap<>();

    public void addRoute(String path, String method, RequestHandler handler) {
        String key = method + ":" + path;
        routes.put(key, handler);
    }

    public interface RequestHandler {
        Response handle(Request request);
    }

    public Response handleRequest(Request request) {
        String key = request.getMethod() + ":" + request.getPath();
        RequestHandler handler = routes.get(key);

        if (handler != null) {
            return handler.handle(request);
        }
        // Handle dynamic routes
        for (Map.Entry<String, RequestHandler> entry : routes.entrySet()) {
            String routeKey = entry.getKey();
            if (matchesDynamicRoute(routeKey, key)) {
                return entry.getValue().handle(request);
            }
        }

        return new Response(404, "Not Found");
    }
    private boolean matchesDynamicRoute(String routeKey, String requestKey) {
        String[] routeParts = routeKey.split(":");
        String[] requestParts = requestKey.split(":");

        if (routeParts.length == 2 && requestParts.length == 2) {
            String routePath = routeParts[1];
            String requestPath = requestParts[1];

            String[] routeSegments = routePath.split("/");
            String[] requestSegments = requestPath.split("/");

            if (routeSegments.length == requestSegments.length) {
                for (int i = 0; i < routeSegments.length; i++) {
                    if (!routeSegments[i].startsWith("{") && !routeSegments[i].equals(requestSegments[i])) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public void printRoutes() {
        for (String key : routes.keySet()) {
            System.out.println(key);
        }
    }
}

