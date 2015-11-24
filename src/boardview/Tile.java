package boardview;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import model.Position;

/**
 * Tile interface
 * @author Gustavo
 * @date Nov 2, 2015
 */
public interface Tile {

    /**
     * Get the Position object this Tile logically exists at
     * @return This TileView's Position
     */
    Position getPosition();

    /**
     * Get the Node that represents this Tile
     * @return The Node object
     */
    Node getRootNode();

    /**
     * Set the symbol to be displayed on this Tile, should
     * be a Unicode Chess symbol
     * @param symbol
     */
    void setSymbol(String symbol);

    /**
     * Get the symbol currently displayed at this Tile
     * @return
     */
    String getSymbol();

    /**
     * Highlight this tile with a particular color
     * @param color
     */
    void highlight(Color color);

    /**
     * Return this tile to its normal appearance.
     */
    void clear();

}
