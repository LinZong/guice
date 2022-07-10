package com.google.inject.internal;

import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.spi.Dependency;

import java.util.List;

/**
 * This binder performs just like its super class but only providing dependencies in {@literal List<T>} with order preserves (if exists).
 * @param <T>
 */
public class RealOrderedListMultibinder<T> extends RealMultibinder<T> {
    RealOrderedListMultibinder(Binder binder, Key<T> key) {
        super(binder, key);
    }

    @Override
    public void configure(Binder binder) {
        super.configure(binder);
        // Add ordered list provider.
        RealMultibinderOrderedListProvider<T> orderedListProvider = new RealMultibinderOrderedListProvider<>(bindingSelection);
        binder.bind(bindingSelection.getListKey()).toProvider(orderedListProvider);
        binder.bind(bindingSelection.getListOfExtendsKey()).toProvider(orderedListProvider);
    }


    /**
     * Provider instance implementation that provides the actual list of values. This is parameterized
     * so it can be used to supply a List<T> and List<? extends T>, the latter being useful for Kotlin
     * support.
     */
    private static final class RealMultibinderOrderedListProvider<T> extends BaseFactory<T, List<T>> {
        List<Binding<T>> bindings;
        SingleParameterInjector<T>[] injectors;
        boolean permitDuplicates;

        RealMultibinderOrderedListProvider(BindingSelection<T> bindingSelection) {
            // Note: method reference doesn't work for the 2nd arg for some reason when compiling on java8
            super(bindingSelection, bs -> bs.getDependencies());
        }

        @Override
        protected void doInitialize() {
            bindings = bindingSelection.getBindings();
            injectors = bindingSelection.getParameterInjectors();
            permitDuplicates = bindingSelection.permitsDuplicates();
        }

        @Override
        protected ImmutableList<T> doProvision(InternalContext context, Dependency<?> dependency)
                throws InternalProvisionException {
            // injectors from bindingSelection are preserving order declared by @Ordered annotation.
            SingleParameterInjector<T>[] localInjectors = injectors;
            if (localInjectors == null) {
                // if localInjectors == null, then we have no bindings so return the empty list.
                return ImmutableList.of();
            }
            // Ideally we would just add to an ImmutableList.Builder, but if we did that and there were
            // duplicates we wouldn't be able to tell which one was the duplicate.  So to manage this we
            // first put everything into an array and then construct the list.  This way if something gets
            // dropped we can figure out what it is.
            @SuppressWarnings("unchecked")
            T[] values = (T[]) new Object[localInjectors.length];
            for (int i = 0; i < localInjectors.length; i++) {
                SingleParameterInjector<T> parameterInjector = localInjectors[i];
                T newValue = parameterInjector.inject(context);
                if (newValue == null) {
                    throw newNullEntryException(i);
                }
                values[i] = newValue;
            }
            return ImmutableList.copyOf(values);
        }

        private InternalProvisionException newNullEntryException(int i) {
            return InternalProvisionException.create(
                    ErrorId.NULL_ELEMENT_IN_SET,
                    "List injection failed due to null element bound at: %s",
                    bindings.get(i).getSource());
        }
    }
}
