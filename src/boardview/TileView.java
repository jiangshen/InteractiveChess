package boardview;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import model.Position;

import javafx.scene.text.Font;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;


/**
 * View class for a tile on a chess board
 * A tile should be able to display a chess piece
 * as well as highlight itself during the game.
 *
 * @author Shen Jiang
 */
public class TileView implements Tile {

    private Position pos;
    private Label pSymbol;
    private StackPane rootNode;
    private Rectangle background;
    private Rectangle highlight;

    /**
     * Creates a TileView with a specified position
     * @param p
     */
    public TileView(Position p) {
        this.pos = p;

        background = new Rectangle(75, 75);
        if ((pos.getRow() + pos.getCol()) % 2 == 0) {
            background.setFill(Color.WHITE);
        } else {
            background.setFill(Color.LIGHTGRAY);
        }

        highlight = new Rectangle(75, 75);
        clear();

        pSymbol = new Label("");
        pSymbol.setFont(new Font(50));
        rootNode = new StackPane();
        rootNode.getChildren().addAll(background, highlight, pSymbol);
    }

    @Override
    public Position getPosition() {
        return this.pos;
    }

    @Override
    public Node getRootNode() {
        return this.rootNode;
    }

    @Override
    public void setSymbol(String symbol) {
        pSymbol.setText(symbol);
    }

    @Override
    public String getSymbol() {
        return pSymbol.getText();
    }

    @Override
    public void highlight(Color color) {
        highlight.setFill(color);
    }

    @Override
    public void clear() {
        highlight.setFill(Color.rgb(255, 255, 255, 0));
    }
}
