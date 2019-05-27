package eu.dirk.haase.eu.dirk.haase.domain;

import eu.dirk.haase.value.NonValue;

public class MyGeldBetrag implements GeldBetrag {

    private final String value;

    public MyGeldBetrag() {
        this.value = null;
    }

    public MyGeldBetrag(final String value) {
        this.value = value;
    }

    @Override
    public boolean isNonValue() {
        return (this.value == null);
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public GeldBetrag valueOf(CharSequence representation) {
        if (representation != null) {
            return new MyGeldBetrag(representation.toString());
        } else {
            return NonValue.create(GeldBetrag.class, this);
        }
    }

}
