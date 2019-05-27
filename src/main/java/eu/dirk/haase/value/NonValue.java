package eu.dirk.haase.value;

import eu.dirk.haase.Valuefactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public final class NonValue {

    public final static Predicate<Object> isNonValue = NonValue::isNonValue;

    @SuppressWarnings("unchecked")
    public static <T> T create(final Class<T> valueType, final Class<?> implClass) {
        try {
            return (T) create(valueType, (Valuefactory<?>) implClass.newInstance());
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new IllegalArgumentException(ex.toString(), ex);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T create(final Class<T> valueType, final Valuefactory<?> valuefactory) {
        final ClassLoader cl = valueType.getClassLoader();
        final Class<?>[] ifaces = {valueType, MayBe.class, NonValueMarker.class};
        return (T) Proxy.newProxyInstance(cl, ifaces, new NonValueHandler(valueType, valuefactory));
    }

    private static boolean isNonValue(final Object value) {
        return (value instanceof NonValueMarker);
    }

    private static class NonValueHandler implements InvocationHandler {

        final Class<?> valueType;
        final Valuefactory<?> valuefactory;

        NonValueHandler(final Class<?> valueType, final Valuefactory<?> valuefactory) {
            this.valueType = valueType;
            this.valuefactory = valuefactory;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            switch (method.getName()) {
                // Valuefactory-API
                case "valueOf":
                    return valuefactory.valueOf((String) args[0]);
                // MayBe-API
                case "get":
                    throw new NoSuchElementException();
                case "isPresent":
                    return false;
                // Object-API
                case "hashCode":
                    return valueType.hashCode();
                case "equals":
                    final Object thatObject = args[0];
                    if (valueType.isInstance(thatObject)) {
                        return isNonValue(thatObject);
                    }
                    return false;
                case "clone":
                    return proxy;
                case "toString":
                    return "Non of " + valueType;
                default:
                    throw new UnsupportedOperationException("");
            }
        }
    }

    private interface NonValueMarker {

    }
}
