package de.hszg.intagent.blockworld.core;

import de.hszg.intagent.blockworld.core.conditions.Condition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Solver {

    private final World start;
    private final World end;

    List<Command> finalSolution = new ArrayList<>();

    Set<Set<Condition>> clones = new HashSet<>();

    public Solver(World start, World end){
        this.start = start;
        this.end = end;
    }

    public List<Command> findSolution(){
        System.out.println("finding solution");
        clones.clear();

        List<List<Command>> possibleSolutions = createStartConfiguration();

        boolean solutionFound = false;

        int limit = 100;
        int counter = 0;



        while(!solutionFound){
            counter++;

            if(counter > limit){
                System.out.println("No possible solution found.");
                break;
            }
            solutionFound = executeCommands(possibleSolutions);

            if(solutionFound){
                break;
            }

            fillWithNewCommands(possibleSolutions);
        }

        return finalSolution;
    }

    List<List<Command>> createStartConfiguration() {
        List<List<Command>> possibleSolutions = new ArrayList<>();

        // Create start solution list
        final List<Command> commands = createCommands();
        commands.forEach(command->{
            List<Command> commandList = new ArrayList<>();
            commandList.add(command);

            possibleSolutions.add(commandList);
        });
        return possibleSolutions;
    }

    void fillWithNewCommands(List<List<Command>> possibleSolutions) {
        List<Command> commands = createCommands();
        List<List<Command>> todoAdd = new ArrayList<>();
        possibleSolutions.forEach(solution -> commands.forEach(command ->{
            final Command last = solution.get(solution.size() - 1);

            if(last.getType() != command.getType()){
                if(last.getBlock() == command.getBlock() && last.getPlaceNr() == command.getPlaceNr()){
                    return;
                }
            }

            List<Command> newSolution = new ArrayList<>();
            newSolution.addAll(solution);

            newSolution.add(command);

            todoAdd.add(newSolution);
        }));

        possibleSolutions.clear();
        possibleSolutions.addAll(todoAdd);
    }

    boolean executeCommands(List<List<Command>> possibleSolutions) {
        List<List<Command>> todoDelete = new ArrayList<>();

        for (List<Command> solution : possibleSolutions) {
            final World clone = start.clone();

            for (Command command : solution) {
                if(clone.addCommand(command)){
                    if(areConditionsEqual(end, clone)){
                        finalSolution = solution;
                        return true;
                    }
                }else{
                    todoDelete.add(solution);
                }
            }

            Set<Condition> cloneConditions = new HashSet<>();
            cloneConditions.addAll(clone.conditions());

            if(clones.contains(cloneConditions)){
                todoDelete.add(solution);
            }else{
                clones.add(cloneConditions);
            }
        }
        possibleSolutions.removeAll(todoDelete);

        return false;
    }

    private List<Command> createCommands() {
        List<Command> commands = new ArrayList<>();
        for(int placeNr=0 ; placeNr<World.SIZE ; placeNr++){
            for (BlockStatus blockStatus : BlockStatus.values()) {
                if(blockStatus.isBlock()){
                    commands.add(Command.pickup(placeNr, blockStatus));
                    commands.add(Command.putdown(placeNr, blockStatus));
                }
            }
        }
        return commands;
    }

    boolean areConditionsEqual(World one, World two){
        return one.conditions().containsAll(two.conditions()) &&
            two.conditions().containsAll(one.conditions());
    }

    public void reset(){
        clones.clear();
        finalSolution.clear();
    }
}
