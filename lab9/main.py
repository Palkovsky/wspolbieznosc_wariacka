import sys
import itertools

class Graph(object):
    def __init__(self, vertices, edges):
        self.V = vertices
        self.E = edges

    # Dot format of the graph
    def dot(self):
        dot = "digraph g {\r\n"
        for vertex in self.V:
            lab = vertex.split("_")[0]
            dot +=  "{}[label={}]\r\n".format(vertex, lab)
        for (u, v) in self.E:
            dot += "{} -> {}\r\n".format(u, v)
        dot += "}"
        return dot

    # Removes redundant edges
    def minify(self):
        # mistrzostwo czystego kodu
        [self.E.discard((u, v_prim)) for u in self.V for v in self._direct(u) for v_prim in self._dfs(v) if v_prim != v]

    def as_fnf(self):
        # Nodes with no inbound edges should start BFS
        inbound = dict([(v, 0) for v in self.V])
        for (a, b) in self.E:
            inbound[b] += 1

        start_nodes = [k for (k, v) in inbound.items() if v == 0]
        return self._bfs(start_nodes)

    def _bfs(self, nodes):
        visited = dict([(v, False) for v in self.V])
        queue = set([(node, 0) for node in nodes])
        fnf = []

        while True:
            new_queue = set([])
            for (v, lvl) in queue:
                visited[v] = lvl+1

                for u in self._direct(v):
                    items = [(x, l) for (x, l) in new_queue if x == u]
                    if len(items) == 0:
                        new_queue.add((u, lvl+1))
                    else:
                        x, l = items[0]
                        if lvl > l:
                            new_queue.discard(items[0])
                            new_queue.add((u, lvl+1))

            queue = new_queue
            if len(queue) == 0:
                break

        fnf = []
        while visited:
            min_val = min(visited.values())

            word = []
            for (k, v) in dict(visited).items():
                if v == min_val:
                    sym = k.split("_")[0]
                    word.append(sym)
                    del visited[k]

            fnf.append(''.join(sorted(set(word))))

        return fnf


    # Returns directly conected vertices
    def _direct(self, v):
        return set([b for (a, b) in self.E if a == v])


    # Returns visited vertices
    def _dfs(self, v, visited = set([])):
        if v in visited:
            return set([])

        results = set([v])

        for u in self._direct(v):
            result = self._dfs(u, results)
            results = results.union(result)

        return results

    def __repr__(self):
        return str((self.V, self.E))

class Problem(object):
    def __init__(self, alphabet, word):
        self.sigma = alphabet
        self.w = word
        self.I = []
        self.D = []

    # Load the independency relation
    def RI(self, I):
        self.I = I
        self.D = self._complement(self.I)
        return self

    # Load the dependency relation
    def RD(self, D):
        self.D = D
        self.I = self._complement(self.D)
        return self

    # Generate a trace based on the word w and the relation I
    def trace(self):
        T = {self.w}
        commutes = lambda x, y: (x, y) in self.I

        while True:
            T_next = set(T)

            for w in T:
                for i in range(len(w)-1):
                    # For each neighboring characters check if independent.
                    # If so, swap them and add to the set.
                    prev, c1, c2, rest = w[:i], w[i], w[i+1], w[min(i+2, len(w)):]
                    if commutes(c1, c2):
                        T_next.add(prev + c2 + c1 + rest)

            # If set hadn't changed, it is time to stop.
            if T == T_next:
                break
            T = T_next

        return T

    # Generate the Foata Normal Form
    def fnf(self):
        w, sigma, I, D = self.w, self.sigma, self.I, self.D
        n = len(w)

        # Prepare the stacks for each symbol in the alphabet
        stacks = dict([(c, []) for c in sigma])
        for c in w[::-1]:
            for (ident, stack) in stacks.items():
                if c == ident:
                    stack.insert(0, c)
                elif (c, ident) not in I:
                    stack.insert(0, None)

        fnf = []
        empty = lambda stacks: list(itertools.chain(*stacks.values())) == []
        while not empty(stacks):
            # Get the symbols from stack tops
            chars = [stack[0] for (ident, stack) in stacks.items() if len(stack) > 0 and stack[0] != None]

            # Remove the marking from the dependent stacks
            for char in chars:
                dependencies = [b for (a, b) in D if a == char]
                for dep in dependencies:
                    stack = stacks[dep]
                    if len(stack) > 0 and stack[0] == None:
                        stack.pop(0)

            # Remove the chars from their stacks
            for char in chars:
                stack = stacks[char]
                stack.pop(0)

            # Sort the symbols and convert into a string
            joined = ''.join(sorted(chars))
            if joined != '':
                fnf.append(joined)

        return fnf

    def graph(self):
        cols = self.fnf()
        edges = set([])

        for i in range(len(cols)):
            col1 = cols[i]
            for sym1 in col1:
                edg = [("{}_{}".format(sym1, i), "{}_{}".format(sym2, j)) for j in range(i+1, len(cols)) for sym2 in cols[j] if (sym1, sym2) in self.D]
                edges = edges.union(set(edg))

        vertices = set(itertools.chain(*[[a, b] for (a, b) in edges]))

        return Graph(vertices, edges)


    def __repr__(self):
        return "SIGMA: {}\nI: {}\nD: {}\nw: {}".format(self.sigma, self.I, self.D, self.w)

    def _complement(self, S):
        return [pair for pair in itertools.product(self.sigma, self.sigma) if pair not in S]


def read_input():
    lines = [l.rstrip() for l in sys.stdin.readlines()]
    alphabet = lines[0].split(" ")

    rel_type, *relation = lines[1].split(" ")
    rels = [(relation[i], relation[i+1]) for i in range(0, len(relation), 2)]

    word = lines[2]

    problem = Problem(alphabet, word)
    return problem.RI(rels) if rel_type == "I" else problem.RD(rels)


if __name__ == "__main__":
    prob = read_input()

    print("=== PROB ===")
    print(prob)

    print("=== TRACE ===")
    print(prob.w)
    print(prob.trace())

    print("=== FNF ===")
    print(prob.fnf())

    print("=== GRAPH ===")
    g = prob.graph()
    g.minify()
    print(g.dot())

    print("=== FNF FROM GRAPH ===")
    print(g.as_fnf())
