# The five algorithms

Every algorithm sorts the `int[]` **in place** (it changes the same array, no copy is returned to the window). The window calls them all through `Principal.sort(name, array)`, so typed numbers and numbers read from a file go through exactly the same code.

| Algorithm | Class | Best | Average | Worst | Extra memory | Stable |
|---|---|---|---|---|---|---|
| Bubble Sort | `Bubble_Sort` | O(n) | O(n²) | O(n²) | O(1) | Yes |
| Insertion Sort | `Insertion_Sort` | O(n) | O(n²) | O(n²) | O(1) | Yes |
| Shell Sort | `Shell_Sort` | O(n log n) | depends on the gaps | O(n²) | O(1) | No |
| Merge Sort | `Merge_Sort` | O(n log n) | O(n log n) | O(n log n) | O(n) | Yes |
| QuickSort | `Quick_Sort` | O(n log n) | O(n log n) | O(n²) | O(log n) | No |

*Stable* means that equal values keep their original order. With plain numbers you cannot see the difference, but it matters when you sort records by one field.

## Bubble Sort

Walks the array comparing **neighbours** (`array[j]` and `array[j + 1]`) and swaps them when they are in the wrong order. After the first pass the biggest value has "bubbled up" to the end, after the second pass the two biggest, and so on, so each pass can stop one position earlier.

If a whole pass makes no swap the array is already sorted and the loop stops: that is why an already sorted input takes O(n).

> Before v1 the class compared `array[i]` with every later `array[j]`. That also sorts, but it is a different algorithm (exchange sort) and never stops early.

## Insertion Sort

Grows a sorted part on the left, one element at a time. It takes the next value (`key`), moves every bigger value of the sorted part one place to the right, and drops `key` into the hole. Like sorting playing cards in your hand. Very fast on small or almost sorted arrays.

## Shell Sort

An insertion sort that first compares elements that are far apart (`gap = n/2`, then `n/4`, ... down to `1`). The early passes move values close to their final place with few moves, so the last pass (a normal insertion sort, `gap = 1`) has little left to do. Its speed depends on the gap sequence; halving the gap is the simplest one.

## Merge Sort

Divide and conquer: split the array in two halves, sort each half recursively, then **merge** the two sorted halves by repeatedly taking the smaller front element. Always O(n log n), whatever the input looks like, but it needs temporary arrays (`L` and `R`) as big as the part being merged.

## QuickSort

Also divide and conquer, but the work happens before the recursion: **partition** the array around a *pivot* so that everything on the left is ≤ everything on the right, then sort both sides.

v1 uses **Hoare's partition** with the **middle element** as the pivot: two indexes walk towards each other and swap the pairs found on the wrong side. Two details make it safe on any input:

- **Middle pivot.** The original code used the last element. On an already sorted file that pivot is always the biggest value, every partition removes only one element, and the recursion went one level deep per number until Java threw `StackOverflowError` (around 50,000 numbers).
- **Recurse into the smaller side, loop over the bigger one.** Even with an unlucky pivot, the recursion depth is at most log₂ n (about 18 levels for 200,000 numbers).

The worst case is still O(n²) time for specially built inputs; picking a random pivot or switching to Heap Sort (introsort) would remove it. That is left for v2.

## About the measured time

The window shows how long one run took with `System.nanoTime()`. Take it as a rough idea, not a benchmark: the first runs include the time the JVM spends compiling the code (JIT), and other programs running on the computer also change the result. Run the same input several times and you will see different numbers.
