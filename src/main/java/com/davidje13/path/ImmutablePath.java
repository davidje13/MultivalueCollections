package com.davidje13.path;

import com.davidje13.Box;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public interface ImmutablePath<NodeT, EdgeT> {
	/**
	 * Create a new builder from the given node. This method is provided for
	 * convenience; it does not use any properties of the current path except
	 * its type.
	 *
	 * @param node the node to seed the new builder with
	 * @return a builder containing the given node
	 */
	PathBuilder<NodeT, EdgeT, ? extends PathBuilder> builderFromNode(NodeT node);

	/**
	 * Create a new builder seeded from the current path.
	 *
	 * @return a builder containing the current path, ready to be modified
	 */
	default PathBuilder<NodeT, EdgeT, ? extends PathBuilder> toBuilder() {
		PathBuilder<NodeT, EdgeT, ? extends PathBuilder> builder = builderFromNode(getHead());
		visitPairsFromHead(builder::pushBack);
		return builder;
	}

	/**
	 * @return the first node in the path
	 */
	NodeT getHead();

	/**
	 * @return the last node in the path
	 */
	NodeT getTail();

	/**
	 * Visit nodes and edges in order from the head of the path. Skips the
	 * head node.
	 *
	 * @param visitor lambda to call with each node, edge pair
	 */
	void visitPairsFromHead(BiConsumer<? super EdgeT, ? super NodeT> visitor);

	/**
	 * Visit nodes and edges in order from the tail of the path. Skips the
	 * tail node.
	 *
	 * @param visitor lambda to call with each node, edge pair
	 */
	void visitPairsFromTail(BiConsumer<? super EdgeT, ? super NodeT> visitor);

	/**
	 * Visit all nodes and edges in order from the head of the path.
	 *
	 * The nodeVisitor will be called first, then the edgeVisitor, then node,
	 * etc. The last call will also be to the nodeVisitor.
	 *
	 * @param nodeVisitor lambda to call with each node
	 * @param edgeVisitor lambda to call with each edge
	 */
	default void visitFromHead(
			Consumer<? super NodeT> nodeVisitor,
			Consumer<? super EdgeT> edgeVisitor
	) {
		nodeVisitor.accept(getHead());
		visitPairsFromHead((edge, node) -> {
			edgeVisitor.accept(edge);
			nodeVisitor.accept(node);
		});
	}

	/**
	 * Visit all nodes and edges in order from the tail of the path.
	 *
	 * The nodeVisitor will be called first, then the edgeVisitor, then node,
	 * etc. The last call will also be to the nodeVisitor.
	 *
	 * @param nodeVisitor lambda to call with each node
	 * @param edgeVisitor lambda to call with each edge
	 */
	default void visitFromTail(
			Consumer<? super NodeT> nodeVisitor,
			Consumer<? super EdgeT> edgeVisitor
	) {
		nodeVisitor.accept(getTail());
		visitPairsFromTail((edge, node) -> {
			edgeVisitor.accept(edge);
			nodeVisitor.accept(node);
		});
	}

	default void visitGroupedFromHead(
			BiPredicate<NodeT, NodeT> nodeEqualityTester,
			Consumer<ImmutablePath<NodeT, EdgeT>> groupVisitor,
			Consumer<EdgeT> edgeVisitor
	) {
		PathBuilder<NodeT, EdgeT, ? extends PathBuilder> groupBuilder =
				builderFromNode(getHead());

		visitPairsFromHead((edge, node) -> {
			NodeT lastNode = groupBuilder.getTail();
			if (nodeEqualityTester.test(lastNode, node)) {
				groupBuilder.pushBack(edge, node);
			} else {
				groupVisitor.accept(groupBuilder.build());
				edgeVisitor.accept(edge);
				groupBuilder.reset(node);
			}
		});
		groupVisitor.accept(groupBuilder.build());
	}

	default void visitGroupedFromTail(
			BiPredicate<NodeT, NodeT> nodeEqualityTester,
			Consumer<ImmutablePath<NodeT, EdgeT>> groupVisitor,
			Consumer<EdgeT> edgeVisitor
	) {
		PathBuilder<NodeT, EdgeT, ? extends PathBuilder> groupBuilder =
				builderFromNode(getTail());

		visitPairsFromTail((edge, node) -> {
			NodeT lastNode = groupBuilder.getTail();
			if (nodeEqualityTester.test(lastNode, node)) {
				groupBuilder.pushFront(node, edge);
			} else {
				groupVisitor.accept(groupBuilder.build());
				edgeVisitor.accept(edge);
				groupBuilder.reset(node);
			}
		});
		groupVisitor.accept(groupBuilder.build());
	}

	/**
	 * Create a sub path from the start node index (inclusive) to the end node
	 * index (also inclusive).
	 *
	 * @param startNodeIndex the index of the first node to include in the
	 *                       sub path
	 * @param endNodeIndex the index of the last node to include in the sub path
	 * @return a new path containing the requested sub path
	 * @throws IndexOutOfBoundsException if the requested range is invalid
	 */
	default ImmutablePath<NodeT, EdgeT> subPath(
			int startNodeIndex,
			int endNodeIndex
	) {
		if (startNodeIndex < 0 || endNodeIndex < startNodeIndex) {
			throw new IndexOutOfBoundsException(
					"Invalid range: " + startNodeIndex + " -- " + endNodeIndex
			);
		}

		PathBuilder<NodeT, EdgeT, ? extends PathBuilder> builder = builderFromNode(getHead());
		Box<Integer> index = new Box<>(1);
		visitPairsFromHead((edge, node) -> {
			if (index.value == startNodeIndex) {
				builder.reset(node);
			} else if (index.value > startNodeIndex && index.value <= endNodeIndex) {
				builder.pushBack(edge, node);
			}
			++ index.value;
		});
		int size = index.value;

		if (endNodeIndex >= size) {
			throw new IndexOutOfBoundsException(
					"Invalid range: " + startNodeIndex + " -- " + endNodeIndex +
					" (nodes in path: " + size + ")"
			);
		}

		return builder.build();
	}

	/**
	 * @param subPath the path to look for
	 * @return the lowest index of the requested sub path, or -1 if not found
	 */
	default int firstIndexOfSubPath(ImmutablePath<NodeT, EdgeT> subPath) {
		int size = countNodes();
		int subSize = subPath.countNodes();
		for (int i = 0; i <= size - subSize; ++ i) {
			if (subPath(i, i + subSize - 1).equals(subPath)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * @param subPath the path to look for
	 * @return the highest index of the requested sub path, or -1 if not found
	 */
	default int lastIndexOfSubPath(ImmutablePath<NodeT, EdgeT> subPath) {
		int size = countNodes();
		int subSize = subPath.countNodes();
		for (int i = size - subSize; i >= 0; -- i) {
			if (subPath(i, i + subSize - 1).equals(subPath)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * @param subPath the path to look for
	 * @return true if the path contains the requested sub path, otherwise false
	 */
	default boolean containsSubPath(ImmutablePath<NodeT, EdgeT> subPath) {
		return firstIndexOfSubPath(subPath) != -1;
	}

	/**
	 * @param subPath the path to look for
	 * @return true if the path begins with the requested sub path, otherwise
	 * false
	 */
	default boolean startsWith(ImmutablePath<NodeT, EdgeT> subPath) {
		int size = countNodes();
		int subSize = subPath.countNodes();
		if (subSize > size) {
			return false;
		}
		return subPath(0, subSize - 1).equals(subPath);
	}

	/**
	 * @param subPath the path to look for
	 * @return true if the path ends with the requested sub path, otherwise
	 * false
	 */
	default boolean endsWith(ImmutablePath<NodeT, EdgeT> subPath) {
		int size = countNodes();
		int subSize = subPath.countNodes();
		if (subSize > size) {
			return false;
		}
		return subPath(size - subSize, size - 1).equals(subPath);
	}

	/**
	 * @return the number of nodes in the path
	 */
	default int countNodes() {
		Box<Integer> count = new Box<>(0);
		//noinspection CodeBlock2Expr
		visitFromHead((x) -> { count.value ++; }, (e) -> {});
		return count.value;
	}

	/**
	 * @return the number of edges in the path
	 */
	default int countEdges() {
		return countNodes() - 1;
	}

	/**
	 * @return an ordered list of nodes from the head to the tail
	 */
	default List<NodeT> getNodesFromHead() {
		List<NodeT> nodes = new ArrayList<>();
		visitFromHead(nodes::add, (e) -> {});
		return nodes;
	}

	/**
	 * @return an ordered list of nodes from the tail to the head
	 */
	default List<NodeT> getNodesFromTail() {
		List<NodeT> nodes = new ArrayList<>();
		visitFromTail(nodes::add, (e) -> {});
		return nodes;
	}

	/**
	 * @return an ordered list of edges from the head to the tail
	 */
	default List<EdgeT> getEdgesFromHead() {
		List<EdgeT> edges = new ArrayList<>();
		visitFromHead((n) -> {}, edges::add);
		return edges;
	}

	/**
	 * @return an ordered list of edges from the tail to the head
	 */
	default List<EdgeT> getEdgesFromTail() {
		List<EdgeT> edges = new ArrayList<>();
		visitFromTail((n) -> {}, edges::add);
		return edges;
	}
}
