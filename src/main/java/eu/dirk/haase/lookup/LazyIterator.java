package eu.dirk.haase.lookup;

import java.io.IOException;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.function.Function;

class LazyIterator<V1> implements Iterator<Function<String, V1>> {

    private final AccessControlContext acc;
    private final ClassLoader loader;
    private final ConfigurationFileParser<V1> parser;
    private final LinkedHashMap<String, Function<String, V1>> providers;
    private final Class<Function<String, V1>> valueFactoryClass;

    private Enumeration<URL> configs = null;
    private String nextName = null;
    private Iterator<String> pending = null;

    LazyIterator(final ConfigurationFileParser<V1> parser,
                 final LinkedHashMap<String, Function<String, V1>> providers,
                 final AccessControlContext acc,
                 final Class<Function<String, V1>> valueFactoryClass,
                 final ClassLoader loader) {
        this.parser = parser;
        this.providers = providers;
        this.valueFactoryClass = valueFactoryClass;
        this.acc = acc;
        this.loader = loader;
    }

     public boolean hasNext() {
        if (acc == null) {
            return hasNextValueFactory();
        } else {
            PrivilegedAction<Boolean> action = this::hasNextValueFactory;
            return AccessController.doPrivileged(action, acc);
        }
    }

    private boolean hasNextValueFactory() {
        if (nextName != null) {
            return true;
        }
        if (configs == null) {
            try {
                String fullName = ValueTypeLoader.PREFIX + valueFactoryClass.getName();
                if (loader == null) {
                    configs = ClassLoader.getSystemResources(fullName);
                } else {
                    configs = loader.getResources(fullName);
                }
            } catch (IOException x) {
                throw ValueTypeLoaderError.fail(valueFactoryClass, "Error locating configuration files", x);
            }
        }
        while ((pending == null) || !pending.hasNext()) {
            if (!configs.hasMoreElements()) {
                return false;
            }
            pending = parser.parse(valueFactoryClass, configs.nextElement());
        }
        nextName = pending.next();
        return true;
    }

    public Function<String, V1> next() {
        if (acc == null) {
            return nextValueFactory();
        } else {
            PrivilegedAction<Function<String, V1>> action = this::nextValueFactory;
            return AccessController.doPrivileged(action, acc);
        }
    }

    private Function<String, V1> nextValueFactory() {
        if (!hasNextValueFactory())
            throw new NoSuchElementException();
        String cn = nextName;
        nextName = null;
        Class<?> c = null;
        try {
            c = Class.forName(cn, false, loader);
        } catch (ClassNotFoundException x) {
            throw ValueTypeLoaderError.fail(valueFactoryClass,
                    "Provider " + cn + " not found");
        }
        if (!valueFactoryClass.isAssignableFrom(c)) {
            throw ValueTypeLoaderError.fail(valueFactoryClass,
                    "Provider " + cn + " not a subtype");
        }
        try {
            Function<String, V1> p = valueFactoryClass.cast(c.newInstance());
            providers.put(cn, p);
            return p;
        } catch (Throwable x) {
            throw ValueTypeLoaderError.fail(valueFactoryClass,
                    "Provider " + cn + " could not be instantiated",
                    x);
        }
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

}