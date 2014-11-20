package de.hszg.intagent.blockworld.core.conditions;


import de.hszg.intagent.blockworld.core.BlockStatus;

public class Clear implements Condition {

    private final BlockStatus block;

    public Clear(BlockStatus block){
        verifyBlock(block);
        this.block = block;
    }

    @Override
    public String toString() {
        return "Clear(" + block.name() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Clear clear = (Clear) o;

        if (block != clear.block) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return block != null ? block.hashCode() : 0;
    }
}
