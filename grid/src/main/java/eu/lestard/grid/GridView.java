package eu.lestard.grid;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class GridView<State extends Enum> extends StackPane {


    private Pane rootPane = new Pane();

    private Map<State, Color> colorMapping = new HashMap<>();

    private Map<State, Function<Cell<State>, Node>> nodeMapping = new HashMap<>();

    private ObjectProperty<GridModel<State>> gridModelProperty = new SimpleObjectProperty<>();

    private Map<Cell<State>, Pane> rectangleMap = new HashMap<>();

    private ObjectProperty<Paint> strokeProperty = new SimpleObjectProperty<>(Color.LIGHTGREY);

    private DoubleProperty strokeWidthProperty = new SimpleDoubleProperty(1);

    private DoubleProperty cellSquareSize = new SimpleDoubleProperty();

    public GridView() {
        this.getChildren().add(rootPane);

        gridModelProperty.addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                initGridModel();
            }
        });
    }

    private void initGridModel() {
        NumberBinding widthPerCell = this.widthProperty().divide(getGridModel().numberOfColumns());
        NumberBinding heightPerCell = this.heightProperty().divide(getGridModel().numberOfRows());

        NumberBinding pxPerCell = Bindings.min(widthPerCell, heightPerCell);

        cellSquareSize.bind(pxPerCell);

        NumberBinding rootWidth = pxPerCell.multiply(getGridModel().numberOfColumns());
        NumberBinding rootHeight = pxPerCell.multiply(getGridModel().numberOfRows());

        rootPane.maxWidthProperty().bind(rootWidth);
        rootPane.maxHeightProperty().bind(rootHeight);

        gridModelProperty.get().getCells().forEach(cell -> {
            addedCell(pxPerCell, cell);
        });

        gridModelProperty.get().cells().addListener((ListChangeListener<Cell<State>>) change -> {
            while (change.next()) {

                if (change.wasAdded()) {
                    change.getAddedSubList().forEach(cell -> {
                        addedCell(pxPerCell, cell);
                    });
                }

                if (change.wasRemoved()) {
                    change.getRemoved().forEach(cell -> {
                        Pane pane = rectangleMap.get(cell);
                        rootPane.getChildren().remove(pane);

                        rectangleMap.remove(cell);
                    });
                }
            }
        });

    }

    private void addedCell(NumberBinding pxPerCell, Cell<State> cell) {
        NumberBinding xStart = pxPerCell.multiply(cell.getColumn());
        NumberBinding yStart = pxPerCell.multiply(cell.getRow());

        Pane pane = new StackPane();
        pane.layoutXProperty().bind(xStart);
        pane.layoutYProperty().bind(yStart);

        pane.minWidthProperty().bind(pxPerCell);
        pane.maxWidthProperty().bind(pxPerCell);

        pane.minHeightProperty().bind(pxPerCell);
        pane.maxHeightProperty().bind(pxPerCell);

        updateCell(pane, cell);

        updateStroke(pane);
        strokeProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                updateStroke(pane);
            }
        });

        strokeWidthProperty().addListener((obs, oldV, newV) -> {
            updateStroke(pane);
        });

        cell.stateProperty().addListener((obs, oldValue, newValue) -> {
            updateCell(pane, cell);
        });

        pane.addEventHandler(MouseEvent.MOUSE_CLICKED, (event) -> {
            final EventHandler<MouseEvent> eventHandler = cell.onClickProperty().get();

            if (eventHandler != null) {
                eventHandler.handle(event);
            }
        });

        rectangleMap.put(cell, pane);

        rootPane.getChildren().add(pane);
    }

    public void refresh() {
        rectangleMap.forEach((cell, pane) -> {
            updateCell(pane, cell);
            updateStroke(pane);
        });
    }

    public Pane getCellPane(Cell<State> cell) {
        return rectangleMap.get(cell);
    }

    private void updateCell(Pane pane, Cell<State> cell) {
        pane.setBackground(new Background(new BackgroundFill(colorMapping.get(cell.getState()), CornerRadii.EMPTY, Insets.EMPTY)));
        pane.getChildren().clear();
        final Function<Cell<State>, Node> nodeSupplier = nodeMapping.get(cell.getState());
        if (nodeSupplier != null) {
            pane.getChildren().add(nodeSupplier.apply(cell));
        }
    }

    private void updateStroke(Pane pane) {
        BorderWidths widths = new BorderWidths(strokeWidthProperty().get());
        BorderStroke stroke = new BorderStroke(strokeProperty().get(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, widths);
        pane.setBorder(new Border(stroke));
    }

    public void setGridModel(GridModel<State> gridModel) {
        gridModelProperty.set(gridModel);
    }

    public GridModel<State> getGridModel() {
        return gridModelProperty.get();
    }

    public void addColorMapping(State state, Color color) {
        this.colorMapping.put(state, color);
    }

    /**
     * This method is used to add mapping functions for specific states.
     *
     * A mapping function gets a cell as param and has to return a {@link Node} instance.
     *
     * The function is called every time a cell gets the defined state. The returned node is added
     * to the cell in the grid.
     *
     * This way you can add nodes to the grid when cells are changing there state.
     *
     * For example you can add a Label to the grid for every state.
     * In the example we assume a States enum with the
     * enum constants 'A' and 'B':
     *
     *
     * <pre>
     * <code>
     *
     *      // with java 8 lambdas:
     *      gridModel.addNodeMapping(States.A, (cell) -> {
     *          return new Label("A");
     *      });
     *
     *      // with anonymous inner class:
     *      gridModel.addNodeMapping(States.B, new Function{@code <Cell<States>, Node>}() {
     *          {@literal @}Override
     *          public Node apply(Cell{@code <State>} stateCell) {
     *              return new Label("B");
     *          }
     *      });
     * </code>
     * </pre>
     *
     * @param state           the state for that the mapping function is used.
     * @param mappingFunction the mapping function.
     */
    public void addNodeMapping(State state, Function<Cell<State>, Node> mappingFunction) {
        this.nodeMapping.put(state, mappingFunction);
    }


    public void lala() {

        addNodeMapping(null, new Function<Cell<State>, Node>() {
            @Override
            public Node apply(Cell<State> stateCell) {
                return new Label("B");
            }
        });

    }

    /**
     * The size of a single cell in the grid.
     *
     * @return the size as read-only property.
     */
    public ReadOnlyDoubleProperty cellSizeProperty() {
        return cellSquareSize;
    }

    /**
     * The width of the root pane of the grid.
     *
     * @return the width as read-only property.
     */
    public ReadOnlyDoubleProperty rootWidthProperty() {
        return rootPane.widthProperty();
    }

    /**
     * The height of the root pane of the grid.
     *
     * @return the height as read-only property.
     */
    public ReadOnlyDoubleProperty rootHeightProperty() {
        return rootPane.heightProperty();
    }

    /**
     * The layoutX property of the root pane of the grid.
     *
     * @return the layoutX as read-only property.
     */
    public ReadOnlyDoubleProperty rootLayoutXProperty() {
        return rootPane.layoutXProperty();
    }

    /**
     * The layoutY property of the root pane of the grid.
     *
     * @return the layoutY as read-only property.
     */
    public ReadOnlyDoubleProperty rootLayoutYProperty() {
        return rootPane.layoutYProperty();
    }

    /**
     * The width of the stroke of the grid cells.
     *
     * @return the width as double property.
     */
    public DoubleProperty strokeWidthProperty() {
        return strokeWidthProperty;
    }

    /**
     * The paint that is used as Stroke for the grid cells.
     *
     * @return the stroke paint as object property.
     */
    public ObjectProperty<Paint> strokeProperty() {
        return strokeProperty;
    }

    Pane getRootPane() {
        return rootPane;
    }


}
