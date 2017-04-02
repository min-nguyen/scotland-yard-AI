package player;

import scotlandyard.MapQueue;
import scotlandyard.ScotlandYard;
import scotlandyard.ScotlandYardGraph;
import scotlandyard.Token;

import java.util.List;

/**
 * Created by minni on 05/04/2016.
 */
public class mrXLocation extends ScotlandYard {
    public mrXLocation(Integer numberOfDetectives, List<Boolean> rounds, ScotlandYardGraph graph, MapQueue<Integer, Token> queue, Integer gameId) {
        super(numberOfDetectives, rounds, graph, queue, gameId);
    }

    public void getMrXLocation(){

    }
}
