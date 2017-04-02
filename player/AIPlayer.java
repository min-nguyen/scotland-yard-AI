package player;

import graph.*;
import scotlandyard.*;
import swing.algorithms.Dijkstra;

import java.io.IOException;
import java.util.*;

import static scotlandyard.Ticket.fromTransport;

/**
 * Created by minni on 11/03/2016.
 */
public class AIPlayer implements Player {

    private ScotlandYardGraph graph;
    private Dijkstra dijkstra;
    private int mrXLocation = 1;
    private boolean mrX;

    //Issues - mr x's initial location not yet known
    //Because his location isn't updated/visible in the actual game, we have to keep track of it here after we make the AI move

    public AIPlayer(boolean mrX, ScotlandYardView view, String graphFilename) throws IOException {
        ScotlandYardGraphReader reader = new ScotlandYardGraphReader();
        this.graph = reader.readGraph(graphFilename);
        this.dijkstra = new Dijkstra(graphFilename);
        this.mrX = mrX;
    }

    @Override
    public void notify(int location, List<Move> validmoves, Integer token, Receiver receiver) {

        ScotlandYard game = (ScotlandYard) receiver;
        if(mrX) {
            mrXLocation = location;
        }
        else{
            mrXLocation = game.getPlayerLocation(Colour.Black);
        }
        Move move = initializeMinimax(game, game.validMoves(game.getCurrentPlayer()));

        receiver.playMove(move, token);
    }

    public Move initializeMinimax(ScotlandYard game, List<Move> validMoves) {
        double alpha = Double.NEGATIVE_INFINITY;
        double beta = Double.POSITIVE_INFINITY;
        double mrXBestScore = Double.NEGATIVE_INFINITY;
        double detectiveBestScore = Double.POSITIVE_INFINITY;

        Map<Double, Move> moveScores = new HashMap<>();

        for (Move move : validMoves) {
            //Create new clone for every move and simulate a move on it - takes us to second ply
            CustomScotlandYard cloneGame = cloneGame(game);
            cloneGame.playMove(move);
            List<Move> nextValidMoves = cloneGame.validMoves(cloneGame.getCurrentPlayer());
            //Get score of the board state after simulating a move - score of each move in second ply
            double score = minimax(6, cloneGame, alpha, beta, nextValidMoves);
            //Add move and score to hashmap
            moveScores.put(score, move);

            //Note - no need to check if mrXBestScore>beta or detectiveBestScore<alpha, because if mrX, then beta will stay pos infinity. if detective, then alpha will stay neg infinity.
            //Modify top node's alpha/beta if needed. Modify best-score if needed.
            if (mrX) {
                if (score > mrXBestScore) {
                    mrXBestScore = score;
                    if (mrXBestScore > alpha)
                        alpha = mrXBestScore;
                }
            }
            else {
                if (score < detectiveBestScore) {
                    detectiveBestScore = score;
                    if (detectiveBestScore < beta)
                        beta = detectiveBestScore;
                }
            }
        }

        if(mrX)
            return moveScores.get(mrXBestScore);
        else
            return moveScores.get(detectiveBestScore);
    }

    //Returns best score possible - but not the move
    public double minimax(int depth, CustomScotlandYard cloneGame, double alpha, double beta, List<Move> validMoves){

        if(cloneGame.isGameOver()){
            if(cloneGame.getWinningPlayers().contains(Colour.Black))
                return Double.POSITIVE_INFINITY;
            else
                return Double.NEGATIVE_INFINITY;
        }

        if(depth == 0){
            //This will return his actual location
            int mrXLocation = cloneGame.getPlayerLocation(Colour.Black);
            return score(cloneGame, mrXLocation);
        }

        double maxNodeScore = 0;
        double minNodeScore = Double.POSITIVE_INFINITY;
        //Iterate through all valid moves, determine maximum if mrX, determine minimum if detective
        for(Move move : validMoves){
            //Create another clone of our clone - simulate next move in that clone
            CustomScotlandYard cloneGameV2 = cloneGame(cloneGame);

            cloneGameV2.playMove(move);
            Colour currentPlayer = cloneGameV2.getCurrentPlayer();
            List<Move> validMovesV2 = cloneGameV2.validMoves(currentPlayer);

            double score = minimax(depth-1, cloneGameV2, alpha, beta, validMovesV2);

            //If Maximiser
            if(cloneGame.getCurrentPlayer() == Colour.Black) {
                if (score > maxNodeScore) {
                    maxNodeScore = score;
                    if(maxNodeScore > alpha)
                        alpha = maxNodeScore;
                }//If maximiser can choose a move with a higher score than best current option so far for minimiser, then the minimiser won't consider this branch
                if (maxNodeScore > beta)
                    return maxNodeScore;
            }
            //If Minimiser
            else {
                if (score < minNodeScore) {
                    minNodeScore = score;
                    if(minNodeScore < beta)
                        beta = minNodeScore;
                }//If minimiser can choose a move with a lower score than best current option so far for maximiser, then the maximiser won't consider this branch
                if(minNodeScore < alpha)
                    return minNodeScore;
            }
        }

        //Returns best score possible from all of the valid moves at this ply of the game
        if(cloneGame.getCurrentPlayer() == Colour.Black)
            return maxNodeScore;
        else
            return minNodeScore;
    }


