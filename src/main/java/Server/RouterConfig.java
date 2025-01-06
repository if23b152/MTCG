package Server;

import Controller.TradingController;
import Controller.UserController;
import Controller.CardController;
import Controller.BattleController;
import Models.User;
import java.util.Map;
import java.util.HashMap;
import Controller.StatsController;


public class RouterConfig {
    private final Router router;
    private final UserController userController;
    private final CardController cardController;
    private final BattleController battleController;
    private final Map<String, User> users = new HashMap<>();
    private final TradingController tradingController;
    private final StatsController statsController;

    public RouterConfig(Router router) {
        this.router = router;
        this.userController = new UserController();
        this.cardController = new CardController();
        this.battleController = new BattleController(users);
        this.tradingController = new TradingController(cardController);
        this.statsController = new StatsController(users);
        setupRoutes(); // Only one call
    }


    private void setupRoutes() {
        // User Management Routes
        router.addRoute("POST", "/users", request -> userController.createUser(request));
        router.addRoute("POST", "/sessions", request -> userController.login(request));
        router.addRoute("POST", "/battles", request -> battleController.startBattle(request));
        router.addRoute("GET", "/users/{username}", request -> {
            String username = extractPathParameter(request.getPath(), "username");
            return userController.getUserData(request, username);
        });
        router.addRoute("PUT", "/users/{username}", request -> {
            String body = request.getBody();

            String username = extractPathParameter(request.getPath(), "username");
            return userController.updateUser(request, username);
        });


        // Card Management Routes
        router.addRoute("POST", "/packages", request -> cardController.createPackage(request));
        router.addRoute("POST", "/transactions/packages", request -> cardController.acquirePackage(request));
        router.addRoute("GET", "/cards", request -> cardController.getCards(request));
        router.addRoute("GET", "/deck", request -> cardController.getDeck(request));
        router.addRoute("PUT", "/deck", request -> cardController.configureDeck(request));


        //Trading Routes
        router.addRoute("GET", "/tradings", request -> tradingController.getTradingDeals(request));
        router.addRoute("POST", "/tradings", request -> tradingController.createTradingDeal(request));
        router.addRoute("DELETE", "/tradings/{tradingdealid}", request -> {
            String dealId = extractPathParameter(request.getPath(), "tradingdealid");
            return tradingController.deleteTradingDeal(request, dealId);
        });
        router.addRoute("POST", "/tradings/{tradingdealid}", request -> {
            String dealId = extractPathParameter(request.getPath(), "tradingdealid");
            return tradingController.trade(request, dealId);
        });
        // Stats and Scoreboard Routes
        router.addRoute("GET", "/stats", request -> statsController.getStats(request));
        router.addRoute("GET", "/scoreboard", request -> statsController.getScoreboard(request));
    }

    private String extractPathParameter(String path, String paramName) {
        String[] parts = path.split("/");
        if (paramName.equals("username") && parts.length > 2) {
            String extracted = parts[2].replaceAll("/", ""); // Entfernt eventuelles abschließendes "/"
//            System.out.println("Extracted username: " + extracted);
            return extracted;
        }
        return "";
    }


}
