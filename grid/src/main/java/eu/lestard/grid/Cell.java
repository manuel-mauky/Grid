package eu.lestard.grid;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class Cell <State extends Enum> {

    private final int column;

    private final int row;

    private ObjectProperty<State> state = new SimpleObjectProperty<>();


    public Cell(final int column, final int row){
        this.column = column;
        this.row = row;
    }

    public ReadOnlyObjectProperty<State> stateProperty(){
        return state;
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

    @Override
    public String toString() {
        return "cell[" + column + "," + row + "]";
    }
}
