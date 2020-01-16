import sys
import itertools

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

