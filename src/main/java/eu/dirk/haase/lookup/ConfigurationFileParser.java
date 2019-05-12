package eu.dirk.haase.lookup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.function.Function;

class ConfigurationFileParser<V1> {

    // Cached providers, in instantiation order
    private final LinkedHashMap<String, Function<String, V1>> providers;

    ConfigurationFileParser(final LinkedHashMap<String, Function<String, V1>> providers) {
        this.providers = providers;
    }

    // Parse a single line from the given configuration file, adding the name
    // on the line to the names list.
    //
    private int parseLine(Class<?> valueFactory, URL u, BufferedReader r, int lc,
                          List<String> names)
            throws IOException, ServiceConfigurationError {
        String ln = r.readLine();
        if (ln == null) {
            return -1;
        }
        int ci = ln.indexOf('#');
        if (ci >= 0) ln = ln.substring(0, ci);
        ln = ln.trim();
        int n = ln.length();
        if (n != 0) {
            if ((ln.indexOf(' ') >= 0) || (ln.indexOf('\t') >= 0))
                throw ValueTypeLoaderError.fail(valueFactory, u, lc, "Illegal configuration-file syntax");
            int cp = ln.codePointAt(0);
            if (!Character.isJavaIdentifierStart(cp))
                throw ValueTypeLoaderError.fail(valueFactory, u, lc, "Illegal provider-class name: " + ln);
            for (int i = Character.charCount(cp); i < n; i += Character.charCount(cp)) {
                cp = ln.codePointAt(i);
                if (!Character.isJavaIdentifierPart(cp) && (cp != '.'))
                    throw ValueTypeLoaderError.fail(valueFactory, u, lc, "Illegal provider-class name: " + ln);
            }
            if (!providers.containsKey(ln) && !names.contains(ln))
                names.add(ln);
        }
        return lc + 1;
    }

    // Parse the content of the given URL as a provider-configuration file.
    //
    // @param  valueFactoryClass
    //         The valueFactoryClass type for which providers are being sought;
    //         used to construct error detail strings
    //
    // @param  u
    //         The URL naming the configuration file to be parsed
    //
    // @return A (possibly empty) iterator that will yield the provider-class
    //         names in the given configuration file that are not yet members
    //         of the returned set
    //
    // @throws ServiceConfigurationError
    //         If an I/O error occurs while reading from the given URL, or
    //         if a configuration-file format error is detected
    //
    Iterator<String> parse(Class<?> valueFactory, URL u)
            throws ServiceConfigurationError {
        ArrayList<String> names = new ArrayList<>();
        try (InputStream in = u.openStream();
             BufferedReader r = new BufferedReader(new InputStreamReader(in, "utf-8"));) {
            int lc = 1;
            while ((lc = parseLine(valueFactory, u, r, lc, names)) >= 0) ;
        } catch (IOException x) {
            throw ValueTypeLoaderError.fail(valueFactory, "Error reading configuration file", x);
        }
        return names.iterator();
    }

}
