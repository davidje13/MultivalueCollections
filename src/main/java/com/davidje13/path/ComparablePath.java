package com.davidje13.path;

import java.util.Objects;

public abstract class ComparablePath<NodeT, EdgeT> implements ImmutablePath<NodeT, EdgeT> {
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ImmutablePath)) {
			return false;
		}
		ImmutablePath<?, ?> that = (ImmutablePath<?, ?>) o;
		return (
				Objects.equals(getNodesFromHead(), that.getNodesFromHead()) &&
				Objects.equals(getEdgesFromHead(), that.getEdgesFromHead())
		);
	}

	@Override
	public int hashCode() {
		return Objects.hash(getNodesFromHead(), getEdgesFromHead());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		visitFromHead(
				(node) -> builder.append(node).append(", "),
				(edge) -> builder.append(edge).append(", ")
		);
		builder.setLength(builder.length() - 2);
		return builder.toString();
	}
}
