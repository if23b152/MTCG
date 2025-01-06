package Controller;

import Models.MonsterCard;
import Models.TradingDeal;
import Server.Request;
import Server.Response;
import java.util.*;

public class TradingController {
    private final Map<String, TradingDeal> tradingDeals = new HashMap<>();
    private final CardController cardController;

    public TradingController(CardController cardController) {
        this.cardController = cardController;
    }

    public Response getTradingDeals(Request request) {
        if (tradingDeals.isEmpty()) {
            return new Response(204, "No trading deals available");
        }
        return new Response(200, tradingDealsToJson(tradingDeals.values()));
    }

    public Response createTradingDeal(Request request) {
        String token = request.getHeaders().get("Authorization");
        String username = extractUsername(token);

        try {
            TradingDeal deal = parseTradingDeal(request.getBody());
            if (deal == null) {
                return new Response(400, "Invalid trading deal data");
            }

            if (tradingDeals.containsKey(deal.getId().toString())) {
                return new Response(409, "A deal with this ID already exists");
            }

            tradingDeals.put(deal.getId().toString(), deal);
            return new Response(201, "Trading deal successfully created");
        } catch (Exception e) {
            return new Response(400, "Invalid request");
        }
    }

    public Response deleteTradingDeal(Request request, String dealId) {
        String token = request.getHeaders().get("Authorization");
        String username = extractUsername(token);

        TradingDeal deal = tradingDeals.get(dealId);
        if (deal == null) {
            return new Response(404, "Trading deal not found");
        }

        tradingDeals.remove(dealId);
        return new Response(200, "Trading deal successfully deleted");
    }

    public Response trade(Request request, String tradingDealId) {
        String token = request.getHeaders().get("Authorization");
        String username = extractUsername(token);

        TradingDeal deal = tradingDeals.get(tradingDealId);
        if (deal == null) {
            return new Response(404, "Trading deal not found");
        }

        MonsterCard cardToTrade = cardController.findCardById(username, deal.getCardToTrade().toString());
        if (cardToTrade == null) {
            return new Response(403, "Trading with yourself is not allowed");
        }

        String offeredCardId = request.getBody().replaceAll("\"", "");
        MonsterCard offeredCard = cardController.findCardById(username, offeredCardId);

        if (offeredCard == null) {
            return new Response(403, "The offered card is not owned by the user");
        }

        if (!isTradeValid(deal, offeredCard)) {
            return new Response(403, "The offered card does not meet the trading requirements");
        }

        String cardToTradeOwner = username;
        cardController.exchangeCards(cardToTradeOwner, username, deal.getCardToTrade().toString(), offeredCardId);
        tradingDeals.remove(tradingDealId);

        return new Response(200, "Trading deal successfully executed");
    }

    private boolean isTradeValid(TradingDeal deal, MonsterCard offeredCard) {
        return offeredCard.getDamage() >= deal.getMinimumDamage();
    }

    private String extractUsername(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7, token.length() - 9);
        }
        return "";
    }

    private TradingDeal parseTradingDeal(String json) {
        // Implementation des JSON-Parsings
        return null;
    }

    private String tradingDealsToJson(Collection<TradingDeal> deals) {
        StringBuilder json = new StringBuilder("[");
        Iterator<TradingDeal> iterator = deals.iterator();
        while (iterator.hasNext()) {
            TradingDeal deal = iterator.next();
            json.append(String.format(
                    "{\"Id\":\"%s\",\"CardToTrade\":\"%s\",\"Type\":\"%s\",\"MinimumDamage\":%.1f}",
                    deal.getId().toString(),
                    deal.getCardToTrade().toString(),
                    deal.getType(),
                    deal.getMinimumDamage()
            ));
            if (iterator.hasNext()) {
                json.append(",");
            }
        }
        json.append("]");
        return json.toString();
    }
}
