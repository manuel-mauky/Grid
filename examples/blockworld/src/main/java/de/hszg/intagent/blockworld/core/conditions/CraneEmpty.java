package de.hszg.intagent.blockworld.core.conditions;

public class CraneEmpty implements Condition {

    @Override
    public String toString() {
        return "CraneEmpty";
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null){
            return false;
        }

        if(obj == this){
            return true;
        }

        return (obj instanceof CraneEmpty);
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
