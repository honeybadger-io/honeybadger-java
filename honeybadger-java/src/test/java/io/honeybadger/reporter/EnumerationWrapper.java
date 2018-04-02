package io.honeybadger.reporter;

import com.google.common.collect.ImmutableList;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * Test utility class for turning N number of items into an {@link Enumeration}.
 */
@SuppressWarnings("JdkObsolete")
class EnumerationWrapper<E> implements Enumeration<E> {
    final Iterator<E> itr;

    EnumerationWrapper(Iterator<E> backing) {
        this.itr = backing;
    }

    EnumerationWrapper(Iterable<E> backing) {
        this.itr = backing.iterator();
    }

    @SuppressWarnings("unchecked")
    public static <E> EnumerationWrapper<E> of(E... items) {
        return new EnumerationWrapper<>(ImmutableList.copyOf(items).iterator());
    }

    @Override
    public boolean hasMoreElements() {
        return itr.hasNext();
    }

    @Override
    public E nextElement() {
        return itr.next();
    }
}
