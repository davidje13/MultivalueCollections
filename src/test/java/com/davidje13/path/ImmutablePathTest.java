package com.davidje13.path;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.davidje13.matchers.RunnableThrowsMatcher.throwsException;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNot.not;

@RunWith(Parameterized.class)
@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
public class ImmutablePathTest {
	@Test
	public void getHead_returnsFirstNode() {
		assertThat(path1.getHead(), equalTo("node 1"));
		assertThat(path2.getHead(), equalTo("node 1"));
		assertThat(path3.getHead(), equalTo("node 1"));
	}

	@Test
	public void getTail_returnsLastNode() {
		assertThat(path1.getTail(), equalTo("node 1"));
		assertThat(path2.getTail(), equalTo("node 2"));
		assertThat(path3.getTail(), equalTo("node 3"));
	}

	@Test
	public void getNodesFromHead_returnsNodesOrderedFromHead() {
		assertThat(path1.getNodesFromHead(), equalTo(asList(
				"node 1"
		)));
		assertThat(path2.getNodesFromHead(), equalTo(asList(
				"node 1",
				"node 2"
		)));
		assertThat(path3.getNodesFromHead(), equalTo(asList(
				"node 1",
				"node 2",
				"node 3"
		)));
	}

	@Test
	public void getNodesFromTail_returnsNodesOrderedFromTail() {
		assertThat(path1.getNodesFromTail(), equalTo(asList(
				"node 1"
		)));
		assertThat(path2.getNodesFromTail(), equalTo(asList(
				"node 2",
				"node 1"
		)));
		assertThat(path3.getNodesFromTail(), equalTo(asList(
				"node 3",
				"node 2",
				"node 1"
		)));
	}

	@Test
	public void getEdgesFromHead_returnsEdgesOrderedFromHead() {
		assertThat(path1.getEdgesFromHead(), equalTo(asList()));
		assertThat(path2.getEdgesFromHead(), equalTo(asList(1)));
		assertThat(path3.getEdgesFromHead(), equalTo(asList(1, 2)));
	}

	@Test
	public void getEdgesFromTail_returnsEdgesOrderedFromTail() {
		assertThat(path1.getEdgesFromTail(), equalTo(asList()));
		assertThat(path2.getEdgesFromTail(), equalTo(asList(1)));
		assertThat(path3.getEdgesFromTail(), equalTo(asList(2, 1)));
	}

	@Test
	public void countNodes_returnsNumberOfNodes() {
		assertThat(path1.countNodes(), equalTo(1));
		assertThat(path2.countNodes(), equalTo(2));
		assertThat(path3.countNodes(), equalTo(3));
	}

	@Test
	public void countEdges_returnsNumberOfEdges() {
		assertThat(path1.countEdges(), equalTo(0));
		assertThat(path2.countEdges(), equalTo(1));
		assertThat(path3.countEdges(), equalTo(2));
	}

	@Test
	public void visitFromHead_iteratesThroughNodesAndEdgesFromHead() {
		List<Object> visited1 = new ArrayList<>();
		path1.visitFromHead(visited1::add, visited1::add);
		assertThat(visited1, equalTo(asList("node 1")));

		List<Object> visited2 = new ArrayList<>();
		path2.visitFromHead(visited2::add, visited2::add);
		assertThat(visited2, equalTo(asList("node 1", 1, "node 2")));

		List<Object> visited3 = new ArrayList<>();
		path3.visitFromHead(visited3::add, visited3::add);
		assertThat(visited3, equalTo(asList("node 1", 1, "node 2", 2, "node 3")));
	}

	@Test
	public void visitFromTail_iteratesThroughNodesAndEdgesFromTail() {
		List<Object> visited1 = new ArrayList<>();
		path1.visitFromTail(visited1::add, visited1::add);
		assertThat(visited1, equalTo(asList("node 1")));

		List<Object> visited2 = new ArrayList<>();
		path2.visitFromTail(visited2::add, visited2::add);
		assertThat(visited2, equalTo(asList("node 2", 1, "node 1")));

		List<Object> visited3 = new ArrayList<>();
		path3.visitFromTail(visited3::add, visited3::add);
		assertThat(visited3, equalTo(asList("node 3", 2, "node 2", 1, "node 1")));
	}

