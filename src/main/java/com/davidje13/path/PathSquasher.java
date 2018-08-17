package com.davidje13.path;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class PathSquasher {
	public <InNodeT, InEdgeT, OutNodeT, OutEdgeT>
	ImmutablePath<OutNodeT, OutEdgeT> squashNodes(
			ImmutablePath<InNodeT, InEdgeT> path,
			BiPredicate<InNodeT, InNodeT> nodeEqualityTester,
			Function<ImmutablePath<InNodeT, InEdgeT>, OutNodeT> nodeCompactor,
			Function<InEdgeT, OutEdgeT> edgeConverter
	) {
		List<OutNodeT> outNodes = new ArrayList<>();
		List<OutEdgeT> outEdges = new ArrayList<>();

		path.visitGroupedFromHead(
				nodeEqualityTester,
				(group) -> outNodes.add(nodeCompactor.apply(group)),
				(edge) -> outEdges.add(edgeConverter.apply(edge))
		);

		return buildPath(outNodes, outEdges);
	}

	public <InNodeT, InEdgeT, OutNodeT, OutEdgeT>
	ImmutablePath<OutNodeT, OutEdgeT> expandNodes(
			ImmutablePath<InNodeT, InEdgeT> path,
			Function<InNodeT, ImmutablePath<OutNodeT, OutEdgeT>> nodeExpander,
			Function<InEdgeT, OutEdgeT> edgeConverter
	) {
		List<OutNodeT> outNodes = new ArrayList<>();
		List<OutEdgeT> outEdges = new ArrayList<>();

		path.visitFromHead(
				(node) -> nodeExpander.apply(node).visitFromHead(outNodes::add, outEdges::add),
				(edge) -> outEdges.add(edgeConverter.apply(edge))
		);

		return buildPath(outNodes, outEdges);
	}

	private <NodeT, EdgeT> ImmutablePath<NodeT, EdgeT> buildPath(
			List<NodeT> nodes,
			List<EdgeT> edges
	) {
		if (nodes.size() != edges.size() + 1) {
			throw new IllegalArgumentException(
					"Mismatched nodes (" + nodes.size() + ")" +
					" and edges (" + edges.size() + ")"
			);
		}

		PathBuilder<NodeT, EdgeT, ? extends PathBuilder> builder =
				LinkedPath.builder(nodes.get(0));

		for (int index = 0; index < edges.size(); ++ index) {
			builder.pushBack(
					edges.get(index),
					nodes.get(index + 1)
			);
		}

		return builder.build();
	}

}
