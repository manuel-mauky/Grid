package de.hszg.intagent.blockworld.core;

public class Command {


    public static enum Type{
        PICKUP,
        PUTDOWN
    }

    private final int placeNr;
    private final BlockStatus block;
    private final Type type;



    private Command(int placeNr, BlockStatus block, Type type){
        this.placeNr = placeNr;
        this.block = block;
        this.type = type;
    }

    public static Command pickup(int placeNr, BlockStatus block){
        return new Command(placeNr, block, Type.PICKUP);
    }

    public static Command putdown(int placeNr, BlockStatus block){
        return new Command(placeNr, block, Type.PUTDOWN);
    }

    public int getPlaceNr() {
        return placeNr;
    }

    public BlockStatus getBlock() {
        return block;
    }

    public Type getType() {
        return type;
    }


    @Override
    public boolean equals(Object obj) {
        if(this == obj){
            return true;
        }

        if(obj == null){
            return false;
        }

        if(!(obj instanceof Command)){
            return false;
        }

        Command other = (Command) obj;

        return other.getType() == this.getType() &&
            other.getPlaceNr() == this.getPlaceNr() &&
            other.getBlock() == this.getBlock();
    }

    @Override
    public int hashCode() {
        return this.getType().hashCode() + Integer.hashCode(this.getPlaceNr()) + this.getBlock().hashCode();
    }

    @Override
    public String toString() {
        return getType() + "(" + placeNr + "," + block.name() + ")";
    }
}
