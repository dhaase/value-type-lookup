package eu.dirk.haase;

import eu.dirk.haase.value.NonValue;

public interface Valuefactory<T> {

    T valueOf(final CharSequence representation);
}
