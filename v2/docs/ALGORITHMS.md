# The algorithms of AlgoScope

Six sorting algorithms, four search algorithms and one maze generator. Every number on this page was produced by the code in this folder; the same inputs give the same numbers in the app.

For how they are recorded and animated, see [ARCHITECTURE.md](ARCHITECTURE.md).

---

## Sorting

Every algorithm sorts the array **in place**, in ascending order, and only touches it through three operations, which are what the app counts and animates:

- **Compare** two values (amber). The most common operation, and the one that makes O(n²) algorithms slow.
- **Swap** two values (red).
- **Write** a value over another one (blue): Insertion Sort shifting values, Merge Sort copying back from its helper array.

| Algorithm | Best | Average | Worst | Extra memory | Stable |
|---|---|---|---|---|---|
| Bubble Sort | O(n) | O(n²) | O(n²) | O(1) | Yes |
| Insertion Sort | O(n) | O(n²) | O(n²) | O(1) | Yes |
| Selection Sort | O(n²) | O(n²) | O(n²) | O(1) | No |
| Merge Sort | O(n log n) | O(n log n) | O(n log n) | O(n) | Yes |
| Quick Sort | O(n log n) | O(n log n) | O(n²) | O(log n) | No |
| Heap Sort | O(n log n) | O(n log n) | O(n log n) | O(1) | No |

*Stable* means equal values keep their original order. With plain numbers it cannot be seen, but it matters when sorting records by one field (people by age, keeping them in alphabetical order inside each age).

### Measured: 100 values

Comparisons / swaps / writes for each kind of input. These are the arrays the app shows when the size is set to 100 before pressing *New input* (seed 1):

| Algorithm | Random | Already sorted | Reversed | Few unique |
|---|---|---|---|---|
| Bubble Sort | 4,935 / 2,800 / 0 | **99** / 0 / 0 | 4,950 / 4,950 / 0 | 4,389 / 1,962 / 0 |
| Insertion Sort | 2,895 / 0 / 2,894 | **99** / 0 / 0 | 4,950 / 0 / 5,049 | 2,060 / 0 / 2,039 |
| Selection Sort | 4,950 / 94 / 0 | 4,950 / 0 / 0 | 4,950 / 50 / 0 | 4,950 / 77 / 0 |
| Merge Sort | 596 / 0 / 594 | **99** / 0 / 0 | 415 / 0 / 672 | 575 / 0 / 571 |
| Quick Sort | 992 / 160 / 0 | 771 / 0 / 0 | 772 / 50 / 0 | 876 / 265 / 0 |
| Heap Sort | 1,028 / 576 / 0 | 1,081 / 640 / 0 | 944 / 516 / 0 | 850 / 421 / 0 |

What the table shows:

- **n² vs n log n:** 4,950 is every pair of 100 values (100 · 99 / 2). The O(n²) algorithms get close to it; the O(n log n) ones stay around 600 to 1,000 (100 · log₂ 100 ≈ 664).
- **Selection Sort does not care about the input:** always 4,950 comparisons. But it never swaps more than n − 1 times, the fewest of all.
- **Already sorted input is the best case for Bubble, Insertion and Merge Sort** (99 comparisons) and changes nothing for Heap Sort, which first rearranges the values into a heap anyway.
- **Insertion Sort on reversed input** writes 5,049 times: 4,950 shifts plus 99 times dropping the value into place.

### Bubble Sort

Compares neighbours (`a[j]`, `a[j + 1]`) and swaps them when they are out of order. After each pass the biggest value left has "bubbled up" to the end, so the next pass can stop one position earlier. A pass without swaps means the array is sorted, which is why sorted input takes only n − 1 comparisons.

### Insertion Sort

Grows a sorted part on the left. It takes the next value (the *key*), shifts every bigger value of the sorted part one place to the right, and drops the key into the gap. Like sorting playing cards in your hand. Very fast on small or almost sorted arrays, which is why real libraries use it for small pieces inside Merge Sort or Quick Sort.

