# Multivalue Collections

Java collection classes for situations where multiple types of object need to
live in one collection.

Collections are immutable. Populating collections with values is achieved via
builders.

## Path

Paths store a node-edge-node-edge-node pattern. They always start and finish on
a node.

The types of path supported are:

- `LinkedPath` (backed by a doubly-linked-list)
- `ArrayPath` (backed by an array)

The API for both is identical. The common interface is `ImmutablePath`.

### Example

```java
import com.davidje13.path.*;

ImmutablePath<String, String> path = LinkedPath.builder("node 1")
    .pushBack("edge 1", "node 2")
    .pushBack("edge 2", "node 3")
    .build();

List<String> nodes = path.getNodesFromHead();
// nodes = ["node 1", "node 2", "node 3"]

List<Object> seen = new ArrayList();
path.visitFromHead(
    (node) -> seen.add(node),
    (edge) -> seen.add(edge)
);
// seen = ["node 1", "edge 1", "node 2", "edge 2", "node 3"]
```
