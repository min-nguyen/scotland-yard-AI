package player;

import net.PlayerFactory;
import scotlandyard.Colour;
import scotlandyard.Player;
import scotlandyard.ScotlandYardView;
import scotlandyard.Spectator;

import java.io.IOException;
import java.util.List;

/**
 * Created by minni on 13/03/2016.
 */
public class AIPlayerFactory implements PlayerFactory {
    @Override
    public Player getPlayer(Colour player, ScotlandYardView view, String mapFileName) {
        try {
            if(player == Colour.Black) {
                AIPlayer mrx = new AIPlayer(true, view, mapFileName);
                return mrx;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Spectator> getSpectators(ScotlandYardView scotlandYardView) {
        return null;
    }

    @Override
    public void ready() {

    }

    @Override
    public void finish() {

    }
}
