package io.github.lmhjava.ui.util;


import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

import java.util.Set;

public final class EnhancedBindings {

    /**
     * Sync content mono-directionally from an {@code observableList} to a {@code set}.
     *
     * @param set target set
     * @param observableList origin observable list
     * @param <T> type parameter of containers
     */
    public static <T> void bindContent(Set<T> set, ObservableList<T> observableList) {
        observableList.addListener((ListChangeListener<? super T>) c -> {
            if (c.wasAdded()) {
                set.addAll(c.getAddedSubList());
            } else if (c.wasRemoved()) {
                c.getRemoved().forEach(set::remove);
            }
        });
    }

    /**
     * Sync content mono-directionally from an {@code observableSet} to an {@code observableList}.
     *
     * @param observableList target observable list
     * @param observableSet original observable set
     * @param <T> type parameter of containers
     */
    public static <T> void bindContent(ObservableList<T> observableList, ObservableSet<T> observableSet) {
        observableSet.addListener((SetChangeListener<? super T>) c -> {
            if (c.wasAdded()) {
                observableList.add(c.getElementAdded());
            } else if (c.wasRemoved()) {
                observableList.remove(c.getElementRemoved());
            }
        });
    }
}