	@Test
	public void visitGroupedFromHead_combinesNodesWhileTraversing() {
		ImmutablePath<String, Integer> path = testPath("a1", 1, "a2", 2, "b1", 3, "c1", 4, "c2");

		List<Object> visited = new ArrayList<>();
		path.visitGroupedFromHead(
				(node1, node2) -> node1.substring(0, 1).equals(node2.substring(0, 1)),
				(group) -> visited.add(group.toString()),
				visited::add
		);
		assertThat(visited, equalTo(asList("a1, 1, a2", 2, "b1", 3, "c1, 4, c2")));
	}

	@Test
	public void visitGroupedFromTail_combinesNodesWhileTraversing() {
		ImmutablePath<String, Integer> path = testPath("a1", 1, "a2", 2, "b1", 3, "c1", 4, "c2");

		List<Object> visited = new ArrayList<>();
		path.visitGroupedFromTail(
				(node1, node2) -> node1.substring(0, 1).equals(node2.substring(0, 1)),
				(group) -> visited.add(group.toString()),
				visited::add
		);
		assertThat(visited, equalTo(asList("c1, 4, c2", 3, "b1", 2, "a1, 1, a2")));
	}

	@Test
	public void visitPairsFromHead_iteratesThroughEdgeNodePairsFromHead_excludingHeadNode() {
		List<Object> visitedEdges1 = new ArrayList<>();
		List<Object> visitedNodes1 = new ArrayList<>();
		path1.visitPairsFromHead((e, n) -> {
			visitedEdges1.add(e);
			visitedNodes1.add(n);
		});
		assertThat(visitedEdges1, equalTo(asList()));
		assertThat(visitedNodes1, equalTo(asList()));

		List<Object> visitedEdges2 = new ArrayList<>();
		List<Object> visitedNodes2 = new ArrayList<>();
		path2.visitPairsFromHead((e, n) -> {
			visitedEdges2.add(e);
			visitedNodes2.add(n);
		});
		assertThat(visitedEdges2, equalTo(asList(1)));
		assertThat(visitedNodes2, equalTo(asList("node 2")));

		List<Object> visitedEdges3 = new ArrayList<>();
		List<Object> visitedNodes3 = new ArrayList<>();
		path3.visitPairsFromHead((e, n) -> {
			visitedEdges3.add(e);
			visitedNodes3.add(n);
		});
		assertThat(visitedEdges3, equalTo(asList(1, 2)));
		assertThat(visitedNodes3, equalTo(asList("node 2", "node 3")));
	}

	@Test
	public void visitPairsFromTail_iteratesThroughEdgeNodePairsFromTail_excludingTailNode() {
		List<Object> visitedEdges1 = new ArrayList<>();
		List<Object> visitedNodes1 = new ArrayList<>();
		path1.visitPairsFromTail((e, n) -> {
			visitedEdges1.add(e);
			visitedNodes1.add(n);
		});
		assertThat(visitedEdges1, equalTo(asList()));
		assertThat(visitedNodes1, equalTo(asList()));

		List<Object> visitedEdges2 = new ArrayList<>();
		List<Object> visitedNodes2 = new ArrayList<>();
		path2.visitPairsFromTail((e, n) -> {
			visitedEdges2.add(e);
			visitedNodes2.add(n);
		});
		assertThat(visitedEdges2, equalTo(asList(1)));
		assertThat(visitedNodes2, equalTo(asList("node 1")));

		List<Object> visitedEdges3 = new ArrayList<>();
		List<Object> visitedNodes3 = new ArrayList<>();
		path3.visitPairsFromTail((e, n) -> {
			visitedEdges3.add(e);
			visitedNodes3.add(n);
		});
		assertThat(visitedEdges3, equalTo(asList(2, 1)));
		assertThat(visitedNodes3, equalTo(asList("node 2", "node 1")));
	}

