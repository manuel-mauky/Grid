package de.hszg.intagent.blockworld.view;

import de.hszg.intagent.blockworld.core.BlockStatus;
import de.hszg.intagent.blockworld.core.Command;
import de.hszg.intagent.blockworld.core.Solver;
import de.hszg.intagent.blockworld.core.World;
import de.hszg.intagent.blockworld.core.conditions.Condition;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.*;
import javafx.collections.ObservableList;

import java.util.LinkedList;
import java.util.Queue;

public class MainViewModel implements ViewModel {

    private World startWorld;

    private World endWorld;

    private World currentWorld;

    private Solver solver;

    private IntegerProperty placeChoice = new SimpleIntegerProperty();

    private ObjectProperty<BlockStatus> blockChoice = new SimpleObjectProperty<>();

    private Queue<Command> commandQueue = new LinkedList<>();

    private BooleanProperty nextButtonActive = new SimpleBooleanProperty(false);

    private StringProperty errorLabel = new SimpleStringProperty();

    public MainViewModel() {
        startWorld = new World();
        endWorld = new World();
        currentWorld = new World();

        solver = new Solver(startWorld, endWorld);

        reset();
    }

    public World getStartWorld() {
        return startWorld;
    }

    public World getEndWorld() {
        return endWorld;
    }

    public World getCurrentWorld() {
        return currentWorld;
    }

    public void step() {
        final Command nextCommand = commandQueue.poll();

        if(commandQueue.isEmpty()){
            nextButtonActive.set(false);
        }

        currentWorld.addCommand(nextCommand);
    }

    public void reset() {
        startWorld.reset();
        startWorld.makeConfigurable();
        startWorld.addCrane();

        endWorld.reset();
        endWorld.makeConfigurable();
        endWorld.addCrane();

        currentWorld.reset();

        solver.reset();
        errorLabel.set("");
    }

    public ObservableList<Command> commandList() {
        return currentWorld.commands();
    }

    public ObservableList<Condition> conditionsList() {
        return currentWorld.conditions();
    }

    public void start() {
        currentWorld.conditions().clear();
        currentWorld.conditions().addAll(startWorld.conditions());
        currentWorld.addCrane();

        commandQueue.clear();
        commandQueue.addAll(solver.findSolution());

        if(commandQueue.isEmpty()){
            errorLabel.set("No possible Solution found");
            return;
        }

        nextButtonActive.set(true);
    }

    public void pickup() {
        System.out.println("pickup(" + placeChoice.get() + "," + blockChoice.get() + ")");
        boolean result = currentWorld.addCommand(Command.pickup(placeChoice.get(), blockChoice.get()));
        if(!result){
            System.out.println("Pickup invalid");
        }
    }

    public void putdown() {
        System.out.println("pickup(" + placeChoice.get() + "," + blockChoice.get() + ")");
        boolean result = currentWorld.addCommand(Command.putdown(placeChoice.get(), blockChoice.get()));
        if(!result){
            System.out.println("Putdown invalid");
        }
    }

    public IntegerProperty placeChoiceProperty(){
        return placeChoice;
    }

    public ObjectProperty<BlockStatus> blockChoiceProperty(){
        return blockChoice;
    }

    public ReadOnlyBooleanProperty nextButtonActive(){
        return nextButtonActive;
    }

    public ReadOnlyStringProperty errorLabel(){
        return errorLabel;
    }
}