### Selection Sort

Finds the smallest value of the unsorted part and swaps it to the front. Always n(n − 1)/2 comparisons, but at most n − 1 swaps. It is **not stable**: the long swap can jump a value over an equal one (`[2a, 2b, 1]` ends as `[1, 2b, 2a]`).

### Merge Sort

Divide and conquer: sort each half, then merge the two sorted halves by repeatedly taking the smaller front value. It needs a helper array as big as the input.

Two details:
- **Equal values are taken from the left half first**, which keeps it stable.
- **If the last value on the left is not bigger than the first on the right, the halves are already in order and the merge is skipped.** That turns sorted input into n − 1 comparisons, and also explains why reversed input needs fewer comparisons (415) than random input (596): every merge of reversed runs takes the whole right half first and then copies the left one without comparing.

### Quick Sort

Picks a **pivot** and splits the array so that everything on the left is ≤ everything on the right, then sorts both sides. It uses **Hoare's partition**: two indexes walk towards each other and swap the pairs found on the wrong side.

- **The pivot is the middle value.** The last value, the textbook choice and the one v1 used, makes sorted input the worst case: every split removes one value, and v1 crashed with `StackOverflowError` around 50,000 values.
- **It recurses into the smaller side and loops over the bigger one**, so the recursion is never deeper than log₂ n, whatever the pivots. Tested with 1,000,000 sorted, reversed and equal values.
- The worst case is still O(n²) time for specially built inputs. Introsort (switching to Heap Sort when the recursion gets too deep) would remove it; that is what C++'s `std::sort` does.

In the app, Quick Sort's comparisons say "with the pivot": the pivot is kept in a variable, not at a fixed position of the array.

### Heap Sort

Arranges the array as a **max-heap**: a binary tree stored in the array (the children of position i are at 2i + 1 and 2i + 2) where every parent is ≥ its children, so the biggest value is at the root. Then, again and again: swap the root to the end, shrink the heap by one, and sift the new root down to repair it. O(n log n) always and no extra memory, but it jumps around the array, which is why it is usually slower in practice than Quick Sort.

---

## Pathfinding

The map is a **grid**, which is a graph in disguise: every cell is a node connected to its open neighbours up, right, down and left. Entering a cell costs:

| Terrain | Cost |
|---|---|
| Open ground | 1 |
| Mud | 5 |
| Wall | cannot be entered |

Without mud every move costs the same, the path with the fewest steps is also the cheapest one, and BFS and Dijkstra find the same thing. Mud is what separates them.

The app shows three states for a cell: **frontier** (green: discovered, waiting to be explored), **explored** (blue) and **path** (yellow).

| Algorithm | Data structure | Uses mud costs | Path found |
|---|---|---|---|
| BFS | Queue (first in, first out) | No | Fewest steps |
| DFS | Stack (last in, first out) | No | A path, rarely the best |
| Dijkstra | Priority queue by cost so far | Yes | Cheapest |
| A* | Priority queue by cost so far + estimate | Yes | Cheapest, exploring fewer cells |

### Measured

Medium maps (25 x 41), as the app creates them, and an open 30 x 30 grid. The *maze with loops* is the map the app opens with, and the *random map* is the one the first press of *Random map* gives:

| Map | Algorithm | Steps | Cost | Cells explored |
|---|---|---|---|---|
| **Random map** (25% walls, 20% mud, seed 2) | BFS | 64 | 140 | 750 |
| | DFS | 128 | 248 | 150 |
| | Dijkstra | 66 | **78** | 721 |
| | A* | 70 | **78** | **361** |
| **Maze with loops** (12%, seed 1) | BFS | 84 | 84 | 559 |
| | DFS | 140 | 140 | 171 |
| | Dijkstra | 84 | 84 | 559 |
| | A* | 84 | 84 | **240** |
| **Perfect maze** (no loops, seed 1) | all four | 296 | 296 | BFS 543, DFS 465, Dijkstra 543, A* 514 |
| **Open 30 x 30** | BFS, Dijkstra | 58 | 58 | 900 |
| | DFS, A* | 58 | 58 | **59** |

