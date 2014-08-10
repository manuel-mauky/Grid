package eu.lestard.grid;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


/**
 * This class is the UI part of the grid and defines how the grid is shown in the view.
 *
 * To use this class you need to first create a {@link eu.lestard.grid.GridModel} and add it to this view class with the {@link #setGridModel} method.
 *
 * After that you can define what the grid will look like when a cell gets a specific State.
 *
 * @param <State> the generic enum type that defines the states that the grid view can show.
 */
public class GridView<State extends Enum> extends StackPane {

    private Pane rootPane = new Pane();

    private Map<State, Color> colorMapping = new HashMap<>();

    private Map<State, Function<Cell<State>, Node>> nodeMapping = new HashMap<>();

    private ObjectProperty<GridModel<State>> gridModelProperty = new SimpleObjectProperty<>();

    private Map<Cell<State>, Pane> rectangleMap = new HashMap<>();

    private DoubleProperty cellSquareSize = new SimpleDoubleProperty();


    private ObjectProperty<Paint> cellBorderColorProperty = new SimpleObjectProperty<>(Color.LIGHTGREY);
    private DoubleProperty cellBorderWidthProperty = new SimpleDoubleProperty(1);
    private ObjectProperty<Border> cellBorderProperty = new SimpleObjectProperty<>();


    private DoubleProperty gridBorderWidthProperty = new SimpleDoubleProperty(0);
    private ObjectProperty<Paint> gridBorderColorProperty = new SimpleObjectProperty<>(Color.TRANSPARENT);


    private Rectangle gridBackground = new Rectangle();

