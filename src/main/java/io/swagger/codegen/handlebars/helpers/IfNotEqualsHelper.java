package io.swagger.codegen.handlebars.helpers;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;

import java.io.IOException;
import java.util.Objects;

public class IfNotEqualsHelper implements Helper<Object> {

    public static final String NAME = "ifNotEquals";

    @Override
    public Object apply(final Object context, final Options options)
            throws IOException {
        return notEquals((String) context, options);
    }

    /**
     * Checks if values are not equal.
     * @param obj1
     * @param options
     * @return
     * @throws IOException
     */
    public CharSequence notEquals(final Object obj1, final Options options) throws IOException {
        Object obj2 = options.param(0);
        return !Objects.equals(obj1, obj2) ? options.fn() : options.inverse();
    }
}
