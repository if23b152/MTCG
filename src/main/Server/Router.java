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

        return new Response(404, "Not Found");
    }
    public void printRoutes() {
        for (String key : routes.keySet()) {
            System.out.println(key);
        }
    }
}

