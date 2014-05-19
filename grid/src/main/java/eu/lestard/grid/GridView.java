package eu.lestard.grid;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

import java.util.HashMap;
import java.util.Map;

public class GridView<State extends Enum> extends StackPane {


    private Pane rootPane = new Pane();

    private Map<State, Color> colorMapping = new HashMap<>();

    private ObjectProperty<GridModel<State>> gridModelProperty = new SimpleObjectProperty<>();

    private Map<Cell<State>, Rectangle> rectangleMap = new HashMap<>();

    private ObjectProperty<Paint> strokeProperty = new SimpleObjectProperty<>(Color.LIGHTGREY);

    private DoubleProperty strokeWidthProperty = new SimpleDoubleProperty(1);

    public GridView() {
        final NumberBinding fullSize = Bindings.min(this.widthProperty(), this.heightProperty());

        rootPane.maxWidthProperty().bind(fullSize);
        rootPane.maxHeightProperty().bind(fullSize);

        this.getChildren().add(rootPane);

        rootPane.setStyle("-fx-border-color:black");

        gridModelProperty.addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                initGridModel();
            }
        });
    }

    private void initGridModel() {
        final NumberBinding size = Bindings.max(getGridModel().numberOfColumns(), getGridModel().numberOfRows());
        NumberBinding pxPerCell = Bindings.min(rootPane.widthProperty(), rootPane.heightProperty()).divide(size);

        gridModelProperty.get().getCells().forEach(cell->{
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
                        final Rectangle rectangle = rectangleMap.get(cell);
                        rootPane.getChildren().remove(rectangle);

                        rectangleMap.remove(cell);
                    });
                }
            }
        });

    }

    private void addedCell(NumberBinding pxPerCell, Cell<State> cell) {
        NumberBinding xStart = pxPerCell.multiply(cell.getColumn());
        NumberBinding yStart = pxPerCell.multiply(cell.getRow());

        Rectangle rectangle = new Rectangle();
        rectangle.setStrokeType(StrokeType.INSIDE);
        rectangle.strokeProperty().bind(strokeProperty);
        rectangle.strokeWidthProperty().bind(strokeWidthProperty);

        rectangle.xProperty().bind(xStart);
        rectangle.yProperty().bind(yStart);

        rectangle.widthProperty().bind(pxPerCell);
        rectangle.heightProperty().bind(pxPerCell);

        rectangle.setFill(colorMapping.get(cell.stateProperty().get()));

        cell.stateProperty().addListener((obs, oldValue, newValue) -> {
            rectangle.setFill(colorMapping.get(newValue));
        });

        rectangleMap.put(cell, rectangle);

        rootPane.getChildren().add(rectangle);
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

    public ReadOnlyDoubleProperty rootWidthProperty(){
        return rootPane.widthProperty();
    }

    public ReadOnlyDoubleProperty rootHeightProperty(){
        return rootPane.heightProperty();
    }

    public ReadOnlyDoubleProperty rootLayoutXProperty(){
        return rootPane.layoutXProperty();
    }

    public ReadOnlyDoubleProperty rootLayoutYProperty(){
        return rootPane.layoutYProperty();
    }

    public DoubleProperty strokeWidthProperty(){
        return strokeWidthProperty;
    }

    public ObjectProperty<Paint> strokeProperty(){
        return strokeProperty;
    }

    Pane getRootPane() {
        return rootPane;
    }


}
