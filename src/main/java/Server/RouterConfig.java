package Server;

import Controller.TradingController;
import Controller.UserController;
import Controller.CardController;
import Controller.BattleController;
import Database.TradingRepository;
import Models.User;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

import Controller.StatsController;


public class RouterConfig {
    private final Router router;
    private final UserController userController;
    private final CardController cardController;
    private final BattleController battleController;
    private final TradingController tradingController;
    private final StatsController statsController;

    public RouterConfig(Router router) {
        this.router = router;
        this.userController = new UserController();
        this.cardController = new CardController();
        Map<String, User> users = new HashMap<>();
        this.battleController = new BattleController(users);
        TradingRepository tradingRepository = new TradingRepository();
        this.tradingController = new TradingController(cardController, tradingRepository);

        this.statsController = new StatsController(users);
        setupRoutes(); // Only one call
    }


    private void setupRoutes() {
        // User Management Routes
        router.addRoute("POST", "/users", userController::createUser);
        router.addRoute("POST", "/sessions", userController::login);
        router.addRoute("POST", "/battles", battleController::startBattle);
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
        router.addRoute("POST", "/packages", cardController::createPackage);
        router.addRoute("POST", "/transactions/packages", cardController::acquirePackage);
        router.addRoute("GET", "/cards", cardController::getCards);
        router.addRoute("GET", "/deck", cardController::getDeck);
        router.addRoute("PUT", "/deck", cardController::configureDeck);


        //Trading Routes
        router.addRoute("GET", "/tradings", tradingController::getTradingDeals);
        router.addRoute("POST", "/tradings", tradingController::createTradingDeal);


//        router.addRoute("POST", "/tradings/{tradingdealid}", request -> {
//            String dealId = extractPathParameter(request.getPath(), "tradingdealid");
//            String cardId = request.getBody().trim().replace("\"", "");
//            try {
//                UUID cardToTradeId = UUID.fromString(cardId);
//                return tradingController.createTradingDealWithCard(request, dealId, cardToTradeId);
//            } catch (IllegalArgumentException e) {
//                return new Response(400, "Invalid card ID format");
//            }
//
//        });
        router.addRoute("DELETE", "/tradings/{tradingdealid}", request -> {
            String dealId = extractPathParameter(request.getPath(), "tradingdealid");
            return tradingController.deleteTradingDeal(request, dealId);
        });
        router.addRoute("POST", "/tradings/{tradingdealid}", request -> {
            String dealId = extractPathParameter(request.getPath(), "tradingdealid");
            return tradingController.trade(request, dealId);
        });


        // Stats and Scoreboard Routes
        router.addRoute("GET", "/stats", statsController::getStats);
        router.addRoute("GET", "/scoreboard", statsController::getScoreboard);
    }

    private String extractPathParameter(String path, String paramName) {
        String[] parts = path.split("/");

        // valide Route
        if (parts.length < 3) {
            return "";
        }

        // Extrahiere parameter Wert je nach anfrage
        if (paramName.equals("username") && parts.length > 2) {
            return parts[2].replaceAll("/", ""); // /users/{username}
        } else if (paramName.equals("tradingdealid") && parts.length > 2) {
            return parts[2]; // /tradings/{tradingdealid} oder /tradings/{tradingdealid}/trade
        }
        return "";
    }


}