package gamecontrol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import model.IllegalMoveException;
import model.Move;
import model.Piece;
import model.PieceType;
import model.Position;
import model.Side;
import model.SideHasNoMovesException;

import model.chess.CastlingMove;
import model.chess.ChessBoard;
import model.chess.ChessPiece;
import model.chess.ChessUtils;
import model.chess.PawnCaptureMove;
import model.chess.PromotionMove;


/**
 * Controls a game between two local players
 *
 * @author Joe
 */
public class ChessController implements GameController {

    private ChessBoard board;
    private Side currentSide;
    private Map<Piece, Set<Move>> currentMoves;
    private List<BiConsumer<Move, List<Position>>> moveCallbacks;
    private List<Consumer<GameState>> stateCallbacks;
    private List<Consumer<Side>> sideCallbacks;
    private Piece selectedPiece;

    private Supplier<PieceType> promotionCallback;

    private GameState currentState;


    public ChessController() {
        moveCallbacks = new ArrayList<>();
        stateCallbacks = new ArrayList<>();
        sideCallbacks = new ArrayList<>();
        promotionCallback = null;
        setCurrentState(ChessState.ONGOING);
        setBoard(new ChessBoard());
        setCurrentSide(Side.WHITE);
        setCurrentMoves(new HashMap<>());
    }

    protected void setCurrentState(GameState state) {
        currentState = state;
        for (Consumer<GameState> listener : stateCallbacks) {
            listener.accept(currentState);
        }
    }


    @Override
    public GameState getCurrentState() {
        return currentState;
    }

    @Override
    public void startGame() {
        currentState = ChessState.ONGOING;
        beginTurn();
    }

    @Override
    public void beginTurn() {
        boolean whiteInCheck = getCurrentState() == ChessState.WHITE_IN_CHECK;
        boolean blackInCheck = getCurrentState() == ChessState.BLACK_IN_CHECK;
        boolean sideIsWhite = getCurrentSide().equals(Side.WHITE);

        boolean isInCheck = sideIsWhite && whiteInCheck
                || !sideIsWhite && blackInCheck;
        try {
            setCurrentMoves(getBoard()
                    .generateAllMovesForSide(getCurrentSide()));
        } catch (SideHasNoMovesException e) {
            if (isInCheck) {
                setCurrentMoves(null);
                setCurrentState((getCurrentSide() == Side.WHITE)
                        ? ChessState.BLACK_WINS : ChessState.WHITE_WINS);
            } else {
                setCurrentMoves(null);
                setCurrentState(ChessState.STALEMATE);
            }
        }
    }

    @Override
    public void endTurn() {
        setCurrentSide((getCurrentSide().equals(Side.WHITE)) ? Side.BLACK
                : Side.WHITE);
        if (getBoard().isInCheck(getCurrentSide())) {
            setCurrentState((getCurrentSide().equals(Side.BLACK))
                    ? ChessState.BLACK_IN_CHECK : ChessState.WHITE_IN_CHECK);
        } else {
            setCurrentState(ChessState.ONGOING);
        }
    }

    @Override
    public Set<Move> getMovesForPieceAt(Position p) {
        Piece piece = getBoard().getPieceAt(p);
        return (piece == null || piece.getSide() != getCurrentSide())
                ? new HashSet<>() : getCurrentMoves().get(piece);
    }

