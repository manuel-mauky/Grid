package de.hszg.intagent.blockworld.view;

import de.hszg.intagent.blockworld.core.BlockStatus;
import de.hszg.intagent.blockworld.core.Command;
import de.hszg.intagent.blockworld.core.conditions.Condition;
import de.saxsys.jfx.mvvm.api.FxmlView;
import de.saxsys.jfx.mvvm.api.InjectViewModel;
import de.saxsys.jfx.mvvm.viewloader.ViewLoader;
import de.saxsys.jfx.mvvm.viewloader.ViewTuple;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.ResourceBundle;

public class MainView implements FxmlView<MainViewModel>, Initializable {

    @FXML
    private GridPane centerGridPane;

    @FXML
    private ListView<Condition> conditionsList;

    @FXML
    private ListView<Command> commandList;

    @FXML
    private ChoiceBox<Integer> placeChoiceBox;

    @FXML
    private ChoiceBox<BlockStatus> blockChoiceBox;

    @FXML
    private Button nextStepButton;

    @FXML
    private Label errorLabel;


    @InjectViewModel
    private MainViewModel viewModel;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        ViewLoader viewLoader = new ViewLoader();

        final ViewTuple<WorldView, WorldViewModel> startViewTuple = viewLoader.loadViewTuple(WorldView.class);

        startViewTuple.getCodeBehind().getViewModel().setWorld(viewModel.getStartWorld());

        startViewTuple.getCodeBehind().getViewModel().titleProperty().set("Start");
        startViewTuple.getCodeBehind().makeEditable();
        centerGridPane.add(startViewTuple.getView(), 0, 0);


        final ViewTuple<WorldView, WorldViewModel> endViewTuple = viewLoader.loadViewTuple(WorldView.class);


        endViewTuple.getCodeBehind().getViewModel().setWorld(viewModel.getEndWorld());
        endViewTuple.getCodeBehind().getViewModel().titleProperty().set("Ziel");
        endViewTuple.getCodeBehind().makeEditable();

        centerGridPane.add(endViewTuple.getView(), 1, 0);


        final ViewTuple<WorldView, WorldViewModel> currentViewTuple = viewLoader.loadViewTuple(WorldView.class);

        currentViewTuple.getCodeBehind().getViewModel().setWorld(viewModel.getCurrentWorld());

        currentViewTuple.getCodeBehind().getViewModel().titleProperty().set("Aktuell");
        centerGridPane.add(currentViewTuple.getView(), 2, 0);

        commandList.setItems(viewModel.commandList());

        conditionsList.setItems(viewModel.conditionsList());

        placeChoiceBox.getItems().addAll(0, 1, 2);
        viewModel.placeChoiceProperty().bind(placeChoiceBox.valueProperty());
        placeChoiceBox.setValue(0);

        blockChoiceBox.setItems(viewModel.getCurrentWorld().getAddableBlockStates());
        viewModel.blockChoiceProperty().bind(blockChoiceBox.valueProperty());
        blockChoiceBox.setValue(BlockStatus.A);


        nextStepButton.disableProperty().bind(viewModel.nextButtonActive().not());


        errorLabel.textProperty().bind(viewModel.errorLabel());
        errorLabel.visibleProperty().bind(viewModel.errorLabel().isEmpty().not());
    }


    @FXML
    public void reset(){
        viewModel.reset();
    }

    @FXML
    public void step(){
        viewModel.step();
    }

    @FXML
    public void start(){
        viewModel.start();
    }

    @FXML
    public void pickup(){
        viewModel.pickup();
    }

    @FXML
    public void putdown(){
        viewModel.putdown();
    }
}
