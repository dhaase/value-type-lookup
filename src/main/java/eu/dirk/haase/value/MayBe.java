package eu.dirk.haase.value;

public interface MayBe<T> {

    boolean isPresent();

    T get();

}
