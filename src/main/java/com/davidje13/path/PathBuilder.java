package com.davidje13.path;

public interface PathBuilder<NodeT, EdgeT, T extends PathBuilder<NodeT, EdgeT, T>> {
	NodeT getHead();
	NodeT getTail();

	T pushBack(EdgeT edge, NodeT node);
	T pushFront(NodeT node, EdgeT edge);

	T reset(NodeT node);
	ImmutablePath<NodeT, EdgeT> build();

	/**
	 * Add a path to the back of the current builder
	 * @param edge the edge to be added between the current nodes and the new
	 *             nodes
	 * @param path the path to add
	 * @return the current builder (for chaining)
	 */
	default T pushBack(EdgeT edge, ImmutablePath<NodeT, EdgeT> path) {
		pushBack(edge, path.getHead());
		path.visitPairsFromHead(this::pushBack);

		//noinspection unchecked
		return (T) this;
	}

	/**
	 * Add a path to the front of the current builder
	 * @param path the path to add
	 * @param edge the edge to be added between the new nodes and the current
	 *             nodes
	 * @return the current builder (for chaining)
	 */
	default T pushFront(ImmutablePath<NodeT, EdgeT> path, EdgeT edge) {
		pushFront(path.getTail(), edge);
		path.visitPairsFromTail((e, n) -> this.pushFront(n, e));

		//noinspection unchecked
		return (T) this;
	}
}
