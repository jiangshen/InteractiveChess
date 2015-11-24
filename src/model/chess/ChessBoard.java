package model.chess;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import model.Board;
import model.Move;
import model.Piece;
import model.Position;
import model.Side;
import model.SideHasNoMovesException;

/**
 * Stateful ChessBoard
 * @author Joe
 * @date Nov 2, 2015
 */
public class ChessBoard implements Board {

    private Map<Piece, Position> whitePositions;
    private Map<Piece, Position> blackPositions;
    private Map<Side, Piece> kings;
    private Piece[][] board;

    private Map<Side, Boolean> kingsideCastlingRules;
    private Map<Side, Boolean> queensideCastlingRules;

    private Move lastMove;
    private Piece lastMovedPiece;

    //creates a chess board with all pieces in the right place
    public ChessBoard() {
        kingsideCastlingRules = new HashMap<>();
        queensideCastlingRules = new HashMap<>();

        kingsideCastlingRules.put(Side.WHITE, true);
        queensideCastlingRules.put(Side.WHITE, true);
        kingsideCastlingRules.put(Side.BLACK, true);
        queensideCastlingRules.put(Side.BLACK, true);

        whitePositions = new HashMap<>();
        blackPositions = new HashMap<>();
        kings = new HashMap<>();
        board = new ChessPiece[8][8];

        for (Side side : Side.values()) {
            int frontRow = -1;
            int backRow = -1;

            if (side.equals(Side.BLACK)) {
                //blacks in the lower numbers
                frontRow = 1;
                backRow = 0;
            } else {
                frontRow = 6;
                backRow = 7;
            }

            // create pawns
            for (int i = 0; i < 8; i++) {
                placePiece(new Pawn(side), new Position(frontRow, i));
            }

            // create Rooks
            placePiece(new Rook(side), new Position(backRow, 0));
            placePiece(new Rook(side), new Position(backRow, 7));

            // create knights
            placePiece(new Knight(side), new Position(backRow, 1));
            placePiece(new Knight(side), new Position(backRow, 6));

            // create Bishops
            placePiece(new Bishop(side), new Position(backRow, 2));
            placePiece(new Bishop(side), new Position(backRow, 5));

            // create Queen
            placePiece(new Queen(side),
                    new Position(side == Side.BLACK ? 0 : 7, 3));

            // create King
            ChessPiece king = new King(side);
            kings.put(side, king);
            placePiece(king, new Position(side == Side.BLACK ? 0 : 7, 4));
        }
    }

