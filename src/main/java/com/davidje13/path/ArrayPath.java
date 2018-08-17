package com.davidje13.path;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static java.util.Collections.singletonList;

public class ArrayPath<NodeT, EdgeT> extends ComparablePath<NodeT, EdgeT> {
	private final List<EdgeNodeWrapper<NodeT, EdgeT>> items;

	public static <NodeT, EdgeT> ArrayPathBuilder<NodeT, EdgeT> builder(
			NodeT node
	) {
		return new ArrayPathBuilder<>(node);
	}

	public static <NodeT, EdgeT> ArrayPathBuilder<NodeT, EdgeT> builder(
			ImmutablePath<NodeT, EdgeT> basePath
	) {
		return new ArrayPathBuilder<>(basePath);
	}

	public static <NodeT, EdgeT> ArrayPath<NodeT, EdgeT> concatenate(
			ImmutablePath<NodeT, EdgeT> path1,
			EdgeT joiner,
			ImmutablePath<NodeT, EdgeT> path2
	) {
		return builder(path1).pushBack(joiner, path2).build();
	}

	public ArrayPath(NodeT singleNode) {
		items = singletonList(new EdgeNodeWrapper<>(null, singleNode));
	}

	private ArrayPath(List<EdgeNodeWrapper<NodeT, EdgeT>> items) {
		this.items = items;
	}

	@Override
	public ArrayPathBuilder<NodeT, EdgeT> builderFromNode(NodeT node) {
		return builder(node);
	}

	@Override
	public NodeT getHead() {
		return items.get(0).node;
	}

	@Override
	public NodeT getTail() {
		return items.get(items.size() - 1).node;
	}

	@Override
	public void visitPairsFromHead(
			BiConsumer<? super EdgeT, ? super NodeT> visitor
	) {
		for (int i = 1; i < items.size(); ++ i) {
			EdgeNodeWrapper<NodeT, EdgeT> wrapper = items.get(i);
			visitor.accept(wrapper.edge, wrapper.node);
		}
	}

	@Override
	@SuppressWarnings("ValueOfIncrementOrDecrementUsed")
	public void visitPairsFromTail(
			BiConsumer<? super EdgeT, ? super NodeT> visitor
	) {
		for (int i = items.size(); (i --) > 1;) {
			visitor.accept(items.get(i).edge, items.get(i - 1).node);
		}
	}

	@Override
	public int countNodes() {
		return items.size();
	}

	public static class ArrayPathBuilder<NodeT, EdgeT>
			implements PathBuilder<NodeT, EdgeT, ArrayPathBuilder<NodeT, EdgeT>> {
		private List<EdgeNodeWrapper<NodeT, EdgeT>> items;

		private ArrayPathBuilder(NodeT node) {
			reset(node);
		}

		private ArrayPathBuilder(ImmutablePath<NodeT, EdgeT> basePath) {
			this(basePath.getHead());
			basePath.visitPairsFromHead(this::pushBack);
		}

		@Override
		public NodeT getHead() {
			return items.get(0).node;
		}

		@Override
		public NodeT getTail() {
			return items.get(items.size() - 1).node;
		}

		@Override
		public ArrayPathBuilder<NodeT, EdgeT> pushBack(
				EdgeT edge,
				NodeT node
		) {
			items.add(new EdgeNodeWrapper<>(edge, node));

			return this;
		}

		@Override
		public ArrayPathBuilder<NodeT, EdgeT> pushFront(
				NodeT node,
				EdgeT edge
		) {
			items.get(0).edge = edge;
			items.add(0, new EdgeNodeWrapper<>(null, node));

			return this;
		}

		@Override
		public ArrayPathBuilder<NodeT, EdgeT> reset(NodeT node) {
			items = new ArrayList<>();
			items.add(new EdgeNodeWrapper<>(null, node));

			return this;
		}

		@Override
		public ArrayPath<NodeT, EdgeT> build() {
			ArrayPath<NodeT, EdgeT> path = new ArrayPath<>(items);
			invalidate();
			return path;
		}

		private void invalidate() {
			items = null;
		}
	}

	private static class EdgeNodeWrapper<NodeT, EdgeT> {
		private EdgeT edge;
		private final NodeT node;

		private EdgeNodeWrapper(EdgeT edge, NodeT node) {
			this.edge = edge;
			this.node = node;
		}
	}
}