    //Create CustomScotlandYard clone of a CustomScotlandYard
    public CustomScotlandYard cloneGame(CustomScotlandYard game){
        //Clone the rounds list
        List<Boolean> customRounds = new ArrayList<>();
        for(Boolean round : game.getRounds()){
            if(round==true)
                customRounds.add(true);
            else
                customRounds.add(false);
        }

        //Initialize new cloneGame
        CustomScotlandYard cloneGame = new CustomScotlandYard(game.getPlayers().size(), customRounds, graph);

        //Add cloned players to clonegame
        List<Colour> players = game.getPlayers();

        for(Colour player : players){
            int location = game.getPlayerLocation(player); //Note - haven't yet found out mr x's actual location
            //When adding Mr X to clonegame, ensure his location is up to date with our AI's knowledge.
            if(player==Colour.Black){
                location = mrXLocation;
            }
            Map<Ticket, Integer> tickets = new HashMap<>();

            int busTickets = game.getPlayerTickets(player, Ticket.Bus);
            int undergroundTickets = game.getPlayerTickets(player, Ticket.Underground);
            int taxiTickets = game.getPlayerTickets(player, Ticket.Taxi);

            if(player==Colour.Black) {
                int doubleTickets = game.getPlayerTickets(player, Ticket.Double);
                int secretTickets = game.getPlayerTickets(player, Ticket.Secret);
                tickets.put(Ticket.Double, doubleTickets);
                tickets.put(Ticket.Secret, secretTickets);
            }

            tickets.put(Ticket.Bus, busTickets);
            tickets.put(Ticket.Underground, undergroundTickets);
            tickets.put(Ticket.Taxi, taxiTickets);

            cloneGame.join(player, location, tickets);
        }

        //Set current round of clonegame
        int round = game.getRound();
        cloneGame.setRound(round);

        //Set current player of clonegame
        Colour currentPlayer = game.getCurrentPlayer();
        cloneGame.setCurrentPlayer(currentPlayer);

        return cloneGame;
    }

    //Create CustomScotlandYard clone of a real ScotlandYard game
    public CustomScotlandYard cloneGame(ScotlandYard game){
        List<Colour> players = game.getPlayers();

        //Clone the rounds list
        List<Boolean> customRounds = new ArrayList<>();
        for(Boolean round : game.getRounds()){
            if(round==true)
                customRounds.add(true);
            else
                customRounds.add(false);
        }

        CustomScotlandYard cloneGame = new CustomScotlandYard(game.getPlayers().size(), customRounds, graph);

        //Add cloned players to clonegame
        for(Colour player : players){
            int location = game.getPlayerLocation(player); //Note - haven't yet found out mr x's actual location
            if(player==Colour.Black){
                location = mrXLocation;
            }
            Map<Ticket, Integer> tickets = new HashMap<>();

            int busTickets = game.getPlayerTickets(player, Ticket.Bus);
            int undergroundTickets = game.getPlayerTickets(player, Ticket.Underground);
            int taxiTickets = game.getPlayerTickets(player, Ticket.Taxi);

            if(player==Colour.Black) {
                int doubleTickets = game.getPlayerTickets(player, Ticket.Double);
                int secretTickets = game.getPlayerTickets(player, Ticket.Secret);
                tickets.put(Ticket.Double, doubleTickets);
                tickets.put(Ticket.Secret, secretTickets);
            }

            tickets.put(Ticket.Bus, busTickets);
            tickets.put(Ticket.Underground, undergroundTickets);
            tickets.put(Ticket.Taxi, taxiTickets);

            cloneGame.join(player, location, tickets);
        }

        //Set current player of clonegame
        Colour currentPlayer = game.getCurrentPlayer();
        cloneGame.setCurrentPlayer(currentPlayer);

        //Set current round of clonegame
        int round = game.getRound();
        cloneGame.setRound(round);

        return cloneGame;
    }


    public double score(CustomScotlandYard game, int mrXLocation){
        double distanceFromDetectives = 0;

        for(CustomPlayerData player: game.players) {
            if(player.getColour()!=Colour.Black) {
                Map<Transport, Integer> transports = new HashMap<>();
                Map<Ticket, Integer> tickets = player.getTickets();

                //Converts TicketHashMap to TransportHashMap for dijkstra's
                for (Ticket ticket : tickets.keySet()) {
                    transports.put(fromTransport(ticket), tickets.get(ticket));
                }

                double shortestRoute = dijkstra.getRoute(player.getLocation(), mrXLocation, transports).size();
                distanceFromDetectives =+ shortestRoute;
            }
        }

        return distanceFromDetectives;
    }

    public Transport fromTransport(Ticket ticket) {
        switch (ticket) {
            case Taxi:
                return Transport.Taxi;
            case Bus:
                return Transport.Bus;
            case Underground:
                return Transport.Underground;
            case Secret:
                return Transport.Boat;
            default:
                return Transport.Taxi;
        }
    }
}







