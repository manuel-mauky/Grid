package eu.lestard.grid;

import eu.lestard.advanced_bindings.api.NumberBindings;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
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
public class GridView<State> extends StackPane {

    private Pane rootPane = new Pane();

    private Pane guidelinePane = new Pane();

    private Map<State, Color> colorMapping = new HashMap<>();

    private Map<State, Function<Cell<State>, Node>> nodeMapping = new HashMap<>();

    private ObjectProperty<GridModel<State>> gridModel = new SimpleObjectProperty<>();

    private Map<Cell<State>, Pane> rectangleMap = new HashMap<>();

    private DoubleProperty cellSquareSize = new SimpleDoubleProperty();


    private ObjectProperty<Paint> cellBorderColor = new SimpleObjectProperty<>(Color.LIGHTGREY);
    private DoubleProperty cellBorderWidth = new SimpleDoubleProperty(1);
    private ObjectProperty<Border> cellBorder = new SimpleObjectProperty<>();


    private DoubleProperty gridBorderWidth = new SimpleDoubleProperty(0);
    private ObjectProperty<Paint> gridBorderColor = new SimpleObjectProperty<>(Color.TRANSPARENT);


    private IntegerProperty horizontalGuidelineUnit = new SimpleIntegerProperty(0);
    ObservableList<Integer> horizontalGuidelines = FXCollections.observableArrayList();

    private IntegerProperty verticalGuidelineUnit = new SimpleIntegerProperty(0);
    ObservableList<Integer> verticalGuidelines = FXCollections.observableArrayList();
    private ObjectProperty<Color> guidelineColor = new SimpleObjectProperty<>(Color.TRANSPARENT);
    private DoubleProperty guidelineStrokeWidth = new SimpleDoubleProperty(5);


    private Rectangle gridBackground = new Rectangle();

    // don't inline these bindings to prevent errors with Garbage Collection
    private NumberBinding numberOfHorizontalMajorGuidelines;
    private NumberBinding numberOfVerticalMajorGuidelines;

