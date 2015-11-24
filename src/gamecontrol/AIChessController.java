package gamecontrol;

import gamecontrol.ai.AIChessEngine;
import gamecontrol.ai.RandomChessEngine;
import model.IllegalMoveException;
import model.Move;
import model.Side;

/**
 * Controls a game between one human and one computer
 *
 * @author Joe
 */
public class AIChessController extends ChessController {

    private Side mySide;
    private AIChessEngine aiChessEngine;

    public AIChessController() {
        this(Side.WHITE);
    }

    public AIChessController(Side s) {
        this(s, new RandomChessEngine());
    }

    public AIChessController(Side s, AIChessEngine ai) {
        mySide = s;
        aiChessEngine = ai;
    }

    @Override
    public void beginTurn() {
        super.beginTurn();
        if (getCurrentSide() != mySide && !getCurrentState().isGameOver()) {
            Move selected = aiChessEngine.chooseNextMove(getCurrentMoves(),
                    getBoard());
            try {
                super.makeMove(selected);
            } catch (IllegalMoveException e) {
                beginTurn();
            }
            super.endTurn();
            super.beginTurn();
        }
    }

    @Override
    public GameController getNewInstance() {
        AIChessController res = new AIChessController();
        res.setCurrentState(ChessState.ONGOING);
        return res;
    }
}
