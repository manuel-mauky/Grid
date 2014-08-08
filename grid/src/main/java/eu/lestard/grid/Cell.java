package eu.lestard.grid;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

/**
 * This class represents a single Cell of the grid. It is created and managed by the {@link eu.lestard.grid.GridModel}.
 *
 * @param <State> the generic type of states that the cell can have. The state needs to be an enum.
 */
public class Cell <State extends Enum> {

    private final int column;

    private final int row;

    private ObjectProperty<EventHandler<MouseEvent>> onClick = new SimpleObjectProperty<>();

    private ObjectProperty<EventHandler<MouseEvent>> onMouseOver = new SimpleObjectProperty<>();

    private ObjectProperty<State> state = new SimpleObjectProperty<>();

    /**
     * New cells my not be created by the user. They are created by the {@link eu.lestard.grid.GridModel} only.
     *
     * @param column the column of the cell
     * @param row the row of the cell
     */
    Cell(final int column, final int row){
        this.column = column;
        this.row = row;
    }

    /**
     * @return the state as read only property.
     */
    public ReadOnlyObjectProperty<State> stateProperty(){
        return state;
    }

    /**
     * @return the current state of this cell.
     */
    public State getState(){
        return state.get();
    }

    /**
     * This method is used to change the state of this cell.
     *
     * @param newState the new state.
     */
    public void changeState(State newState){
        state.setValue(newState);
    }

    /**
     * @return the column where this cell is located in the grid.
     */
    public int getColumn() {
        return column;
    }

    /**
     * @return the row where this cell is located in the grid.
     */
    public int getRow() {
        return row;
    }

    /**
     * Set an event handler that is called when this cell is clicked by the user.
     *
     * @param eventHandler the event handler.
     */
    public void setOnClick(EventHandler<MouseEvent> eventHandler){
        this.onClick.set(eventHandler);
    }

    /**
     * @return an Object property containing the onClick handler.
     */
    ObjectProperty<EventHandler<MouseEvent>> onClickProperty(){
        return onClick;
    }


    /**
     * Set an event handler that is called when the mouse is moving over this cell.
     *
     * @param eventHandler the event handler.
     */
    public void setOnMouseOver(EventHandler<MouseEvent> eventHandler){
        this.onMouseOver.set(eventHandler);
    }


    /**
     * @return an Object property containing the onMouseOver handler.
     */
    ObjectProperty<EventHandler<MouseEvent>> onMouseOverProperty(){
        return onMouseOver;
    }

    @Override
    public String toString() {
        return "cell[" + column + "," + row + "]";
    }
}
