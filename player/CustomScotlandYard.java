package player;

import java.io.IOException;
import java.util.*;

import graph.*;
import scotlandyard.*;

import java.util.*;
/**
 * A class to perform all of the game logic.
 */

public class CustomScotlandYard implements ScotlandYardView{

    protected CustomPlayerData currentPlayer;
    protected List<CustomPlayerData> players = new ArrayList<>();
    protected ScotlandYardGraph graph;
    protected Integer numberOfPlayers;
    protected List<Boolean> rounds;
    protected Integer mrXLocation = 0; //updated by play()
    protected Integer round = 0; //updated by play()
    protected boolean mrxwinner;


    /**
     * Constructs a new ScotlandYard object. This is used to perform all of the game logic.
     *
     * @param numberOfDetectives the number of detectives in the game.
     * @param rounds             the List of booleans determining at which rounds Mr X is visible.
     * @param graph              the graph used to represent the board.
     */
    public CustomScotlandYard(Integer numberOfDetectives, List<Boolean> rounds, ScotlandYardGraph graph) {
        this.numberOfPlayers = numberOfDetectives;
        this.rounds = rounds;
        this.graph = graph;
    }

    /**
     * Plays a move sent from a player.
     *
     * @param move  the move chosen by the player.
     *
     **/
    public void playMove(Move move) {
            play(move);
            nextPlayer();
    }

    /**
     * Passes priority onto the next player whose turn it is to play.
     */
    protected void nextPlayer() {
        int i = 0;

        for (CustomPlayerData x : players) {
            if (currentPlayer == x) {
                i++;
                continue;
            }
            if (i == 1) {
                currentPlayer = x;
                return;
            }
        }
        currentPlayer = players.get(0);
    }

    /**
     * Allows the game to play a given move.
     *
     * @param move the move that is to be played.
     */
    protected void play(Move move) {
        if (move instanceof MoveTicket)
            play((MoveTicket) move);
        else if (move instanceof MoveDouble) {
            play((MoveDouble) move);
        } else if (move instanceof MovePass)
            play((MovePass) move);
    }

    /**
     * Plays a MoveTicket.
     *
     * @param move the MoveTicket to play.
     */
    protected void play(MoveTicket move) {
        Ticket ticket = move.ticket;

        //Set new location & decrement ticket
        currentPlayer.setLocation(move.target);
        currentPlayer.removeTicket(ticket);
        //Add used tickets to Mr X's tickets, if current player isn't Mr X
        if (move.colour != Colour.Black) {
            for (CustomPlayerData player : players) {
                if (player.getColour() == Colour.Black) {
                    player.addTicket(ticket);
                }
            }
        }
        //Increment round at end of move, if current player is Mr X
        if (currentPlayer.getColour() == Colour.Black) {
            round++;
            //ALWAYS UPDATE MRXLOCATION
            mrXLocation = currentPlayer.getLocation();
        }
    }

    /**
     * Plays a MoveDouble.
     *
     * @param move the MoveDouble to play.
    */
    protected void play(MoveDouble move) {
        MoveTicket moveA = move.move1;
        play(moveA);
        MoveTicket moveB = move.move2;
        play(moveB);
        //decrement double tickets
        currentPlayer.removeTicket(Ticket.Double);
    }

    /**
     * Plays a MovePass.
     *
     * @param move the MovePass to play.
     */
    protected void play(MovePass move) {

    }

