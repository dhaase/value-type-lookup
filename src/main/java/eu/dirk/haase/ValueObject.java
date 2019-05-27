package eu.dirk.haase;

import eu.dirk.haase.Valuefactory;

public interface ValueObject<T> extends Valuefactory<T> {


    default boolean isNonValue() {
        return false;
    }

}
