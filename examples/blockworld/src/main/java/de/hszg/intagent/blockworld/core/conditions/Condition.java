package de.hszg.intagent.blockworld.core.conditions;

import de.hszg.intagent.blockworld.core.BlockStatus;
import de.hszg.intagent.blockworld.core.World;

public interface Condition {

    default void verifyBlock(BlockStatus block){
        if(!block.isBlock()){
            throw new IllegalArgumentException("The block may only be one of [A,B,C,D] but was:" + block);
        }
    }

    default void verifyPlaceNr(int placeNr){
        if(placeNr < 0 || placeNr >= World.SIZE){
            throw new IllegalArgumentException("The placeNr has to be between (including) 0 and (excluding) " + World.SIZE);
        }
    }
}
