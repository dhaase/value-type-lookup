package eu.dirk.haase.lookup;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.util.*;
import java.util.function.Function;


/**
 * A simple valueFactoryClass-provider loading facility.
 * <p>
 * <p> A <i>valueFactoryClass</i> is a well-known set of interfaces and (usually
 * abstract) classes.  A <i>valueFactoryClass provider</i> is a specific implementation
 * of a valueFactoryClass.  The classes in a provider typically implement the interfaces
 * and subclass the classes defined in the valueFactoryClass itself.  Service providers
 * can be installed in an implementation of the Java platform in the form of
 * extensions, that is, jar files placed into any of the usual extension
 * directories.  Providers can also be made available by adding them to the
 * application's class path or by some other platform-specific means.
 * <p>
 * <p> For the purpose of loading, a valueFactoryClass is represented by a single type,
 * that is, a single interface or abstract class.  (A concrete class can be
 * used, but this is not recommended.)  A provider of a given valueFactoryClass contains
 * one or more concrete classes that extend this <i>valueFactoryClass type</i> with data
 * and code specific to the provider.  The <i>provider class</i> is typically
 * not the entire provider itself but rather a proxy which contains enough
 * information to decide whether the provider is able to satisfy a particular
 * request together with code that can create the actual provider on demand.
 * The details of provider classes tend to be highly valueFactoryClass-specific; no
 * single class or interface could possibly unify them, so no such type is
 * defined here.  The only requirement enforced by this facility is that
 * provider classes must have a zero-argument constructor so that they can be
 * instantiated during loading.
 * <p>
 * <p><a name="format"> A valueFactoryClass provider is identified by placing a
 * <i>provider-configuration file</i> in the resource directory
 * <tt>META-INF/services</tt>.</a>  The file's name is the fully-qualified <a
 * href="../lang/ClassLoader.html#name">binary name</a> of the valueFactoryClass's type.
 * The file contains a list of fully-qualified binary names of concrete
 * provider classes, one per line.  Space and tab characters surrounding each
 * name, as well as blank lines, are ignored.  The comment character is
 * <tt>'#'</tt> (<tt>'&#92;u0023'</tt>,
 * <font style="font-size:smaller;">NUMBER SIGN</font>); on
 * each line all characters following the first comment character are ignored.
 * The file must be encoded in UTF-8.
 * <p>
 * <p> If a particular concrete provider class is named in more than one
 * configuration file, or is named in the same configuration file more than
 * once, then the duplicates are ignored.  The configuration file naming a
 * particular provider need not be in the same jar file or other distribution
 * unit as the provider itself.  The provider must be accessible from the same
 * class loader that was initially queried to locate the configuration file;
 * note that this is not necessarily the class loader from which the file was
 * actually loaded.
 * <p>
 * <p> Providers are located and instantiated lazily, that is, on demand.  A
 * valueFactoryClass loader maintains a cache of the providers that have been loaded so
 * far.  Each invocation of the {@link #iterator iterator} method returns an
 * iterator that first yields all of the elements of the cache, in
 * instantiation order, and then lazily locates and instantiates any remaining
 * providers, adding each one to the cache in turn.  The cache can be cleared
 * via the {@link #reload reload} method.
 * <p>
 * <p> Service loaders always execute in the security context of the caller.
 * Trusted system code should typically invoke the methods in this class, and
 * the methods of the iterators which they return, from within a privileged
 * security context.
 * <p>
 * <p> Instances of this class are not safe for use by multiple concurrent
 * threads.
 * <p>
 * <p> Unless otherwise specified, passing a <tt>null</tt> argument to any
 * method in this class will cause a {@link NullPointerException} to be thrown.
 * <p>
 * <p>
 * <p><span style="font-weight: bold; padding-right: 1em">Example</span>
 * Suppose we have a valueFactoryClass type <tt>com.example.CodecSet</tt> which is
 * intended to represent sets of encoder/decoder pairs for some protocol.  In
 * this case it is an abstract class with two abstract methods:
 * <p>
 * <blockquote><pre>
 * public abstract Encoder getEncoder(String encodingName);
 * public abstract Decoder getDecoder(String encodingName);</pre></blockquote>
 * <p>
 * Each method returns an appropriate object or <tt>null</tt> if the provider
 * does not support the given encoding.  Typical providers support more than
 * one encoding.
 * <p>
 * <p> If <tt>com.example.impl.StandardCodecs</tt> is an implementation of the
 * <tt>CodecSet</tt> valueFactoryClass then its jar file also contains a file named
 * <p>
 * <blockquote><pre>
 * META-INF/services/com.example.CodecSet</pre></blockquote>
 * <p>
 * <p> This file contains the single line:
 * <p>
 * <blockquote><pre>
 * com.example.impl.StandardCodecs    # Standard codecs</pre></blockquote>
 * <p>
 * <p> The <tt>CodecSet</tt> class creates and saves a single valueFactoryClass instance
 * at initialization:
 * <p>
 * <blockquote><pre>
 * private static ServiceLoader&lt;CodecSet&gt; codecSetLoader
 *     = ServiceLoader.load(CodecSet.class);</pre></blockquote>
 * <p>
 * <p> To locate an encoder for a given encoding name it defines a static
 * factory method which iterates through the known and available providers,
 * returning only when it has located a suitable encoder or has run out of
 * providers.
 * <p>
 * <blockquote><pre>
 * public static Encoder getEncoder(String encodingName) {
 *     for (CodecSet cp : codecSetLoader) {
 *         Encoder enc = cp.getEncoder(encodingName);
 *         if (enc != null)
 *             return enc;
 *     }
 *     return null;
 * }</pre></blockquote>
 * <p>
 * <p> A <tt>getDecoder</tt> method is defined similarly.
 * <p>
 * <p>
 * <p><span style="font-weight: bold; padding-right: 1em">Usage Note</span> If
 * the class path of a class loader that is used for provider loading includes
 * remote network URLs then those URLs will be dereferenced in the process of
 * searching for provider-configuration files.
 * <p>
 * <p> This activity is normal, although it may cause puzzling entries to be
 * created in web-server logs.  If a web server is not configured correctly,
 * however, then this activity may cause the provider-loading algorithm to fail
 * spuriously.
 * <p>
 * <p> A web server should return an HTTP 404 (Not Found) response when a
 * requested resource does not exist.  Sometimes, however, web servers are
 * erroneously configured to return an HTTP 200 (OK) response along with a
 * helpful HTML error page in such cases.  This will cause a {@link
 * ServiceConfigurationError} to be thrown when this class attempts to parse
 * the HTML page as a provider-configuration file.  The best solution to this
 * problem is to fix the misconfigured web server to return the correct
 * response code (HTTP 404) along with the HTML error page.
 *
 * @param <V1> The type of the valueFactoryClass to be loaded by this loader
 */