	@Test
	public void subPath_createsSubPathBetweenGivenIndices_inclusive() {
		assertThat(path1.subPath(0, 0), equalTo(path1));

		assertThat(path2.subPath(0, 1), equalTo(path2));
		assertThat(path2.subPath(0, 0), equalTo(testPath("node 1")));
		assertThat(path2.subPath(1, 1), equalTo(testPath("node 2")));

		assertThat(path3.subPath(0, 2), equalTo(path3));
		assertThat(path3.subPath(0, 1), equalTo(testPath("node 1", 1, "node 2")));
		assertThat(path3.subPath(1, 2), equalTo(testPath("node 2", 2, "node 3")));
		assertThat(path3.subPath(0, 0), equalTo(testPath("node 1")));
		assertThat(path3.subPath(1, 1), equalTo(testPath("node 2")));
		assertThat(path3.subPath(2, 2), equalTo(testPath("node 3")));
	}

	@Test
	public void subPath_throwsIfRangeIsBeyondLimitsOrReversed() {
		assertThat(() -> path1.subPath(-1, 0), throwsException(instanceOf(IndexOutOfBoundsException.class)));
		assertThat(() -> path1.subPath(0, 1), throwsException(instanceOf(IndexOutOfBoundsException.class)));
		assertThat(() -> path1.subPath(1, 1), throwsException(instanceOf(IndexOutOfBoundsException.class)));

		assertThat(() -> path2.subPath(0, 2), throwsException(instanceOf(IndexOutOfBoundsException.class)));
		assertThat(() -> path2.subPath(2, 2), throwsException(instanceOf(IndexOutOfBoundsException.class)));
		assertThat(() -> path2.subPath(1, 0), throwsException(instanceOf(IndexOutOfBoundsException.class)));
	}

	@Test
	public void containsSubPath_returnsTrueWhenSearchingForSelf() {
		assertThat(path1.containsSubPath(path1), equalTo(true));
		assertThat(path2.containsSubPath(path2), equalTo(true));
		assertThat(path3.containsSubPath(path3), equalTo(true));
	}

	@Test
	public void startsWith_returnsTrueWhenSearchingForSelf() {
		assertThat(path1.startsWith(path1), equalTo(true));
		assertThat(path2.startsWith(path2), equalTo(true));
		assertThat(path3.startsWith(path3), equalTo(true));
	}

	@Test
	public void endsWith_returnsTrueWhenSearchingForSelf() {
		assertThat(path1.endsWith(path1), equalTo(true));
		assertThat(path2.endsWith(path2), equalTo(true));
		assertThat(path3.endsWith(path3), equalTo(true));
	}

	@Test
	public void firstIndexOfSubPath_returnsIndexOfIdentifiedSubPath() {
		assertThat(path1.firstIndexOfSubPath(path1), equalTo(0));

		assertThat(path2.firstIndexOfSubPath(path2), equalTo(0));
		assertThat(path2.firstIndexOfSubPath(testPath("node 1")), equalTo(0));
		assertThat(path2.firstIndexOfSubPath(testPath("node 2")), equalTo(1));

		assertThat(path3.firstIndexOfSubPath(path3), equalTo(0));
		assertThat(path3.firstIndexOfSubPath(testPath("node 1")), equalTo(0));
		assertThat(path3.firstIndexOfSubPath(testPath("node 2")), equalTo(1));
		assertThat(path3.firstIndexOfSubPath(testPath("node 3")), equalTo(2));
		assertThat(path3.firstIndexOfSubPath(testPath("node 1", 1, "node 2")), equalTo(0));
		assertThat(path3.firstIndexOfSubPath(testPath("node 2", 2, "node 3")), equalTo(1));
	}