    private ChessBoard(ChessBoard b) {
        board = new ChessPiece[8][8];

        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                board[row][col] = b.board[row][col];
            }
        }

        whitePositions = new HashMap<>(b.whitePositions);
        blackPositions = new HashMap<>(b.blackPositions);
        kings = new HashMap<>(b.kings);

        kingsideCastlingRules = new HashMap<>(b.kingsideCastlingRules);
        queensideCastlingRules = new HashMap<>(b.queensideCastlingRules);
    }

    @Override
    public void movePiece(Piece p, Move m) {

        // check for castling
        validateCastling(p, m);

        Position destPos = m.getDestination();

        Piece destPiece = getPieceAt(destPos.getRow(), destPos.getCol());

        // check for en passant capture
        if (m instanceof PawnCaptureMove
                && ((PawnCaptureMove) m).isEnPassant()) {
            destPiece = getPieceAt(
                    ((PawnCaptureMove) m).getEnPassantCapturePosition());
        }

        //kill a piece
        if (destPiece != null) {
            removePiece(destPiece);
        }

        //move p
        removePiece(p);
        placePiece(p, destPos);

        // keep short history for en passant
        lastMove = m;
        lastMovedPiece = p;
    }

    @Override
    public Map<Piece, Set<Move>> generateAllMovesForSide(Side s)
        throws SideHasNoMovesException {

        boolean sideInCheck = isInCheck(s);
        Map<Piece, Set<Move>> allMoves = new HashMap<>();
        Map<Piece, Position> piecePositions = (s == Side.WHITE)
            ? whitePositions
            : blackPositions;
        boolean sideHasMoves = false;

        for (Piece p : piecePositions.keySet()) {
            Set<Move> moves = p.generateMoves(piecePositions.get(p));
            if (p instanceof King) {
                if (!sideInCheck && queensideCastlingRules.get(s)) {
                    Position start = new Position(s.getBackRow(), 4);
                    Position dest = new Position(s.getBackRow(), 2);
                    moves.add(new CastlingMove(start, dest,
                            CastlingMove.CastlingType.QUEEN_SIDE));
                }
                if (!sideInCheck && kingsideCastlingRules.get(s)) {
                    Position start = new Position(s.getBackRow(), 4);
                    Position dest = new Position(s.getBackRow(), 6);
                    moves.add(new CastlingMove(start, dest,
                            CastlingMove.CastlingType.KING_SIDE));
                }
            }
            Set<Move> filtered = moves
                .stream()
                .filter(m -> pieceCanMove(m, s))
                .collect(Collectors.toCollection(HashSet<Move>::new));

            if (!filtered.isEmpty()) {
                sideHasMoves = true;
            }
            allMoves.put(p, filtered);
        }

        if (!sideHasMoves) {
            throw new SideHasNoMovesException(s.toString() + " has no moves.");
        }

        return allMoves;
    }

    @Override
    public boolean pieceCanMove(Move m, Side movingSide) {
        Position dest = m.getDestination();
        Position start = m.getStart();

        Piece pieceAtDest = getPieceAt(dest);
        Piece mover = getPieceAt(start);

        boolean positionAvailable = pieceAtDest == null;

        boolean isEnPassantCapture = false;

        if (lastMovedPiece != null && lastMove != null) {
            isEnPassantCapture = mover instanceof Pawn
                    && lastMovedPiece instanceof Pawn
                    && m instanceof PawnCaptureMove
                    && Math.abs(lastMove.getStart().getRow()
                    - lastMove.getDestination().getRow()) == 2
                    && m.getDestination().getCol()
                    == lastMove.getDestination().getCol()
                    && lastMove.getDestination().getRow()
                    == m.getStart().getRow();
        }

        if (isEnPassantCapture) {
            PawnCaptureMove pcm = (PawnCaptureMove) m;
            pcm.setIsEnPassant(true);
            pcm.setEnPassantCapturePosition(lastMove.getDestination());
        }

        boolean canCapture = (!positionAvailable
                && !pieceAtDest.getSide().equals(movingSide))
                || isEnPassantCapture;

        if (!positionAvailable && !canCapture) {
            return false;
        }

        if (!canCapture && m.mustCapture()) {
            return false;
        }

        if (canCapture && m.cannotCapture()) {
            return false;
        }

        if (!m.isJump()) {
            //walk towards destination and check for pieces
            //along the way
            int xi = start.getCol(), xf = dest.getCol();
            int yi = start.getRow(), yf = dest.getRow();

            int dx = 0, dy = 0;
            if (Math.abs(xf - xi) != 0) {
                dx = (xf - xi) / Math.abs(xf - xi);
            }
            if (Math.abs(yf - yi) != 0) {
                dy = (yf - yi) / Math.abs(yf - yi);
            }

            for (int x = xi + dx, y = yi + dy;
                    x != xf || y != yf;
                    x += dx, y += dy) {
                if (getPieceAt(y, x) != null) {
                    return false;
                }
            }
        }

        if (positionAvailable || canCapture) {
            ChessBoard preview = new ChessBoard(this);
            Position destPos = m.getDestination();

            Piece destPiece = getPieceAt(destPos.getRow(), destPos.getCol());

            //kill a piece
            if (destPiece != null) {
                preview.removePiece(destPiece);
            }
            //move p
            preview.removePiece(mover);
            preview.placePiece(mover, destPos);

            boolean moveCausesCheck = preview.isInCheck(movingSide);
            return !moveCausesCheck;
        } else {
            return false;
        }
    }

    @Override
    public Map<Piece, Position> getAllActivePiecesPositions() {
        Map<Piece, Position> all = new HashMap<>();
        all.putAll(whitePositions);
        all.putAll(blackPositions);
        return all;
    }

    public boolean isInCheck(Side s) {
        Piece k = kings.get(s);
        Map<Piece, Position> piecePositions;

        if (s.equals(Side.BLACK)) {
            piecePositions = blackPositions;
        } else {
            piecePositions = whitePositions;
        }

        Position kingPosition = piecePositions.get(k);

        //throws NPE when king is killed
        int row = kingPosition.getRow();
        int col = kingPosition.getCol();

        // check for attacking knights because they're weird and special
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                if (dx != dy && dx != 0 && dy != 0 && dx + dy != 0) {
                    if (ChessUtils.posBoundsTest(row + dy, col + dx)) {
                        Piece p = getPieceAt(row + dy, col + dx);
                        if (p != null
                                && !p.getSide().equals(s)
                                && p.getType().equals(
                                    ChessPiece.ChessPieceType.KNIGHT)) {
                            return true;
                        }
                    }
                }
            }
        }


        // check for all the other linear pieces
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int attackCol = col + dx;
                int attackRow = row + dy;
                boolean kingDefended = false;
                while (!kingDefended
                        && (dx != 0 || dy != 0)
                        && ChessUtils.posBoundsTest(attackRow, attackCol)) {
                    Piece attacker = getPieceAt(attackRow, attackCol);
                    if (attacker != null && !attacker.getSide().equals(s)) {
                        Position p = new Position(attackRow, attackCol);
                        if (attacker.generateMoves(p).stream()
                                .filter(m -> !m.cannotCapture())
                                .map(m -> m.getDestination())
                                .collect(Collectors
                                    .toCollection(HashSet<Position>::new))
                                .contains(kingPosition)) {
                            return true;
                        } else {
                            kingDefended = true;
                        }
                    } else if (attacker != null
                            && attacker.getSide().equals(s)) {
                        kingDefended = true;
                    }
                    attackCol = attackCol + dx;
                    attackRow = attackRow + dy;
                }
            }
        }

        return false;
    }

    @Override
    public Piece getPieceAt(Position p) {
        return board[p.getRow()][p.getCol()];
    }

    @Override
    public void replacePieceAt(Position pos, Piece newPiece) {
        Piece old = getPieceAt(pos);
        removePiece(old);
        placePiece(newPiece, pos);
    }

    private Piece getPieceAt(int row, int col) {
        return board[row][col];
    }

    private void placePiece(Piece p, Position pos) {
        if (p.getSide().equals(Side.BLACK)) {
            blackPositions.put(p, pos);
        } else {
            whitePositions.put(p, pos);
        }
        board[pos.getRow()][pos.getCol()] = p;
    }

    private void removePiece(Piece p) {
        Position pos;
        if (p.getSide().equals(Side.BLACK)) {
            pos = blackPositions.get(p);
            blackPositions.remove(p);
        } else {
            pos = whitePositions.get(p);
            whitePositions.remove(p);
        }
        board[pos.getRow()][pos.getCol()] = null;
    }

    private void validateCastling(Piece p, Move m) {
        if (p instanceof King && m.getStart().getCol() == 4
                && (m.getStart().getRow() == p.getSide().getBackRow())) {
            kingsideCastlingRules.put(p.getSide(), false);
            queensideCastlingRules.put(p.getSide(), false);
        } else if (p instanceof Rook && m.getStart().getCol() == 0
                && m.getStart().getRow() == p.getSide().getBackRow()) {
            queensideCastlingRules.put(p.getSide(), false);
        } else if (p instanceof Rook && m.getStart().getCol() == 7
                && m.getStart().getRow() == p.getSide().getBackRow()) {
            kingsideCastlingRules.put(p.getSide(), false);
        }
    }
}
