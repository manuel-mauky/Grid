package eu.lestard.grid;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GridModel<State extends Enum> {

    private final ObservableList<Cell<State>> cells = FXCollections.observableArrayList();
    private final IntegerProperty numberOfRows = new SimpleIntegerProperty();
    private final IntegerProperty numberOfColumns = new SimpleIntegerProperty();

    private Optional<Consumer<Cell<State>>> onCellAddedHandler = Optional.empty();

    private State defaultState;


    public GridModel() {
        ChangeListener<Number> sizeChanged = (obs, oldValue, newValue) -> {
            init();
        };

        numberOfColumns.addListener(sizeChanged);
        numberOfRows.addListener(sizeChanged);
    }

    private void init() {
        List<Cell<State>> cellsToRemove = new ArrayList<>();
        cellsToRemove.addAll(cells);

        for (int column = 0; column < numberOfColumns.get(); column++) {
            for (int row = 0; row < numberOfRows.get(); row++) {
                Cell<State> existingCell = getCell(column, row);
                if(existingCell == null){
                    Cell<State> cell = new Cell<>(column, row);
                    if(onCellAddedHandler.isPresent()){
                        onCellAddedHandler.get().accept(cell);
                    }

                    if (defaultState != null) {
                        cell.changeState(defaultState);
                    }
                    cells.add(cell);
                }else{
                    cellsToRemove.remove(existingCell);
                }
            }
        }

        cells.removeAll(cellsToRemove);
    }

    /**
     * This consumer is called every time when a new cell is added to the grid. This happens when the size of
     * the grid grows.
     *
     * @param handler the consumer.
     */
    public void setOnCellAddedHandler(Consumer<Cell<State>> handler){
        onCellAddedHandler = Optional.of(handler);
    }

    public ObservableList<Cell<State>> cells() {
        return cells;
    }

    public Cell<State> getCell(final int column, final int row) {
        return cells.stream()
            .filter(cell ->
                (cell.getColumn() == column && cell.getRow() == row))
            .findFirst()
            .orElse(null);
    }


    public List<Cell<State>> getNeighbours(int column, int row) {
        List<Cell<State>> result = new ArrayList<>();

        addIfNotNull(result, getCell(column + 1, row));
        addIfNotNull(result, getCell(column - 1, row));
        addIfNotNull(result, getCell(column, row + 1));
        addIfNotNull(result, getCell(column, row - 1));

        return result;
    }

    private <T> void addIfNotNull(Collection<T> collection, T element){
        if(element != null){
            collection.add(element);
        }
    }

    public List<Cell<State>> getCellsWithState(State state){
        return cells.stream().filter(c->c.getState() == state).collect(Collectors.toList());
    }

    public List<Cell<State>> getNeighbours(Cell<State> cell){
        return getNeighbours(cell.getColumn(), cell.getRow());
    }

    public List<Cell<State>> getCells(){
        return cells;
    }


    public List<Cell<State>> getCellsOfRow(int rowNumber) {
        return cells.stream().filter(cell -> cell.getRow() == rowNumber).collect(Collectors.toList());
    }

    public List<Cell<State>> getCellsOfColumn(int columnNumber) {
        return cells.stream().filter(cell -> cell.getColumn() == columnNumber).collect(Collectors.toList());
    }

    public void setDefaultState(State defaultState) {
        this.defaultState = defaultState;

        getCellsWithState(null).forEach(cell -> cell.changeState(defaultState));
    }

    public IntegerProperty numberOfColumns(){
        return numberOfColumns;
    }

    public int getNumberOfColumns(){
        return numberOfColumns.get();
    }

    public void setNumberOfColumns(int value){
        numberOfColumns.set(value);
    }

    public IntegerProperty numberOfRows(){
        return numberOfRows;
    }

    public int getNumberOfRows(){
        return numberOfRows.get();
    }

    public void setNumberOfRows(int value){
        numberOfRows.set(value);
    }


}