    /**
     * Create a new instance of the GridView.
     */
    public GridView() {
        this.getChildren().add(gridBackground);
        this.getChildren().add(rootPane);

        gridModelProperty.addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                initGridModel();
            }
        });

        updateCellBorder();
        cellBorderWidthProperty.addListener((obs, oldValue, newValue)-> updateCellBorder());
        cellBorderColorProperty.addListener((obs, oldValue, newValue)-> updateCellBorder());
    }

    /**
     * This method is called when the gridModel is added to the view. It is used
     * to define the bindings for the size of the grid cells.
     *
     */
    private void initGridModel() {
        NumberBinding widthPerCell = this.widthProperty()
            .subtract(gridBorderWidthProperty.multiply(2))
            .divide(getGridModel().numberOfColumns());
        NumberBinding heightPerCell = this.heightProperty()
            .subtract(gridBorderWidthProperty.multiply(2))
            .divide(getGridModel().numberOfRows());

        NumberBinding pxPerCell = Bindings.min(widthPerCell, heightPerCell);

        cellSquareSize.bind(pxPerCell);

        NumberBinding rootWidth = pxPerCell.multiply(getGridModel().numberOfColumns());
        NumberBinding rootHeight = pxPerCell.multiply(getGridModel().numberOfRows());

        rootPane.maxWidthProperty().bind(rootWidth);
        rootPane.maxHeightProperty().bind(rootHeight);


        gridBackground.widthProperty().bind(rootWidth.add(gridBorderWidthProperty.multiply(2)));
        gridBackground.heightProperty().bind(rootHeight.add(gridBorderWidthProperty.multiply(2)));
        gridBackground.fillProperty().bind(gridBorderColorProperty);




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

    /**
     * This method is called when new cells are added in the grid model.
     */
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

        updateCellFill(pane, cell);

        pane.borderProperty().bind(cellBorderProperty);

        cell.stateProperty().addListener((obs, oldValue, newValue) -> updateCellFill(pane, cell));




        initMouseOverFilter(pane, cell);


        // mouse click handler
        pane.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> fireEventHandler(event, cell.onClickProperty()));

        rectangleMap.put(cell, pane);

        rootPane.getChildren().add(pane);
    }

    /**
     * This method initialized the filters that are needed for the mouse over click handler of the cell (see {@link eu.lestard.grid.Cell#setOnMouseOver(javafx.event.EventHandler)}.
     *
     *
     * @param pane
     * @param cell
     */
    private void initMouseOverFilter(Pane pane, Cell<State> cell){
        // When no mouse button is pressed, this event handler is used.
        pane.addEventHandler(MouseEvent.MOUSE_ENTERED, event-> fireEventHandler(event, cell.onMouseOverProperty()));


        // when a mouse button is pressed, we need a special treatment because JavaFX switches to Drag-And-Drop gesture.

        // this is called when the user clicks on a cell and moves the mouse while holding the mouse button
        pane.addEventFilter(MouseEvent.DRAG_DETECTED, event->{
            pane.startFullDrag();
            pane.setMouseTransparent(true);

            fireEventHandler(event, cell.onMouseOverProperty());
        });

        pane.addEventFilter(MouseEvent.MOUSE_RELEASED, event-> pane.setMouseTransparent(false));
        pane.addEventFilter(MouseDragEvent.MOUSE_DRAG_ENTERED, event-> fireEventHandler(event, cell.onMouseOverProperty()));

    }

    private void fireEventHandler(MouseEvent event, ObjectProperty<EventHandler<MouseEvent>> handler){
        if(handler.isNotNull().get()){
            handler.get().handle(event);
        }
    }

    private void updateAllCells() {
        rectangleMap.forEach((cell, pane) -> updateCellFill(pane, cell));
    }


    public Pane getCellPane(Cell<State> cell) {
        return rectangleMap.get(cell);
    }

    private void updateCellFill(Pane pane, Cell<State> cell) {
        pane.setBackground(new Background(new BackgroundFill(colorMapping.get(cell.getState()), CornerRadii.EMPTY,
            Insets.EMPTY)));
        pane.getChildren().clear();
        final Function<Cell<State>, Node> nodeSupplier = nodeMapping.get(cell.getState());
        if (nodeSupplier != null) {
            pane.getChildren().add(nodeSupplier.apply(cell));
        }
    }

    private void updateCellBorder() {
        BorderWidths widths = new BorderWidths(cellBorderWidthProperty.get());
        BorderStroke stroke = new BorderStroke(cellBorderColorProperty.get(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY,
            widths);
        cellBorderProperty.set(new Border(stroke));
    }

    public void setGridModel(GridModel<State> gridModel) {
        gridModelProperty.set(gridModel);
    }

    public GridModel<State> getGridModel() {
        return gridModelProperty.get();
    }

    public void addColorMapping(State state, Color color) {
        this.colorMapping.put(state, color);
        updateAllCells();
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
     *
     *      // with java 8 lambdas:
     *      gridModel.addNodeMapping(States.A, (cell){@code ->} {
     *          return new Label("A");
     *      });
     *
     *      // with anonymous inner class:
     *      gridModel.addNodeMapping(States.B, new Function{@code<Cell<States>, Node>}() {
     *          {literal @}Override
     *          public Node apply(Cell{@code<State>} stateCell) {
     *              return new Label("B");
     *          }
     *      });
     *
     * </pre>
     *
     * @param state           the state for that the mapping function is used.
     * @param mappingFunction the mapping function.
     */
    public void addNodeMapping(State state, Function<Cell<State>, Node> mappingFunction) {
        this.nodeMapping.put(state, mappingFunction);
        updateAllCells();
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
     * The width of the borders around each cell in the grid.
     *
     * Default value is: 1.
     *
     * @return the width as double property.
     */
    public DoubleProperty cellBorderWidthProperty() {
        return cellBorderWidthProperty;
    }

    /**
     * The color of the borders around each cell in the grid.
     *
     * Default value is: {@link Color#LIGHTGREY}.
     *
     * @return the border color as object property.
     */
    public ObjectProperty<Paint> cellBorderColorProperty() {
        return cellBorderColorProperty;
    }


    /**
     * The width of the border around the whole grid.
     *
     * Default value is: 0.
     *
     * @return the width as double property.
     */
    public DoubleProperty gridBorderWidthProperty(){
        return gridBorderWidthProperty;
    }

    /**
     * The color of the border around the whole grid.
     *
     * Default value is: {@link Color#TRANSPARENT}.
     *
     * @return the border color as object property.
     */
    public ObjectProperty<Paint> gridBorderColorProperty(){
        return gridBorderColorProperty;
    }

    Pane getRootPane() {
        return rootPane;
    }


}