    @Override
    public void makeMove(Move possibleMove) throws IllegalMoveException {

        Piece mover = getBoard().getPieceAt(possibleMove.getStart());
        selectedPiece = mover;

        Move m = queryForMove(possibleMove);

        if (m == null) {
            throw new IllegalMoveException(possibleMove);
        }

        if (m instanceof PromotionMove
                && possibleMove instanceof PromotionMove
                && ((PromotionMove) possibleMove).getPromotingTo() != null) {
            ((PromotionMove) m).setPromotingTo(
                ((PromotionMove) possibleMove).getPromotingTo());
        }

        if (getCurrentMoves() != null
                && getCurrentMoves().get(mover) != null
                && getCurrentMoves().get(mover).contains(m)) {
            getBoard().movePiece(mover, m);

            List<Position> capturePositions = new ArrayList<>();

            capturePositions.add(m.getDestination());

            // check for castling
            if (m instanceof CastlingMove) {
                CastlingMove cm = (CastlingMove) m;
                CastlingMove.CastlingType castlingType = cm.getCastlingType();
                Position start = new Position(mover.getSide().getBackRow(),
                        castlingType.getStartCol());

                Position dest = new Position(mover.getSide().getBackRow(),
                        castlingType.getDestCol());

                Move moveTwo = new Move(start, dest);
                Piece rook = getBoard().getPieceAt(start);
                getBoard().movePiece(rook, moveTwo);
                for (BiConsumer<Move, List<Position>> callBack
                        : moveCallbacks) {
                    callBack.accept(moveTwo, new ArrayList<>());
                }
            }

            // check for pawn promotion
            if (m instanceof PromotionMove) {
                PromotionMove pm = (PromotionMove) m;
                if (promotionCallback != null
                        && pm.getPromotingTo() == null) {
                    pm.setPromotingTo(promotionCallback.get());

                }
                board.replacePieceAt(m.getDestination(),
                        ChessUtils.getPieceOfType(
                                pm.getPromotingTo(), currentSide));
            }

            // check for en passant
            if (m instanceof PawnCaptureMove
                    && ((PawnCaptureMove) m).isEnPassant()) {
                capturePositions.add(
                        ((PawnCaptureMove) m).getEnPassantCapturePosition());
            }

            for (BiConsumer<Move, List<Position>> callBack : moveCallbacks) {
                callBack.accept(m, capturePositions);
            }
        } else {
            throw new IllegalMoveException(possibleMove);
        }
    }

    @Override
    public Map<Piece, Position> getAllActivePiecesPositions() {
        return getBoard().getAllActivePiecesPositions();
    }

    @Override
    public void addMoveListener(BiConsumer<Move, List<Position>> moveListener) {
        moveCallbacks.add(moveListener);
    }

    @Override
    public void addGameStateChangeListener(Consumer<GameState> listener) {
        stateCallbacks.add(listener);
    }

    @Override
    public void addCurrentSideListener(Consumer<Side> sideListener) {
        sideCallbacks.add(sideListener);
    }

    @Override
    public void setPromotionListener(Supplier<PieceType> supplier) {
        promotionCallback = supplier;
    }

    @Override
    public List<PieceType> getPromotionTypes() {
        List<PieceType> types = new ArrayList<>();
        types.add(ChessPiece.ChessPieceType.QUEEN);
        types.add(ChessPiece.ChessPieceType.BISHOP);
        types.add(ChessPiece.ChessPieceType.KNIGHT);
        types.add(ChessPiece.ChessPieceType.ROOK);

        return types;
    }

    @Override
    public PieceType getDefaultPromotionType() {
        return ChessPiece.ChessPieceType.QUEEN;
    }

    @Override
    public Side getCurrentSide() {
        return currentSide;
    }

    private void setCurrentSide(Side currentSide) {
        this.currentSide = currentSide;
        for (Consumer<Side> sides : sideCallbacks) {
            sides.accept(currentSide);
        }
    }

    @Override
    public String getSymbolForPieceAt(Position pos) {
        Piece p = board.getPieceAt(pos);
        if (p == null) {
            return "";
        }
        return p.getType().getSymbol(p.getSide());
    }

    @Override
    public boolean moveResultsInCapture(Move m) {
        Piece mover = board.getPieceAt(m.getStart());
        Piece target = board.getPieceAt(m.getDestination());

        if (m instanceof PawnCaptureMove
                && ((PawnCaptureMove) m).isEnPassant()) {
            target = board.getPieceAt(
                    ((PawnCaptureMove) m).getEnPassantCapturePosition());
        }

        return target != null && !mover.getSide().equals(target.getSide());
    }

    @Override
    public GameController getNewInstance() {
        ChessController res = new ChessController();
        res.setCurrentState(ChessState.ONGOING);
        return res;
    }

    protected ChessBoard getBoard() {
        return board;
    }

    private void setBoard(ChessBoard board) {
        this.board = board;
    }

    protected Map<Piece, Set<Move>> getCurrentMoves() {
        return currentMoves;
    }

    protected Move queryForMove(Move m) {
        Move found = null;
        for (Move test : currentMoves.get(selectedPiece)) {
            if (m.equals(test)) {
                found = test;
            }
        }
        return found;
    }

    private void setCurrentMoves(Map<Piece, Set<Move>> currentMoves) {
        this.currentMoves = currentMoves;
    }
}
