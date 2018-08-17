package com.davidje13.path;

import com.davidje13.path.LinkedPath.LinkedPathBuilder;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
public class PathSquasherTest {
	private final PathSquasher pathSquasher = new PathSquasher();

	@Test
	public void squashNodes_combinesChosenNodes() {
		ImmutablePath<String, Integer> path = testPath("a1", 1, "a2", 2, "b");

		ImmutablePath<List<Object>, Long> squashedPath = pathSquasher.squashNodes(
				path,
				(node1, node2) -> node1.substring(0, 1).equals(node2.substring(0, 1)),
				this::readAll,
				(edge) -> (long) edge
		);

		assertThat(squashedPath, equalTo(
				testPathSquashed(asList("a1", 1, "a2"), 2L, asList("b"))
		));
	}

	@Test
	public void expandNodes_convertsNodesIntoLargerPaths() {
		ImmutablePath<List<Object>, Long> squashedPath = testPathSquashed(
				asList("a", 10, "b"),
				1L,
				asList("c", 20, "d", 30, "e"),
				2L,
				asList("f")
		);

		ImmutablePath<String, Integer> path = pathSquasher.expandNodes(
				squashedPath,
				parts -> testPath(parts.toArray()),
				(edge) -> (int) (long) edge
		);

		assertThat(path, equalTo(
				testPath("a", 10, "b", 1, "c", 20, "d", 30, "e", 2, "f")
		));
	}

	private List<Object> readAll(ImmutablePath<?, ?> path) {
		List<Object> all = new ArrayList<>();
		path.visitFromHead(all::add, all::add);
		return all;
	}

	private ImmutablePath<String, Integer> testPath(Object... parts) {
		LinkedPathBuilder<String, Integer> builder =
				LinkedPath.builder((String) parts[0]);

		for (int i = 1; i < parts.length; i += 2) {
			builder.pushBack((Integer) parts[i], (String) parts[i + 1]);
		}

		return builder.build();
	}

	@SuppressWarnings("unchecked")
	private ImmutablePath<List<Object>, Long> testPathSquashed(Object... parts) {
		LinkedPathBuilder<List<Object>, Long> builder =
				LinkedPath.builder((List<Object>) parts[0]);

		for (int i = 1; i < parts.length; i += 2) {
			builder.pushBack((Long) parts[i], (List<Object>) parts[i + 1]);
		}

		return builder.build();
	}
}