	@Test
	public void lastIndexOfSubPath_returnsIndexOfIdentifiedSubPath() {
		assertThat(path1.lastIndexOfSubPath(path1), equalTo(0));

		assertThat(path2.lastIndexOfSubPath(path2), equalTo(0));
		assertThat(path2.lastIndexOfSubPath(testPath("node 1")), equalTo(0));
		assertThat(path2.lastIndexOfSubPath(testPath("node 2")), equalTo(1));

		assertThat(path3.lastIndexOfSubPath(path3), equalTo(0));
		assertThat(path3.lastIndexOfSubPath(testPath("node 1")), equalTo(0));
		assertThat(path3.lastIndexOfSubPath(testPath("node 2")), equalTo(1));
		assertThat(path3.lastIndexOfSubPath(testPath("node 3")), equalTo(2));
		assertThat(path3.lastIndexOfSubPath(testPath("node 1", 1, "node 2")), equalTo(0));
		assertThat(path3.lastIndexOfSubPath(testPath("node 2", 2, "node 3")), equalTo(1));
	}

	@Test
	public void containsSubPath_returnsTrueIfFound() {
		assertThat(path2.containsSubPath(testPath("node 1")), equalTo(true));
		assertThat(path2.containsSubPath(testPath("node 2")), equalTo(true));

		assertThat(path3.containsSubPath(testPath("node 1")), equalTo(true));
		assertThat(path3.containsSubPath(testPath("node 2")), equalTo(true));
		assertThat(path3.containsSubPath(testPath("node 3")), equalTo(true));
		assertThat(path3.containsSubPath(testPath("node 1", 1, "node 2")), equalTo(true));
		assertThat(path3.containsSubPath(testPath("node 2", 2, "node 3")), equalTo(true));
	}

	@Test
	public void startsWith_returnsTrueIfFoundAtStart() {
		assertThat(path2.startsWith(testPath("node 1")), equalTo(true));

		assertThat(path3.startsWith(testPath("node 1")), equalTo(true));
		assertThat(path3.startsWith(testPath("node 1", 1, "node 2")), equalTo(true));
	}

	@Test
	public void endsWith_returnsTrueIfFoundAtEnd() {
		assertThat(path2.endsWith(testPath("node 2")), equalTo(true));

		assertThat(path3.endsWith(testPath("node 3")), equalTo(true));
		assertThat(path3.endsWith(testPath("node 2", 2, "node 3")), equalTo(true));
	}

	@Test
	public void firstIndexOfSubPath_returnsOccurrenceClosestToHead() {
		ImmutablePath<String, Integer> path = testPath("a", 1, "a");
		assertThat(path.firstIndexOfSubPath(testPath("a")), equalTo(0));
	}

	@Test
	public void lastIndexOfSubPath_returnsOccurrenceClosestToTail() {
		ImmutablePath<String, Integer> path = testPath("a", 1, "a");
		assertThat(path.lastIndexOfSubPath(testPath("a")), equalTo(1));
	}

	@Test
	public void firstIndexOfSubPath_returnsMinusOneIfNotFound() {
		assertThat(path1.firstIndexOfSubPath(testPath("nope")), equalTo(-1));
		assertThat(path2.firstIndexOfSubPath(testPath("nope")), equalTo(-1));
		assertThat(path3.firstIndexOfSubPath(testPath("nope")), equalTo(-1));
		assertThat(path3.firstIndexOfSubPath(testPath("node 1", 2, "node 2")), equalTo(-1));
	}

	@Test
	public void lastIndexOfSubPath_returnsMinusOneIfNotFound() {
		assertThat(path1.lastIndexOfSubPath(testPath("nope")), equalTo(-1));
		assertThat(path2.lastIndexOfSubPath(testPath("nope")), equalTo(-1));
		assertThat(path3.lastIndexOfSubPath(testPath("nope")), equalTo(-1));
		assertThat(path3.lastIndexOfSubPath(testPath("node 1", 2, "node 2")), equalTo(-1));
	}