    /**
     * Returns the list of valid moves for a given player.
     *
     * @param player the player whose moves we want to see.
     * @return the list of valid moves for a given player.
     */
    public List<Move> validMoves(Colour player) {
        List<Move> validMoves = new ArrayList<>();


        for (CustomPlayerData x : players) {
            if (x.getColour() == player) {

                Map<Ticket, Integer> tickets = x.getTickets();
                int numberOfSecretTickets = tickets.get(Ticket.Secret);
                int location = x.getLocation();

                Node<Integer> node = graph.getNode(location);
                List<Edge<Integer, Transport>> edgelistA = new ArrayList<>();
                List<Edge<Integer, Transport>> edgelistB = new ArrayList<>();
                edgelistA = graph.getEdgesFrom(node);

                for (Edge<Integer, Transport> edgeA : edgelistA) {

                    Node<Integer> destnodeA = edgeA.other(node);
                    int destA = destnodeA.getIndex();
                    Transport transportA = edgeA.getData();
                    Ticket ticketA = Ticket.fromTransport(transportA);
                    int numberofticketsA = tickets.get(ticketA);

                    MoveTicket moveA = MoveTicket.instance(player, ticketA, destA);
                    Move moveSecret = MoveTicket.instance(player, Ticket.Secret, destA);

                    boolean checkoccupied = false;

                    //check if node is occupied by detective
                    for (CustomPlayerData y : players) {
                        if (y.getColour() != Colour.Black && y.getLocation() == destA)
                            checkoccupied = true;
                    }

                    if (checkoccupied == false) {
                        if (numberofticketsA > 0)
                            validMoves.add(moveA);
                        if (numberOfSecretTickets > 0)
                            validMoves.add(moveSecret);
                    }

                    //adding double moves
                    if (player == Colour.Black && tickets.get(Ticket.Double) > 0) {
                        edgelistB = graph.getEdgesFrom(destnodeA);

                        for (Edge<Integer, Transport> edgeB : edgelistB) {
//        					System.out.println("Edge is " + edgeB);

                            Node<Integer> destnodeB = edgeB.other(destnodeA);
                            int destB = destnodeB.getIndex();
                            Transport transportB = edgeB.getData();
                            Ticket ticketB = Ticket.fromTransport(transportB);
                            int numberofticketsB = tickets.get(ticketB);

                            MoveTicket moveB = MoveTicket.instance(player, ticketB, destB);

                            Move doubleMove = MoveDouble.instance(player, moveA, moveB);
                            Move doubleMoveS1 = MoveDouble.instance(player, Ticket.Secret, destA, ticketB, destB);
                            Move doubleMoveS2 = MoveDouble.instance(player, ticketA, destA, Ticket.Secret, destB);
                            Move doubleMoveS3 = MoveDouble.instance(player, Ticket.Secret, destA, Ticket.Secret, destB);

                            boolean checkoccupiedB = false;

                            //check nodes aren't occupied
                            for (CustomPlayerData b : players) {
                                if (b.getColour() != player) {
                                    int playerLocation = b.getLocation();
                                    if (playerLocation == destA || playerLocation == destB)
                                        checkoccupiedB = true;
                                }
                            }

                            if (checkoccupiedB == false) {
                                if (ticketA == ticketB) {
                                    if (numberofticketsA > 1)
                                        validMoves.add(doubleMove);
                                    if (numberOfSecretTickets > 0) {
                                        validMoves.add(doubleMoveS1);
                                        validMoves.add(doubleMoveS2);
                                    }
                                    if (numberOfSecretTickets > 1) {
                                        validMoves.add(doubleMoveS3);
                                    }
                                } else if (numberofticketsA > 0) {
                                    if (numberofticketsA > 0)
                                        validMoves.add(doubleMove);
                                    if (numberOfSecretTickets > 0) {
                                        validMoves.add(doubleMoveS1);
                                        validMoves.add(doubleMoveS2);
                                    }
                                    if (numberOfSecretTickets > 1) {
                                        validMoves.add(doubleMoveS3);
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            }
        }


        //If no moves, insert pass move
        Move passMove = MovePass.instance(player);

        if (validMoves.isEmpty() && player != Colour.Black)
            validMoves.add(passMove);

        return validMoves;
    }

    /**
     * Allows players to join the game with a given starting state. When the
     * last player has joined, the game must ensure that the first player to play is Mr X.
     *

     * @param colour   the colour of the player.
     * @param location the starting location of the player.
     * @param tickets  the starting tickets for that player.
     * @return true if the player has joined successfully.
     */
    public boolean join(Colour colour, int location, Map<Ticket, Integer> tickets) {
        CustomPlayerData player = new CustomPlayerData(colour, location, tickets);
        players.add(player);

        if (players.size() == numberOfPlayers + 1) {
            for (CustomPlayerData x : players) {
                if (x.getColour() == Colour.Black) {
                    currentPlayer = x;
                    //initialise mrXLocation
                    mrXLocation = x.getLocation();
                }
            }
        }
        return true;
    }

    /**
     * A list of the colours of players who are playing the game in the initial order of play.
     * The length of this list should be the number of players that are playing,
     * the first element should be Colour.Black, since Mr X always starts.
     *
     * @return The list of players.
     */
    public List<Colour> getPlayers() {
        List<Colour> currentPlayers = new ArrayList<>();
        currentPlayers.add(Colour.Black);
        int playernumber = numberOfPlayers;
        if (playernumber > 0)
            currentPlayers.add(Colour.Blue);
        if (playernumber > 1)
            currentPlayers.add(Colour.Green);
        if (playernumber > 2)
            currentPlayers.add(Colour.Red);
        if (playernumber > 3)
            currentPlayers.add(Colour.White);
        if (playernumber > 4)
            currentPlayers.add(Colour.Yellow);
        return currentPlayers;
    }

    /**
     * Returns the colours of the winning players. If Mr X it should contain a single
     * colour, else it should send the list of detective colours
     *
     * @return A set containing the colours of the winning players
     */
    public Set<Colour> getWinningPlayers() {
        Set<Colour> winningPlayers = new HashSet<>();
        boolean mrxwins = true;
        int mrXLocation = 0;

        //Get MrX's Location and check if he has valid moves
        for (CustomPlayerData player : players) {
            if (player.getColour() == Colour.Black) {
                mrXLocation = player.getLocation();
                if (currentPlayer == player && validMoves(player.getColour()).isEmpty()) {
                    mrxwins = false;
                }
            }
        }
        //Check if player has landed on MrX
        for (CustomPlayerData player : players) {
            if (player.getColour() != Colour.Black)
                if (player.getLocation() == mrXLocation) {
                    System.out.println("On location");
                    mrxwins = false;
                }
        }
        if (mrxwins == true && isGameOver()) {
            winningPlayers.add(Colour.Black);
        } else if (isGameOver()) {
            System.out.println("game is over");
            for (CustomPlayerData player : players) {
                if (player.getColour() != Colour.Black) {
                    winningPlayers.add(player.getColour());
                }
            }
        }
        return winningPlayers;
    }


    /**
     * The location of a player with a given colour in its last known location.
     *
     * @param colour The colour of the player whose location is requested.
     * @return The location of the player whose location is requested.
     * If Black, then this returns 0 if MrX has never been revealed,
     * otherwise returns the location of MrX in his last known location.
     * MrX is revealed in round n when {@code rounds.get(n)} is true.
     */

    //Returns field mrXLocation
    public int getPlayerLocation(Colour colour) {
        if (colour == Colour.Black) {
            for (CustomPlayerData x : players) {
                if (x.getColour() == Colour.Black) {
                    return mrXLocation;
                }
            }
        } else {
            for (CustomPlayerData x : players) {
                if (x.getColour() == colour)
                    return x.getLocation();
            }
        }
        return 0;
    }

    /**
     * The number of a particular ticket that a player with a specified colour has.
     *
     * @param colour The colour of the player whose tickets are requested.
     * @param ticket The type of tickets that is being requested.
     * @return The number of tickets of the given player.
     */
    public int getPlayerTickets(Colour colour, Ticket ticket) {
        for (CustomPlayerData x : players) {
            if (x.getColour() == colour) {
                int ticketnumber = x.tickets.get(ticket);
                return ticketnumber;
            }
        }
        return -1;
    }

    /**
     * The game is over when MrX has been found or the agents are out of
     * tickets. See the rules for other conditions.
     *
     * @return true when the game is over, false otherwise.
     */
    public boolean isGameOver() {
        //Game over if only one player
        if (players.size() == 1) {
            mrxwinner = true;
            return true;
        }
        //Check Mr X has any moves left
        if (currentPlayer.getColour() == Colour.Black) {
            List<Move> validmoves = validMoves(Colour.Black);
            if (validmoves.isEmpty()) {
                return true;
            }
        }
        //Check players still have valid moves
        int numberOfPasses = 0;
        for (CustomPlayerData player : players) {
            List<Move> validmoves = new ArrayList<>();
            validmoves = validMoves(player.getColour());
            for (Move move : validmoves) {
                if (move instanceof MovePass)
                    numberOfPasses++;
            }
        }
        if (numberOfPasses == players.size() - 1) {
            return true;
        }
        //Check if Detective has landed on Mr X
        int blacklocation = 0;
        for (CustomPlayerData player : players) {
            if (player.getColour() == Colour.Black)
                blacklocation = player.getLocation();
        }
        for (CustomPlayerData player : players) {
            if (player.getColour() != Colour.Black && player.getLocation() == blacklocation) {
                return true;
            }
        }
        //Check players still have tickets
        int i = 0;
        for (CustomPlayerData player : players) {
            if (player.getColour() != Colour.Black) {
                Map<Ticket, Integer> tickets = player.getTickets();
                for (Integer value : tickets.values()) {
                    if (value > 0)
                        i++;
                }
            }
        }
        if (i == 0) {
            mrxwinner = true;
            return true;
        }
        if (round == rounds.size() - 1 && currentPlayer.getColour() == Colour.Black) {
            mrxwinner = true;
            return true;
        }

        return false;
    }

    /**
     * A game is ready when all the required players have joined.
     *
     * @return true when the game is ready to be played, false otherwise.
     */
    public boolean isReady() {
        if (players.size() == (numberOfPlayers + 1))
            return true;
        return false;
    }

    /**
     * The player whose turn it is.
     *
     * @return The colour of the current player.
     */
    public Colour getCurrentPlayer() {
        return currentPlayer.getColour();
    }

    public void setCurrentPlayer(Colour colour) {
        for(CustomPlayerData player : players){
            if (player.getColour() == colour){
                currentPlayer = player;
            }
        }
    }
    /**
     * The round number is determined by the number of moves MrX has played.
     * Initially this value is 0, and is incremented for each move MrX makes.
     * A double move counts as two moves.
     *
     * @return the number of moves MrX has played.
     */
    public int getRound() {
        return round;
    }

    public void setRound(int round){
        this.round = round;
    }

    /**
     * A list whose length-1 is the maximum number of moves that MrX can play in a game.
     * The getRounds().get(n) is true when MrX reveals the target location of move n,
     * and is false otherwise.
     * Thus, if getRounds().get(0) is true, then the starting location of MrX is revealed.
     *
     * @return a list of booleans that indicate the turns where MrX reveals himself.
     */
    public List<Boolean> getRounds() {
        return rounds;
    }
}