What the table shows:

- **On the random map BFS takes the fewest steps (64) but pays 140**, because it walks straight through the mud. Dijkstra and A* go around it for 78.
- **Dijkstra and A* find different paths with the same cost** (66 and 70 steps, both 78): there can be several cheapest paths, and each algorithm finds one.
- **A* explores half the cells of Dijkstra on the random map** and 15 times fewer on the open grid.
- **In a perfect maze there is only one route**, so all four find the same 296 steps, and A*'s estimate barely helps (514 vs 543 cells): the walls keep forcing it away from the goal.
- **Without mud, BFS and Dijkstra behave the same** (maze with loops: same path, same 559 cells).
- **DFS is lucky on the open grid**: it tries up first, but from the top row that is outside the map, so it goes right along the edge and then down, which is exactly a shortest way to the goal. On the random map it wanders: 128 steps.

### BFS (breadth-first search)

Explores in rings: every cell 1 step away, then every cell 2 steps away, and so on. A **queue** keeps that order: cells are explored in the order they were discovered. So the first time the goal is reached, it is through a path with the fewest steps. It ignores mud entirely.

### DFS (depth-first search)

Follows one direction as far as it can and backs up only when stuck. A **stack** gives that order: the last cell discovered is the first explored. It always finds a route if there is one, but usually a long, winding one. Neighbours are pushed in reverse so the first direction (up) is the first one tried.

### Dijkstra

Always explores the **cheapest** cell of the frontier, the one with the lowest cost from the start. Cells are therefore explored in order of cost, and the first time the goal is explored its path is the cheapest one. When a cheaper way to reach a frontier cell is found, the cell is queued again with the new cost, and the older, dearer entry is skipped when it comes out.

### A*

Dijkstra with a sense of direction. The priority is:

> cost so far + estimate of the cost left

The estimate is the **Manhattan distance** to the goal (rows apart + columns apart) times the cheapest terrain cost, 1. It **never overestimates**: every move costs at least 1 and at least that many moves are needed. That property (*admissible*, and here also *consistent*) is what guarantees that A* still finds the cheapest path. If a terrain cheaper than 1 were added, or the estimate were multiplied by 3, A* could return a more expensive path. Planting that second bug in a copy of the code makes the comparison against the reference solver fail, which shows the tests would catch it.

**The tie-break that makes A\* fast.** On an open grid many cells have exactly the same priority: going one step right and one step down from the start both cost 1 and leave the same distance. Which of the tied cells is explored first decides everything:

| Tie-break | Cells explored on an open 30 x 30 grid |
|---|---|
| Oldest first (plain priority queue order) | 900, every cell, same as Dijkstra |
| **Smaller estimate first (closer to the goal)** | **59**, just the path and the start |

With oldest-first, A* finishes every "layer" of equal priority before moving on, which on an open grid means everything. Preferring the cell closer to the goal among equal priorities lets it dive straight there. The path is just as cheap either way; only the work changes. AlgoScope breaks ties by estimate, then by age, so every run is also repeatable.

### Maze generator: DFS used to build

`GridGenerator.maze` uses the **recursive backtracker**, the same idea as DFS but used to carve instead of to search:

1. Rooms are the cells with both coordinates even; everything else starts as a wall.
2. From the current room, pick a random neighbouring room (two cells away) that has not been visited, open the wall between them, and move there.
3. When no neighbour is left, back up to the previous room (pop the stack).

The result is a **perfect maze**: exactly one route between any two rooms (as a graph, a tree: n open cells joined by n − 1 connections, which a test checks). It is written with its own stack instead of recursion, so a big maze cannot overflow Java's call stack.

Then a fraction of the remaining inner walls (12% in the app) is knocked down to create **loops**. With several routes the four algorithms start to choose differently, which makes the maze far more interesting to watch.
