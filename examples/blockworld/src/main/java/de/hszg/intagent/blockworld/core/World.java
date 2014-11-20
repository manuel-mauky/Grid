package de.hszg.intagent.blockworld.core;

import de.hszg.intagent.blockworld.core.conditions.*;
import eu.lestard.grid.Cell;
import eu.lestard.grid.GridModel;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class World {

    public static final int HEIGHT = 6;
    public static final int SIZE = 3;


    private int craneColumn = 0;

    private GridModel<BlockStatus> gridModel = new GridModel<>();


    private ObservableList<Condition> conditions = FXCollections.observableArrayList();

    private ObservableList<Command> commands = FXCollections.observableArrayList();

    private ObservableList<BlockStatus> addableBlocks = FXCollections.observableArrayList();

    private boolean configurable = false;

    private final ListChangeListener<Condition> conditionListener = c -> {
        c.next();

        boolean shouldAddButtons = !getAddableBlockStates().isEmpty();
        if (!shouldAddButtons) {
            gridModel.getCells().stream()
                .filter(cell -> cell.getState() == BlockStatus.ADD_BUTTON)
                .forEach(cell -> cell.changeState(BlockStatus.EMPTY));
        }

        if (c.wasAdded()) {
            c.getAddedSubList().forEach(condition -> {
                if (condition instanceof OnTable) {
                    OnTable onTable = (OnTable) condition;
                    gridModel.getCell(onTable.getPlaceNr(), HEIGHT - 1).changeState(onTable.getBlock());

                    if (shouldAddButtons && configurable) {
                        gridModel.getCell(onTable.getPlaceNr(), HEIGHT - 2).changeState(BlockStatus.ADD_BUTTON);
                    }
                }

                if (condition instanceof On) {
                    On on = (On) condition;

                    final Optional<Cell<BlockStatus>> bottomCell = gridModel.cells().stream()
                        .filter(cell -> cell.getState() == on.getBottom()).findFirst();

                    if (bottomCell.isPresent()) {

                        final int column = bottomCell.get().getColumn();
                        final int row = bottomCell.get().getRow();
                        gridModel.getCell(column, row - 1).changeState(on.getTop());

                        if (shouldAddButtons && configurable) {
                            gridModel.getCell(column, row - 2).changeState(BlockStatus.ADD_BUTTON);
                        }
                    }
                }

                if(condition instanceof CraneEmpty){
                    gridModel.getCell(craneColumn,0).changeState(BlockStatus.CRANE);
                }

                if(condition instanceof CraneHolds){
                    CraneHolds holds = (CraneHolds)condition;

                    final BlockStatus block = holds.getBlock();

                    gridModel.getCell(craneColumn,0).changeState(BlockStatus.CRANE);
                    gridModel.getCell(craneColumn, 1).changeState(block);
                }
            });
        }


        if(c.wasRemoved()){
            c.getRemoved().forEach(condition->{

                if(condition instanceof CraneHolds){
                    gridModel.getCell(craneColumn, 0).changeState(BlockStatus.EMPTY);
                    gridModel.getCell(craneColumn, 1).changeState(BlockStatus.EMPTY);
                }

                if(condition instanceof OnTable){
                    OnTable onTable = (OnTable) condition;
                    final Cell<BlockStatus> cell = gridModel.getCellsWithState(onTable.getBlock()).get(0);

                    cell.changeState(BlockStatus.EMPTY);
                }

                if(condition instanceof On){
                    On on = (On)condition;

                    final Cell<BlockStatus> cell = gridModel.getCellsWithState(on.getTop()).get(0);

                    cell.changeState(BlockStatus.EMPTY);
                }

                if(condition instanceof CraneEmpty){
                    gridModel.getCell(craneColumn,0).changeState(BlockStatus.EMPTY);
                }

            });
        }
    };


    public World() {

        gridModel.setDefaultState(BlockStatus.EMPTY);
        gridModel.setNumberOfColumns(SIZE);
        gridModel.setNumberOfRows(HEIGHT);

        reset();

        boolean shouldAddButtons = !getAddableBlockStates().isEmpty();
        gridModel.getCells().forEach(cell -> {
            if (cell.getRow() == HEIGHT - 1 && shouldAddButtons && configurable) {
                cell.changeState(BlockStatus.ADD_BUTTON);
            } else {
                cell.changeState(BlockStatus.EMPTY);
            }
        });

        conditions.addListener(conditionListener);
    }

    public void makeConfigurable() {
        this.configurable = true;
        for (int i = 0; i < SIZE; i++) {
            gridModel.getCell(i, HEIGHT - 1).changeState(BlockStatus.ADD_BUTTON);
        }
    }

    public ObservableList<BlockStatus> getAddableBlockStates() {
        return addableBlocks;
    }

    public GridModel<BlockStatus> getGridModel() {
        return gridModel;
    }

    private void validatePlaceNr(int placeNr){
        if (placeNr < 0 || placeNr >= SIZE) {
            throw new IllegalArgumentException("The place nr may only be between 0 and (excluding) " + SIZE);
        }
    }

    private void validateBlock(BlockStatus status) {
        if (!status.isBlock()) {
            throw new IllegalArgumentException("The status param may only be one of the valid Labels (A,B,C,D)");
        }
    }


    public void addBlockOnTop(int placeNr, BlockStatus status) {
        validatePlaceNr(placeNr);

        validateBlock(status);

        if (!getAddableBlockStates().contains(status)) {
            throw new IllegalArgumentException("The block that you are trying to add is already placed in this world");
        }

        addableBlocks.remove(status);

        addBlockInConditions(placeNr, status);
    }

    public boolean addCommand(Command command){
        int placeNr = command.getPlaceNr();
        BlockStatus block = command.getBlock();

        validatePlaceNr(placeNr);
        validateBlock(block);

        if(command.getType() == Command.Type.PICKUP){
            return pickUp(command);
        }else if(command.getType() == Command.Type.PUTDOWN){
            return putDown(command);
        }

        return false;
    }

    private boolean pickUp(Command command){
        int placeNr = command.getPlaceNr();
        BlockStatus block = command.getBlock();

        if(getCraneHoldsStream().count() != 0 && getCraneEmptyStream().count() != 1){
            return false;
        }

        final Optional<BlockStatus> topBlockOptional = getTopBlockOfPlace(placeNr);

        if(topBlockOptional.isPresent()){
            if(topBlockOptional.get() != block){
                return false;
            }
        }else{
            return false;
        }

        final Optional<On> onOptional = getOnStream().filter(on -> on.getTop() == block).findFirst();

        if(onOptional.isPresent()){

            conditions.remove(onOptional.get());
            conditions.remove(new CraneEmpty());

            craneColumn = placeNr;
            conditions.add(new CraneHolds(block));

            commands.add(command);
            return true;
        }else{
            final Optional<OnTable> onTableOptional = getOnTableStream().filter(onTable -> onTable.getBlock() == block).findFirst();

            if(onTableOptional.isPresent()){
                conditions.remove(onTableOptional.get());
                conditions.remove(new CraneEmpty());
                craneColumn = placeNr;

                conditions.add(new TableEmpty(placeNr));

                conditions.add(new CraneHolds(block));

                commands.add(command);
                return true;
            }
        }

        return false;
    }

    private boolean putDown(Command command){
        int placeNr = command.getPlaceNr();
        BlockStatus block = command.getBlock();


        final Optional<CraneHolds> craneHoldsOptional = getCraneHoldsStream().findFirst();

        if(craneHoldsOptional.isPresent()){
            if(craneHoldsOptional.get().getBlock() == block){
                final Optional<TableEmpty> tableEmptyOptional = getTableEmptyStream().filter(tE -> tE.getPlaceNr() == placeNr).findFirst();

                if(tableEmptyOptional.isPresent()){
                    conditions.remove(tableEmptyOptional.get());

                    conditions.add(new OnTable(placeNr, block));

                } else {
                    final Optional<BlockStatus> topBlockOptional = getTopBlockOfPlace(placeNr);

                    final BlockStatus currentTopBlock = topBlockOptional.get();

                    conditions.add(new On(block, currentTopBlock));
                }

                conditions.remove(craneHoldsOptional.get());
                craneColumn = placeNr;
                conditions.add(new CraneEmpty());
                commands.add(command);
                return true;
            }
        }

        return false;
    }


    private void addBlockInConditions(int placeNr, BlockStatus status) {

        if (conditions.contains(new TableEmpty(placeNr))) {
            conditions.remove(new TableEmpty(placeNr));
            conditions.add(new OnTable(placeNr, status));
            return;
        }



        Optional<BlockStatus> topBlock = getTopBlockOfPlace(placeNr);

        if(topBlock.isPresent()){
            conditions.add(new On(status, topBlock.get()));
        }else{
            throw new IllegalStateException("Invalid Conditions. " +
                "There is not 'TableEmpty' condition for the place nr [" + placeNr + "] " +
                "so there has to be exactly one 'OnTable' condition!");
        }
    }

    private Optional<BlockStatus> getTopBlockOfPlace(int placeNr) {

        if(getTableEmptyStream().filter(tE -> tE.getPlaceNr() == placeNr).findFirst().isPresent()){
            return Optional.empty();
        }

        // find the first block on this table place
        final List<BlockStatus> blocks = getOnTableStream()
            .filter(onTable -> onTable.getPlaceNr() == placeNr)
            .map(OnTable::getBlock)
            .collect(Collectors.toList());

        if (blocks.size() == 1) {
            BlockStatus block = blocks.get(0);

            boolean tmp = true;

            while (tmp) {
                final Optional<BlockStatus> blockOnTop = getBlockOnTopOf(block);

                if (blockOnTop.isPresent()) {
                    block = blockOnTop.get();
                } else {
                    tmp = false;
                }
            }

            return Optional.of(block);
         }

        return Optional.empty();
    }

    private Optional<BlockStatus> getBlockOnTopOf(final BlockStatus block) {
        return getOnStream()
            .filter(on -> on.getBottom() == block)
            .map(On::getTop)
            .findFirst();
    }

    public void reset() {
        addableBlocks.clear();
        addableBlocks.addAll(BlockStatus.A, BlockStatus.B, BlockStatus.C, BlockStatus.D);

        commands.clear();

        gridModel.getCellsWithState(BlockStatus.ADD_BUTTON).forEach(cell -> cell.changeState(BlockStatus.EMPTY));

        conditions.clear();

        conditions.add(new TableEmpty(0));
        conditions.add(new TableEmpty(1));
        conditions.add(new TableEmpty(2));

    }

    public ObservableList<Condition> conditions() {
        return conditions;
    }

    public ObservableList<Command> commands(){
        return commands;
    }

    public void addCrane() {
        conditions.add(new CraneEmpty());
    }


    public World clone(){
        World clone = new World();

        if(this.configurable){
            clone.makeConfigurable();
        }

        clone.conditions.clear();
        clone.conditions.addAll(this.conditions());

        clone.commands.addAll(this.commands());

        this.getGridModel().getCells().forEach(cell->{
            if(cell.getState() != BlockStatus.EMPTY){
                clone.getGridModel().getCell(cell.getColumn(), cell.getRow()).changeState(cell.getState());
            }
        });

        return clone;
    }

    private Stream<OnTable> getOnTableStream(){
        return conditions.stream().filter(c->c instanceof OnTable)
            .map(c -> (OnTable) c);
    }

    private Stream<On> getOnStream(){
        return conditions.stream().filter(c->c instanceof On)
            .map(c -> (On) c);
    }

    private Stream<CraneHolds> getCraneHoldsStream(){
        return conditions.stream().filter(c -> c instanceof CraneHolds)
            .map(c -> (CraneHolds) c);
    }

    private Stream<TableEmpty> getTableEmptyStream(){
        return conditions.stream().filter(c -> c instanceof TableEmpty)
            .map(c -> (TableEmpty) c);
    }

    private Stream<CraneEmpty> getCraneEmptyStream(){
        return conditions.stream().filter(c -> c instanceof CraneEmpty)
            .map(c -> (CraneEmpty) c);
    }
}