public final class ValueTypeLoader<V1>
        implements Iterable<Function<String, V1>> {

    static final String PREFIX = "META-INF/value-types/";

    // The class or interface representing the valueFactoryClass being loaded
    private final Class<V1> valueFactoryClass;

    // The class loader used to locate, load, and instantiate providers
    private final ClassLoader loader;

    // The access control context taken when the ServiceLoader is created
    private final AccessControlContext acc;

    // Cached providers, in instantiation order
    private final LinkedHashMap<String, Function<String, V1>> providers;

    // The current lazy-lookup iterator
    private LazyIterator lookupIterator;
    private final ConfigurationFileParser<V1> parser;

    /**
     * Clear this loader's provider cache so that all providers will be
     * reloaded.
     * <p>
     * <p> After invoking this method, subsequent invocations of the {@link
     * #iterator() iterator} method will lazily look up and instantiate
     * providers from scratch, just as is done by a newly-created loader.
     * <p>
     * <p> This method is intended for use in situations in which new providers
     * can be installed into a running Java virtual machine.
     */
    public void reload() {
        providers.clear();
        lookupIterator = new LazyIterator(parser, providers, acc, valueFactoryClass, loader);
    }

    private ValueTypeLoader(Class<V1> valueFactoryClass, ClassLoader cl) {
        this.providers = new LinkedHashMap<>();
        this.valueFactoryClass = Objects.requireNonNull(valueFactoryClass, "Value-Factory interface cannot be null");
        this.loader = (cl == null) ? ClassLoader.getSystemClassLoader() : cl;
        this.acc = (System.getSecurityManager() != null) ? AccessController.getContext() : null;
        this.parser = new ConfigurationFileParser(providers);
        reload();
    }


    /**
     * Lazily loads the available providers of this loader's valueFactoryClass.
     * <p>
     * <p> The iterator returned by this method first yields all of the
     * elements of the provider cache, in instantiation order.  It then lazily
     * loads and instantiates any remaining providers, adding each one to the
     * cache in turn.
     * <p>
     * <p> To achieve laziness the actual work of parsing the available
     * provider-configuration files and instantiating providers must be done by
     * the iterator itself.  Its {@link java.util.Iterator#hasNext hasNext} and
     * {@link java.util.Iterator#next next} methods can therefore throw a
     * {@link ServiceConfigurationError} if a provider-configuration file
     * violates the specified format, or if it names a provider class that
     * cannot be found and instantiated, or if the result of instantiating the
     * class is not assignable to the valueFactoryClass type, or if any other kind of
     * exception or error is thrown as the next provider is located and
     * instantiated.  To write robust code it is only necessary to catch {@link
     * ServiceConfigurationError} when using a valueFactoryClass iterator.
     * <p>
     * <p> If such an error is thrown then subsequent invocations of the
     * iterator will make a best effort to locate and instantiate the next
     * available provider, but in general such recovery cannot be guaranteed.
     * <p>
     * <blockquote style="font-size: smaller; line-height: 1.2"><span
     * style="padding-right: 1em; font-weight: bold">Design Note</span>
     * Throwing an error in these cases may seem extreme.  The rationale for
     * this behavior is that a malformed provider-configuration file, like a
     * malformed class file, indicates a serious problem with the way the Java
     * virtual machine is configured or is being used.  As such it is
     * preferable to throw an error rather than try to recover or, even worse,
     * fail silently.</blockquote>
     * <p>
     * <p> The iterator returned by this method does not support removal.
     * Invoking its {@link java.util.Iterator#remove() remove} method will
     * cause an {@link UnsupportedOperationException} to be thrown.
     *
     * @return An iterator that lazily loads providers for this loader's
     * valueFactoryClass
     * @implNote When adding providers to the cache, the {@link #iterator
     * Iterator} processes resources in the order that the {@link
     * java.lang.ClassLoader#getResources(java.lang.String)
     * ClassLoader.getResources(String)} method finds the valueFactoryClass configuration
     * files.
     */
    public Iterator<Function<String, V1>> iterator() {
        return new Iterator<Function<String, V1>>() {

            final Iterator<Map.Entry<String, Function<String, V1>>> knownProviders = providers.entrySet().iterator();

            public boolean hasNext() {
                if (knownProviders.hasNext()) {
                    return true;
                }
                return lookupIterator.hasNext();
            }

            public Function<String, V1> next() {
                if (knownProviders.hasNext()) {
                    return knownProviders.next().getValue();
                }
                return (Function<String, V1>) lookupIterator.next();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

        };
    }

    /**
     * Creates a new valueFactoryClass loader for the given valueFactoryClass type and class
     * loader.
     *
     * @param <V2>         the class of the valueFactoryClass type
     * @param valueFactory The interface or abstract class representing the valueFactoryClass
     * @param loader       The class loader to be used to load provider-configuration files
     *                     and provider classes, or <tt>null</tt> if the system class
     *                     loader (or, failing that, the bootstrap class loader) is to be
     *                     used
     * @return A new valueFactoryClass loader
     */
    public static <V2> ValueTypeLoader<V2> load(Class<V2> valueFactory,
                                                ClassLoader loader) {
        return new ValueTypeLoader<>(valueFactory, loader);
    }

    /**
     * Creates a new valueFactoryClass loader for the given valueFactoryClass type, using the
     * current thread's {@linkplain java.lang.Thread#getContextClassLoader
     * context class loader}.
     * <p>
     * <p> An invocation of this convenience method of the form
     * <p>
     * <blockquote><pre>
     * ServiceLoader.load(<i>valueFactoryClass</i>)</pre></blockquote>
     * <p>
     * is equivalent to
     * <p>
     * <blockquote><pre>
     * ServiceLoader.load(<i>valueFactoryClass</i>,
     *                    Thread.currentThread().getContextClassLoader())</pre></blockquote>
     *
     * @param <V2>         the class of the valueFactoryClass type
     * @param valueFactory The interface or abstract class representing the valueFactoryClass
     * @return A new valueFactoryClass loader
     */
    public static <V2> ValueTypeLoader<V2> load(Class<V2> valueFactory) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return ValueTypeLoader.load(valueFactory, cl);
    }

    /**
     * Returns a string describing this valueFactoryClass.
     *
     * @return A descriptive string
     */
    public String toString() {
        return getClass().getName() + "[" + valueFactoryClass.getName() + "]";
    }

}