    /**
     * Create a new instance of the GridView.
     */
    public GridView() {
        this.getChildren().add(gridBackground);
        this.getChildren().add(rootPane);

        guidelinePane.setMouseTransparent(true);
        this.getChildren().add(guidelinePane);

        gridModel.addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                initGridModel();
            }
        });

        updateCellBorder();
        cellBorderWidth.addListener((obs, oldValue, newValue) -> updateCellBorder());
        cellBorderColor.addListener((obs, oldValue, newValue) -> updateCellBorder());
    }

    /**
     * This method is called when the gridModel is added to the view. It is used
     * to define the bindings for the size of the grid cells.
     *
     */
    private void initGridModel() {
        NumberBinding widthPerCell = this.widthProperty()
            .subtract(gridBorderWidth.multiply(2))
            .divide(getGridModel().numberOfColumns());
        NumberBinding heightPerCell = this.heightProperty()
            .subtract(gridBorderWidth.multiply(2))
            .divide(getGridModel().numberOfRows());

        NumberBinding pxPerCell = Bindings.min(widthPerCell, heightPerCell);

        cellSquareSize.bind(pxPerCell);


        NumberBinding rootWidth = pxPerCell.multiply(getGridModel().numberOfColumns());
        NumberBinding rootHeight = pxPerCell.multiply(getGridModel().numberOfRows());

        rootPane.maxWidthProperty().bind(rootWidth);
        rootPane.maxHeightProperty().bind(rootHeight);


        gridBackground.widthProperty().bind(rootWidth.add(gridBorderWidth.multiply(2)));
        gridBackground.heightProperty().bind(rootHeight.add(gridBorderWidth.multiply(2)));
        gridBackground.fillProperty().bind(gridBorderColor);



        guidelinePane.maxWidthProperty().bind(rootPane.widthProperty());
        guidelinePane.maxHeightProperty().bind(rootPane.heightProperty());


        initMajorGuidelinesBindings();

        final InvalidationListener guidelineRepaintListener = (Observable observable) -> {
            guidelinePane.getChildren().clear();


            verticalGuidelines.forEach(row -> {
                Line major = new Line();

                major.startYProperty().bind(guidelineStrokeWidth.divide(2));
                major.startXProperty().bind(cellSquareSize.multiply(row));

                major.endYProperty().bind(gridBackground.heightProperty().subtract(guidelineStrokeWidth));
                major.endXProperty().bind(cellSquareSize.multiply(row));

                major.strokeProperty().bind(guidelineColor);
                major.strokeWidthProperty().bind(guidelineStrokeWidth);

                guidelinePane.getChildren().add(major);
            });

            horizontalGuidelines.forEach(column -> {
                Line major = new Line();

                major.startYProperty().bind(cellSquareSize.multiply(column));
                major.startXProperty().bind(guidelineStrokeWidth.divide(2));

                major.endYProperty().bind(cellSquareSize.multiply(column));
                major.endXProperty().bind(gridBackground.widthProperty().subtract(guidelineStrokeWidth));

                major.strokeProperty().bind(guidelineColor);
                major.strokeWidthProperty().bind(guidelineStrokeWidth);

                guidelinePane.getChildren().add(major);
            });
        };

        horizontalGuidelines.addListener(guidelineRepaintListener);
        verticalGuidelines.addListener(guidelineRepaintListener);

        gridModel.get().getCells().forEach(cell -> {
            addedCell(pxPerCell, cell);
        });

        gridModel.get().cells().addListener((ListChangeListener<Cell<State>>) change -> {
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

    private void initMajorGuidelinesBindings(){
        numberOfHorizontalMajorGuidelines = NumberBindings.divideSafe(getGridModel().numberOfRows().subtract(1), horizontalGuidelineUnit);

        numberOfHorizontalMajorGuidelines.addListener((obs, oldValue, newValue) -> {
            horizontalGuidelines.clear();

            int number = newValue.intValue();
            int offset = horizontalGuidelineUnit.get();

            for (int i = 1; i <= number; i++) {
                horizontalGuidelines.add(i * offset);
            }
        });
        numberOfVerticalMajorGuidelines = NumberBindings.divideSafe(getGridModel().numberOfColumns().subtract(1), verticalGuidelineUnit);

        numberOfVerticalMajorGuidelines.addListener((obs, oldValue, newValue) -> {
            verticalGuidelines.clear();

            int number = newValue.intValue();
            int offset = verticalGuidelineUnit.get();

            for (int i = 1; i <= number; i++) {
                verticalGuidelines.add(i * offset);
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

        pane.borderProperty().bind(cellBorder);

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
        Color backgroundColor = Color.WHITE; // default color
        if(colorMapping.containsKey(cell.getState())){
            backgroundColor = colorMapping.get(cell.getState());
        }

        pane.setBackground(new Background(new BackgroundFill(backgroundColor, CornerRadii.EMPTY,
            Insets.EMPTY)));
        pane.getChildren().clear();
        final Function<Cell<State>, Node> nodeSupplier = nodeMapping.get(cell.getState());
        if (nodeSupplier != null) {
            pane.getChildren().add(nodeSupplier.apply(cell));
        }
    }

    private void updateCellBorder() {
        BorderWidths widths = new BorderWidths(cellBorderWidth.get());
        BorderStroke stroke = new BorderStroke(cellBorderColor.get(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY,
            widths);
        cellBorder.set(new Border(stroke));
    }

    public void setGridModel(GridModel<State> gridModel) {
        this.gridModel.set(gridModel);
    }

    public GridModel<State> getGridModel() {
        return gridModel.get();
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
        return cellBorderWidth;
    }

    /**
     * The color of the borders around each cell in the grid.
     *
     * Default value is: {@link Color#LIGHTGREY}.
     *
     * @return the border color as object property.
     */
    public ObjectProperty<Paint> cellBorderColorProperty() {
        return cellBorderColor;
    }


    /**
     * The width of the border around the whole grid.
     *
     * Default value is: 0.
     *
     * @return the width as double property.
     */
    public DoubleProperty gridBorderWidthProperty(){
        return gridBorderWidth;
    }

    /**
     * The color of the border around the whole grid.
     *
     * Default value is: {@link Color#TRANSPARENT}.
     *
     * @return the border color as object property.
     */
    public ObjectProperty<Paint> gridBorderColorProperty(){
        return gridBorderColor;
    }

    Pane getRootPane() {
        return rootPane;
    }


    public IntegerProperty horizontalGuidelineUnitProperty(){
        return horizontalGuidelineUnit;
    }
    public IntegerProperty verticalGuidelineUnitProperty(){
        return verticalGuidelineUnit;
    }

    public ObjectProperty<Color> guidelineColorProperty(){
        return guidelineColor;
    }

    public DoubleProperty guidelineStrokeWidth(){
        return guidelineStrokeWidth;
    }
}