	@Test
	public void containsSubPath_returnsFalseIfNotFound() {
		assertThat(path1.containsSubPath(testPath("nope")), equalTo(false));
		assertThat(path2.containsSubPath(testPath("nope")), equalTo(false));
		assertThat(path3.containsSubPath(testPath("nope")), equalTo(false));
		assertThat(path3.containsSubPath(testPath("node 1", 2, "node 2")), equalTo(false));
	}

	@Test
	public void startsWith_returnsFalseIfNotFound() {
		assertThat(path1.startsWith(testPath("nope")), equalTo(false));
		assertThat(path2.startsWith(testPath("nope")), equalTo(false));
		assertThat(path3.startsWith(testPath("nope")), equalTo(false));
		assertThat(path3.startsWith(testPath("node 1", 2, "node 2")), equalTo(false));
	}

	@Test
	public void endsWith_returnsFalseIfNotFound() {
		assertThat(path1.endsWith(testPath("nope")), equalTo(false));
		assertThat(path2.endsWith(testPath("nope")), equalTo(false));
		assertThat(path3.endsWith(testPath("nope")), equalTo(false));
		assertThat(path3.endsWith(testPath("node 1", 2, "node 2")), equalTo(false));
	}

	@Test
	public void startsWith_returnsFalseIfNotFoundAtStart() {
		assertThat(path2.startsWith(testPath("node 2")), equalTo(false));
		assertThat(path3.startsWith(testPath("node 2")), equalTo(false));
		assertThat(path3.startsWith(testPath("node 3")), equalTo(false));
		assertThat(path3.startsWith(testPath("node 2", 2, "node 3")), equalTo(false));
	}

	@Test
	public void endsWith_returnsFalseIfNotFoundAtEnd() {
		assertThat(path2.endsWith(testPath("node 1")), equalTo(false));
		assertThat(path3.endsWith(testPath("node 1")), equalTo(false));
		assertThat(path3.endsWith(testPath("node 2")), equalTo(false));
		assertThat(path3.endsWith(testPath("node 1", 1, "node 2")), equalTo(false));
	}

	@Test
	public void firstIndexOfSubPath_returnsMinusOneIfTooLong() {
		assertThat(path1.firstIndexOfSubPath(path2), equalTo(-1));
		assertThat(path2.firstIndexOfSubPath(path3), equalTo(-1));
	}

	@Test
	public void lastIndexOfSubPath_returnsMinusOneIfTooLong() {
		assertThat(path1.lastIndexOfSubPath(path2), equalTo(-1));
		assertThat(path2.lastIndexOfSubPath(path3), equalTo(-1));
	}

	@Test
	public void containsSubPath_returnsFalseIfTooLong() {
		assertThat(path1.containsSubPath(path2), equalTo(false));
		assertThat(path2.containsSubPath(path3), equalTo(false));
	}

	@Test
	public void startsWith_returnsFalseIfTooLong() {
		assertThat(path1.startsWith(path2), equalTo(false));
		assertThat(path2.startsWith(path3), equalTo(false));
	}

	@Test
	public void endsWith_returnsFalseIfTooLong() {
		assertThat(path1.endsWith(path2), equalTo(false));
		assertThat(path2.endsWith(path3), equalTo(false));
	}

	@Test
	public void builder_getHead_returnsFirstNode() {
		assertThat(path2.toBuilder().getHead(), equalTo("node 1"));
		assertThat(path2.toBuilder().pushFront("new", 1).getHead(), equalTo("new"));
	}

	@Test
	public void builder_getTail_returnsLastNode() {
		assertThat(path2.toBuilder().getTail(), equalTo("node 2"));
		assertThat(path2.toBuilder().pushBack(1, "new").getTail(), equalTo("new"));
	}

	@Test
	public void builder_pushBack_addsOneNodeToEndOfPath() {
		ImmutablePath<String, Integer> path = path2.toBuilder().pushBack(7, "abc").build();
		assertThat(path, equalTo(testPath("node 1", 1, "node 2", 7, "abc")));
	}

