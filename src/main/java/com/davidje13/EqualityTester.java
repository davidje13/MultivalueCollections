package com.davidje13;

import java.util.Comparator;
import java.util.function.BiPredicate;

public interface EqualityTester<T> extends BiPredicate<T, T> {
	static <T> EqualityTester<T> fromComparator(Comparator<T> comparator) {
		return (a, b) -> (comparator.compare(a, b) == 0);
	}
}
