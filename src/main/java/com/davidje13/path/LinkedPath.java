package com.davidje13.path;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class LinkedPath<NodeT, EdgeT> extends ComparablePath<NodeT, EdgeT> {
	private final NodeWrapper<NodeT, EdgeT> head;
	private final NodeWrapper<NodeT, EdgeT> tail;

	public static <NodeT, EdgeT> LinkedPathBuilder<NodeT, EdgeT> builder(
			NodeT node
	) {
		return new LinkedPathBuilder<>(node);
	}

	public static <NodeT, EdgeT> LinkedPathBuilder<NodeT, EdgeT> builder(
			ImmutablePath<NodeT, EdgeT> basePath
	) {
		return new LinkedPathBuilder<>(basePath);
	}

	public static <NodeT, EdgeT> LinkedPath<NodeT, EdgeT> concatenate(
			ImmutablePath<NodeT, EdgeT> path1,
			EdgeT joiner,
			ImmutablePath<NodeT, EdgeT> path2
	) {
		return builder(path1).pushBack(joiner, path2).build();
	}

	@SuppressWarnings("NestedAssignment")
	public LinkedPath(NodeT singleNode) {
		head = tail = new NodeWrapper<>(singleNode);
	}

	private LinkedPath(
			NodeWrapper<NodeT, EdgeT> head,
			NodeWrapper<NodeT, EdgeT> tail
	) {
		this.head = head;
		this.tail = tail;
	}

	@Override
	public LinkedPathBuilder<NodeT, EdgeT> builderFromNode(NodeT node) {
		return builder(node);
	}

	@Override
	public NodeT getHead() {
		return head.node;
	}

	@Override
	public NodeT getTail() {
		return tail.node;
	}

	@Override
	public void visitPairsFromHead(
			BiConsumer<? super EdgeT, ? super NodeT> visitor
	) {
		visitPairsSkipFirst(
				head,
				(nodeWrapper) -> nodeWrapper.next,
				(edgeWrapper) -> edgeWrapper.next,
				visitor
		);
	}

	@Override
	public void visitPairsFromTail(
			BiConsumer<? super EdgeT, ? super NodeT> visitor
	) {
		visitPairsSkipFirst(
				tail,
				(nodeWrapper) -> nodeWrapper.prev,
				(edgeWrapper) -> edgeWrapper.prev,
				visitor
		);
	}

	@Override
	public void visitFromHead(
			Consumer<? super NodeT> nodeVisitor,
			Consumer<? super EdgeT> edgeVisitor
	) {
		visit(
				head,
				(nodeWrapper) -> nodeWrapper.next,
				(edgeWrapper) -> edgeWrapper.next,
				nodeVisitor,
				edgeVisitor
		);
	}

	@Override
	public void visitFromTail(
			Consumer<? super NodeT> nodeVisitor,
			Consumer<? super EdgeT> edgeVisitor
	) {
		visit(
				tail,
				(nodeWrapper) -> nodeWrapper.prev,
				(edgeWrapper) -> edgeWrapper.prev,
				nodeVisitor,
				edgeVisitor
		);
	}

	private void visitPairsSkipFirst(
			NodeWrapper<NodeT, EdgeT> begin,
			Function<NodeWrapper<NodeT, EdgeT>, EdgeWrapper<NodeT, EdgeT>> advanceNode,
			Function<EdgeWrapper<NodeT, EdgeT>, NodeWrapper<NodeT, EdgeT>> advanceEdge,
			BiConsumer<? super EdgeT, ? super NodeT> visitor
	) {
		NodeWrapper<NodeT, EdgeT> nodeCursor = begin;
		while (true) {
			EdgeWrapper<NodeT, EdgeT> edgeCursor = advanceNode.apply(nodeCursor);
			if (edgeCursor == null) {
				return;
			}
			nodeCursor = advanceEdge.apply(edgeCursor);
			visitor.accept(edgeCursor.edge, nodeCursor.node);
		}
	}

	private void visit(
			NodeWrapper<NodeT, EdgeT> begin,
			Function<NodeWrapper<NodeT, EdgeT>, EdgeWrapper<NodeT, EdgeT>> advanceNode,
			Function<EdgeWrapper<NodeT, EdgeT>, NodeWrapper<NodeT, EdgeT>> advanceEdge,
			Consumer<? super NodeT> nodeVisitor,
			Consumer<? super EdgeT> edgeVisitor
	) {
		NodeWrapper<NodeT, EdgeT> nodeCursor = begin;
		while (true) {
			nodeVisitor.accept(nodeCursor.node);
			EdgeWrapper<NodeT, EdgeT> edgeCursor = advanceNode.apply(nodeCursor);
			if (edgeCursor == null) {
				return;
			}
			edgeVisitor.accept(edgeCursor.edge);
			nodeCursor = advanceEdge.apply(edgeCursor);
		}
	}

	public static class LinkedPathBuilder<NodeT, EdgeT>
			implements PathBuilder<NodeT, EdgeT, LinkedPathBuilder<NodeT, EdgeT>> {
		private NodeWrapper<NodeT, EdgeT> head;
		private NodeWrapper<NodeT, EdgeT> tail;

		private LinkedPathBuilder(NodeT node) {
			reset(node);
		}

		private LinkedPathBuilder(ImmutablePath<NodeT, EdgeT> basePath) {
			this(basePath.getHead());
			basePath.visitPairsFromHead(this::pushBack);
		}

		@Override
		public NodeT getHead() {
			return head.node;
		}

		@Override
		public NodeT getTail() {
			return tail.node;
		}

		@Override
		public LinkedPathBuilder<NodeT, EdgeT> pushBack(
				EdgeT edge,
				NodeT node
		) {
			EdgeWrapper<NodeT, EdgeT> edgeWrapper = new EdgeWrapper<>(edge);
			NodeWrapper<NodeT, EdgeT> nodeWrapper = new NodeWrapper<>(node);

			link(edgeWrapper, nodeWrapper);
			link(tail, edgeWrapper);
			tail = nodeWrapper;

			return this;
		}

		@Override
		public LinkedPathBuilder<NodeT, EdgeT> pushFront(
				NodeT node,
				EdgeT edge
		) {
			EdgeWrapper<NodeT, EdgeT> edgeWrapper = new EdgeWrapper<>(edge);
			NodeWrapper<NodeT, EdgeT> nodeWrapper = new NodeWrapper<>(node);

			link(nodeWrapper, edgeWrapper);
			link(edgeWrapper, head);
			head = nodeWrapper;

			return this;
		}

		@Override
		public LinkedPathBuilder<NodeT, EdgeT> reset(NodeT node) {
			NodeWrapper<NodeT, EdgeT> nodeWrapper = new NodeWrapper<>(node);
			head = nodeWrapper;
			tail = nodeWrapper;

			return this;
		}

		@Override
		public LinkedPath<NodeT, EdgeT> build() {
			LinkedPath<NodeT, EdgeT> path = new LinkedPath<>(head, tail);
			invalidate();
			return path;
		}

		private void invalidate() {
			head = null;
			tail = null;
		}
	}

	private static class Link<LinkedT> {
		LinkedT prev;
		LinkedT next;
	}

	private static <LinkT1 extends Link<LinkT2>, LinkT2 extends Link<LinkT1>> void link(
			LinkT1 first,
			LinkT2 second
	) {
		first.next = second;
		second.prev = first;
	}

	private static class NodeWrapper<NodeT, EdgeT> extends Link<EdgeWrapper<NodeT, EdgeT>> {
		private final NodeT node;

		private NodeWrapper(NodeT node) {
			this.node = node;
		}
	}

	private static class EdgeWrapper<NodeT, EdgeT> extends Link<NodeWrapper<NodeT, EdgeT>> {
		private final EdgeT edge;

		private EdgeWrapper(EdgeT edge) {
			this.edge = edge;
		}
	}
}