	@Test
	public void builder_pushFront_addsOneNodeToStartOfPath() {
		ImmutablePath<String, Integer> path = path2.toBuilder().pushFront("abc", 7).build();
		assertThat(path, equalTo(testPath("abc", 7, "node 1", 1, "node 2")));
	}

	@Test
	public void builder_pushBack_addsEntirePathToEndOfPath() {
		ImmutablePath<String, Integer> path = path2.toBuilder().pushBack(7, path3).build();
		assertThat(path, equalTo(testPath("node 1", 1, "node 2", 7, "node 1", 1, "node 2", 2, "node 3")));
	}

	@Test
	public void builder_pushFront_addsEntirePathToStartOfPath() {
		ImmutablePath<String, Integer> path = path2.toBuilder().pushFront(path3, 7).build();
		assertThat(path, equalTo(testPath("node 1", 1, "node 2", 2, "node 3", 7, "node 1", 1, "node 2")));
	}

	@Test
	@SuppressWarnings("EqualsWithItself")
	public void equals_checksIdentity() {
		assertThat(path1.equals(path1), equalTo(true));
		assertThat(path1.equals(path2), equalTo(false));
		assertThat(path2.equals(path2), equalTo(true));
	}

	@Test
	@SuppressWarnings("EqualsBetweenInconvertibleTypes")
	public void equals_checksType() {
		assertThat(path1.equals("hello"), equalTo(false));
	}

	@Test
	public void equals_checksEquivalenceOnNodes() {
		assertThat(path1.equals(testPath("node 1")), equalTo(true));
		assertThat(path1.equals(testPath("different")), equalTo(false));
	}

	@Test
	public void equals_checksEquivalenceOnLinks() {
		assertThat(path2.equals(testPath("node 1", 1, "node 2")), equalTo(true));
		assertThat(path2.equals(testPath("node 1", 2, "node 2")), equalTo(false));
	}

	@Test
	public void hashCode_isConsistent() {
		assertThat(path1.hashCode(), equalTo(path1.hashCode()));
		assertThat(path2.hashCode(), equalTo(path2.hashCode()));
	}

	@Test
	public void hashCode_isUsuallyDifferentForDifferentPaths() {
		assertThat(path1.hashCode(), not(equalTo(path2.hashCode())));
		assertThat(path1.hashCode(), not(equalTo(testPath("different").hashCode())));
		assertThat(path2.hashCode(), not(equalTo(testPath("node 1", 2, "node 2").hashCode())));
	}

	@Test
	public void hashCode_isSameForEqualValues() {
		assertThat(path1.hashCode(), equalTo(testPath("node 1").hashCode()));
		assertThat(path2.hashCode(), equalTo(testPath("node 1", 1, "node 2").hashCode()));
	}


	private final ImmutablePath<String, Integer> basePath;
	private final ImmutablePath<String, Integer> path1;
	private final ImmutablePath<String, Integer> path2;
	private final ImmutablePath<String, Integer> path3;

	public ImmutablePathTest(
			Class<? extends ImmutablePath<String, Integer>> implementationClass
	) throws ReflectiveOperationException {
		this.basePath = implementationClass
				.getConstructor(Object.class)
				.newInstance(new Object[] { null });

		this.path1 = testPath("node 1");
		this.path2 = testPath("node 1", 1, "node 2");
		this.path3 = testPath("node 1", 1, "node 2", 2, "node 3");
	}

	private ImmutablePath<String, Integer> testPath(Object... parts) {
		PathBuilder<String, Integer, ?> builder =
				basePath.builderFromNode((String) parts[0]);

		for (int i = 1; i < parts.length; i += 2) {
			builder.pushBack((Integer) parts[i], (String) parts[i + 1]);
		}

		return builder.build();
	}

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Class<? extends ImmutablePath>> implementationsToTest() {
		return asList(LinkedPath.class, ArrayPath.class);
	}
}
