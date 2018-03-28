package io.swagger.codegen.handlebars.helpers;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import java.io.IOException;
import java.util.Objects;

public class IfEqualsHelper implements Helper<Object> {

    public static final String NAME = "ifEquals";

    @Override
    public Object apply(final Object context, final Options options)
            throws IOException {
        return equals((String) context, options);
    }

    /**
     * Checks if values are equal.
     * @param obj1
     * @param options
     * @return
     * @throws IOException
     */
    public CharSequence equals(final Object obj1, final Options options) throws IOException {
        Object obj2 = options.param(0);
        return Objects.equals(obj1, obj2) ? options.fn() : options.inverse();
    }
}
