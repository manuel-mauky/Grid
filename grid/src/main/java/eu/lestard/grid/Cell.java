package eu.lestard.grid;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

public class Cell <State extends Enum> {

    private final int column;

    private final int row;

    private ObjectProperty<EventHandler<MouseEvent>> onClick = new SimpleObjectProperty<>();

    private ObjectProperty<State> state = new SimpleObjectProperty<>();


    public Cell(final int column, final int row){
        this.column = column;
        this.row = row;
    }

    public ReadOnlyObjectProperty<State> stateProperty(){
        return state;
    }

    public State getState(){
        return state.get();
    }

    public void changeState(State newState){
        state.setValue(newState);
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    public void setOnClick(EventHandler<MouseEvent> eventHandler){
        this.onClick.set(eventHandler);
    }

    public ObjectProperty<EventHandler<MouseEvent>> onClickProperty(){
        return onClick;
    }

    @Override
    public String toString() {
        return "cell[" + column + "," + row + "]";
    }
}
