// src/main/java/server/Router.java
package Server;
import java.util.HashMap;
import java.util.Map;


public class Router {
    private Map<String, RequestHandler> routes = new HashMap<>();

    public void addRoute(String method, String path, RequestHandler handler) {
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

        // Überprüfe, ob sowohl die Methode als auch der Pfad vorhanden sind
        if (routeParts.length == 2 && requestParts.length == 2) {
            String routeMethod = routeParts[0];
            String requestMethod = requestParts[0];
            String routePath = routeParts[1];
            String requestPath = requestParts[1];

            // Methode überprüfen
            if (!routeMethod.equals(requestMethod)) {
                return false; // Unterschiedliche Methoden, keine Übereinstimmung
            }

            // Pfadsegmente überprüfen
            String[] routeSegments = routePath.split("/");
            String[] requestSegments = requestPath.split("/");

            if (routeSegments.length == requestSegments.length) {
                for (int i = 0; i < routeSegments.length; i++) {
                    if (!routeSegments[i].startsWith("{") && !routeSegments[i].equals(requestSegments[i])) {
                        return false; // Unterschiedliche Segmente, keine Übereinstimmung
                    }
                }
                return true; // Methode und Pfad stimmen überein
            }
        }
        return false; // Ungültige Struktur
    }

}

