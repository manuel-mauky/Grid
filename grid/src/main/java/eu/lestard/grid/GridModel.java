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

public class GridModel<State> {

    private final ObservableList<Cell<State>> cells = FXCollections.observableArrayList();
    private final IntegerProperty numberOfRows = new SimpleIntegerProperty();
    private final IntegerProperty numberOfColumns = new SimpleIntegerProperty();

    private Optional<Consumer<Cell<State>>> onCellAddedHandler = Optional.empty();

    private State defaultState;


    public GridModel() {
        ChangeListener<Number> sizeChanged = (obs, oldValue, newValue) -> init();

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

    /**
     * Return the cell with the given coordinates.
     * @param column
     * @param row
     * @return
     */
    public Cell<State> getCell(final int column, final int row) {
        return cells.stream()
            .filter(cell ->
                (cell.getColumn() == column && cell.getRow() == row))
            .findFirst()
            .orElse(null);
    }


    /**
     * Get the direct neighbour cells of the cell with the given coordinates.
     *
     * "Direct neighbour" means the 4 cells above, below and on the left and right.
     * Diagonal cells are not included.
     *
     * Only existing neighbour cells are included in the returned list. This means that
     * if the reference cell is located f.e. in the upper left corner of the grid, there is no cell
     * above and on the left of this cell. In this case only two cells are returned: the one below and on the right
     * of the reference cell.
     *
     * If the size of the grid is <code>1</code> there is only one cell available that obviously has no
     * neighbours. An empty collection is returned in this case.
     *
     * @param column the column no. of the reference cell
     * @param row the column no. of the reference cell
     * @return a collection of neighbour cells. This can be up to 4.
     */
    public List<Cell<State>> getNeighbours(int column, int row) {
        List<Cell<State>> result = new ArrayList<>();

        addIfNotNull(result, getCell(column + 1, row));
        addIfNotNull(result, getCell(column - 1, row));
        addIfNotNull(result, getCell(column, row + 1));
        addIfNotNull(result, getCell(column, row - 1));

        return result;
    }

    /**
     * Returns a list of direct neighbour cells of the given cell.
     *
     * See {@link #getNeighbours(int, int)} for a detailed explanation.
     *
     * @param cell the reference cell whose neighbours are returned.
     * @return a collection of neighbour cells.
     */
    public List<Cell<State>> getNeighbours(Cell<State> cell){
        return getNeighbours(cell.getColumn(), cell.getRow());
    }


    private <T> void addIfNotNull(Collection<T> collection, T element){
        if(element != null){
            collection.add(element);
        }
    }

    /**
     * Returns a list of cells that have the given state.
     *
     * @param state the state that should be used as filter
     * @return a collection of cells with the given state.
     */
    public List<Cell<State>> getCellsWithState(State state){
        return cells.stream().filter(c->c.getState() == state).collect(Collectors.toList());
    }


    /**
     * @return a collection of all cells.
     */
    public List<Cell<State>> getCells(){
        return cells;
    }


    /**
     * Returns a list of cells that are located in the row with the given number.
     *
     * @param rowNumber the number of the row that is used to filter the cells.
     * @return a collection of cells of the given row.
     */
    public List<Cell<State>> getCellsOfRow(int rowNumber) {
        return cells.stream().filter(cell -> cell.getRow() == rowNumber).collect(Collectors.toList());
    }

    /**
     * Returns a list of cells that are located in the column with the given number.
     *
     * @param columnNumber the number of the column that is used to filter the cells.
     * @return a collection of cells of the given row.
     */
    public List<Cell<State>> getCellsOfColumn(int columnNumber) {
        return cells.stream().filter(cell -> cell.getColumn() == columnNumber).collect(Collectors.toList());
    }

    /**
     * Specify the state that should be used as default when new cells are generated.
     *
     * All cells that have a state of <code>null</code> at the moment will also get
     * the new state applied.
     *
     * @param defaultState the default state.
     */
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
