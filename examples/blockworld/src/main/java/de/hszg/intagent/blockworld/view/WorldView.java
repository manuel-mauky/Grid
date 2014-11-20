package de.hszg.intagent.blockworld.view;

import de.hszg.intagent.blockworld.core.BlockStatus;
import de.hszg.intagent.blockworld.core.World;
import de.saxsys.jfx.mvvm.api.FxmlView;
import de.saxsys.jfx.mvvm.api.InjectViewModel;
import eu.lestard.grid.GridView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.controlsfx.control.PopOver;

import java.net.URL;
import java.util.ResourceBundle;

public class WorldView implements FxmlView<WorldViewModel>, Initializable {

    @FXML
    private Label title;

    @FXML
    private AnchorPane worldPane;

    @InjectViewModel
    private WorldViewModel viewModel;

    private GridView<BlockStatus> gridView;

    private PopOver addPopover = new PopOver();

    public WorldView() {
        addPopover.setDetachedTitle("Add");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        title.textProperty().bind(viewModel.titleProperty());

        gridView = new GridView<>();
        worldPane.getChildren().add(gridView);


        viewModel.world().addListener((observable, oldWorld, newWorld)->{
            if(newWorld != null){
                gridView.setGridModel(newWorld.getGridModel());
            }
        });

        initGridView(gridView);

        initCrane(gridView);
    }

    public void makeEditable(){
        if(viewModel.getWorld() != null){
            initEditableWorld(gridView, viewModel.getWorld());
        }
    }


    private void initCrane(GridView<BlockStatus> gridView) {
        Image img = new Image("greifer.png");

        ImageView imgView = new ImageView(img);

        imgView.fitHeightProperty().bind(gridView.cellSizeProperty());
        imgView.fitWidthProperty().bind(gridView.cellSizeProperty());

        gridView.addNodeMapping(BlockStatus.CRANE, c -> imgView);
    }


    private void initGridView(GridView<BlockStatus> gridView) {

        AnchorPane.setLeftAnchor(gridView, 0.0);
        AnchorPane.setTopAnchor(gridView, 0.0);
        AnchorPane.setBottomAnchor(gridView, 0.0);
        AnchorPane.setRightAnchor(gridView, 0.0);

        gridView.cellBorderColorProperty().set(Color.BLACK);
        gridView.cellBorderWidthProperty().set(1);

        gridView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        gridView.addColorMapping(BlockStatus.EMPTY, Color.WHITE);
        gridView.addColorMapping(BlockStatus.A, Color.YELLOWGREEN);
        gridView.addColorMapping(BlockStatus.B, Color.YELLOWGREEN);
        gridView.addColorMapping(BlockStatus.C, Color.YELLOWGREEN);
        gridView.addColorMapping(BlockStatus.D, Color.YELLOWGREEN);

        gridView.addNodeMapping(BlockStatus.A, c -> createLabel(BlockStatus.A.name()));
        gridView.addNodeMapping(BlockStatus.B, c -> createLabel(BlockStatus.B.name()));
        gridView.addNodeMapping(BlockStatus.C, c -> createLabel(BlockStatus.C.name()));
        gridView.addNodeMapping(BlockStatus.D, c -> createLabel(BlockStatus.D.name()));
    }

    private void initEditableWorld(GridView<BlockStatus> gridView, World world) {
        world.makeConfigurable();
        gridView.addNodeMapping(BlockStatus.ADD_BUTTON, c -> createLabel("+"));
        gridView.getGridModel().getCells().forEach(cell -> cell.setOnClick(event -> {
            if (cell.getState() == BlockStatus.ADD_BUTTON) {

                HBox buttons = new HBox();
                buttons.setAlignment(Pos.CENTER);
                buttons.setSpacing(3);
                buttons.setPadding(new Insets(5));
                buttons.setMinWidth(80);

                world.getAddableBlockStates().forEach(state -> {
                    Button button = new Button(state.name());

                    button.setOnAction(actionEvent -> {
                        world.addBlockOnTop(cell.getColumn(), state);

                        addPopover.hide();
                    });

                    buttons.getChildren().add(button);
                });

                addPopover.setContentNode(buttons);

                final Pane cellPane = gridView.getCellPane(cell);

                addPopover.show(cellPane, event.getScreenX(), event.getScreenY());

                if (!addPopover.isDetached()) {
                    addPopover.detach();
                }
            }
        }));
    }

    private Node createLabel(String txt) {

        Text text = new Text();
        text.setFont(new Font(23));
        text.setText(txt);

        return text;
    }

    public WorldViewModel getViewModel() {
        return viewModel;
    }
}

