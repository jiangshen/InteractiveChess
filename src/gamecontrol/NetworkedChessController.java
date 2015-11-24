package gamecontrol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import model.IllegalMoveException;
import model.Move;
import model.PieceType;
import model.Position;
import model.Side;
import model.chess.ChessUtils;
import model.chess.PawnCaptureMove;
import model.chess.PromotionMove;

/**
 * This ChessController allows you to play chess remotely over a TCP connection.
 * This is implemented over TCP such that each end of the connection
 * manages a different color and moves are exchanged over the network.
 * @author Gustavo
 * @date Oct 28, 2015
 */
public class NetworkedChessController extends ChessController {

    private Socket socket;
    private Side mySide;
    private InetAddress connectTo;
    private BufferedReader in;
    private PrintWriter out;

    /**
     * Constructor for hosting a game,
     */
    public NetworkedChessController() {
        super();
        mySide = Side.WHITE;
    }

    /**
     * Constructor for connecting to a hosted name
     * @param connectTo InetAddress of the other platyer
     */
    public NetworkedChessController(InetAddress connectTo) {
        this();
        this.connectTo = connectTo;
        mySide = Side.BLACK;
    }

    @Override
    /**
     * This method either opens a TCP socket and connects
     * the the address specified in the constructor or listens
     * over an TCP socket for a connection from a remote player.
     */
    public void startGame() {
        setCurrentState(ChessState.ONGOING);
        super.beginTurn();
        if (mySide == Side.WHITE) {
            try (ServerSocket listener = new ServerSocket(1331)) {
                // block while waiting for other player to join
                socket = listener.accept();
                System.out.println(socket);
                in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException io) {
                io.printStackTrace();
            }
        } else {
            try {
                socket = new Socket(
                        connectTo, 1331, InetAddress.getLocalHost(), 1332);
                in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                // second thread to receive host's first move
                Thread recv = new Thread(() -> performRemoteMoves());
                recv.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    /**
     * This method blocks if this controller belongs to the
     * color that is making the move remotely.
     */
    public void beginTurn() {
        super.beginTurn();
        if (getCurrentSide() != mySide) {
            // second thread while receiving opponent's move
            Thread recv = new Thread(() -> performRemoteMoves());
            recv.start();
        }
    }

    private void performRemoteMoves() {
        try {
            List<Move> moves = parseMoves(in.readLine());
            for (Move m : moves) {
                super.makeMove(m);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalMoveException e) {
            e.printStackTrace();
        }
        super.endTurn();
        super.beginTurn();
    }

    @Override
    public void makeMove(Move possibleMove) throws IllegalMoveException {

        //move piece in the model, invokes the call back
        super.makeMove(possibleMove);

        Move m = queryForMove(possibleMove);
        out.println(m.toString());
    }

    private List<Move> parseMoves(String moves) {
        if (moves == null) {
            return new ArrayList<>();
        }
        String[] allMoves = moves.split(";");
        List<Move> moveList = new ArrayList<>();
        for (String move : allMoves) {
            String[] prefix = move.split(":");
            if (prefix[0].equals("PROMO")) {
                moveList.add(parsePromoMove(move));
            } else if (prefix[0].equals("PASS")) {
                moveList.add(parseEnPassantMove(move));
            } else {
                moveList.add(parseRegularMove(move));
            }
        }
        return moveList;
    }

    private Move parseEnPassantMove(String move) {
        String[] pass = move.split(":");

        String[] coords = pass[1].replaceAll("\\(|\\)", "").split(",");

        int row = Integer.parseInt(coords[0].trim());
        int col = Integer.parseInt(coords[1].trim());

        Position passantCapturePos = new Position(row, col);

        Move m = parseRegularMove(pass[2]);

        PawnCaptureMove pcm = new PawnCaptureMove(m.getStart(),
                m.getDestination(), false, false, true);

        pcm.setEnPassantCapturePosition(passantCapturePos);
        pcm.setIsEnPassant(true);

        return pcm;
    }

    private Move parsePromoMove(String move) {
        String[] promo = move.split(":");

        PieceType promotingTo = ChessUtils.getPieceTypeFromString(promo[1]);

        Move m = parseRegularMove(promo[2]);

        return new PromotionMove(m.getStart(), m.getDestination(), promotingTo);
    }

    private Move parseRegularMove(String move) {
        String[] both = move.split("->");
        String[] first = both[0].replaceAll("\\(|\\)", "").split(",");
        Position uno = new Position(Integer.parseInt(
                first[0].trim()), Integer.parseInt(first[1].trim()));
        String[] second = both[1].replaceAll("\\(|\\)", "").split(",");
        Position dos = new Position(Integer.parseInt(
                second[0].trim()), Integer.parseInt(second[1].trim()));
        return new Move(uno, dos);
    }

    /**
     * Closes the socket
     */
    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Answers what side this controller is responsible for
     * @return Side this controller is responsible for
     */
    public Side getLocalSide() {
        return mySide;
    }


}
