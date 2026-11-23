# Sorting Algorithms

[![v1 tests](https://github.com/Josafath/Sorting-Algorithms/actions/workflows/v1-tests.yml/badge.svg)](https://github.com/Josafath/Sorting-Algorithms/actions/workflows/v1-tests.yml)
[![v2 tests](https://github.com/Josafath/Sorting-Algorithms/actions/workflows/v2-tests.yml/badge.svg)](https://github.com/Josafath/Sorting-Algorithms/actions/workflows/v2-tests.yml)
![Java](https://img.shields.io/badge/java-8%20%7C%2021-orange)
![Platforms](https://img.shields.io/badge/platforms-Windows%20%7C%20macOS%20%7C%20Linux-lightgrey)

A Java Swing desktop app to see how five classic sorting algorithms work: **Bubble Sort, Insertion Sort, QuickSort, Merge Sort and Shell Sort**. Type some numbers or open a `.txt` file, choose an algorithm and the app sorts them and shows how long it took.

<img src="v1/docs/preview.png" alt="The window: a text field for the numbers, the algorithm list, the Abrir Archivo and Ejecutar Algoritmo buttons, the execution time and the sorted result" width="100%">

## Why this project

To understand an algorithm I need to build something with it. I had been using Python for a long time and this was my way to practice Java while learning how these algorithms really work.

📐 **[How each algorithm works, with its complexity](v1/docs/ALGORITHMS.md)**

---

## v2: AlgoScope (in progress)

A rewrite in Java 21 that **animates** each algorithm step by step: every comparison, swap and write is recorded and can be played forwards and backwards, with live counters. Pathfinding on a grid (BFS, DFS, Dijkstra, A*) comes next.

<img src="v2/docs/sorting.png" alt="AlgoScope halfway through Quick Sort on 300 values: the left and right ends are already sorted, the middle is still unsorted, and one bar is highlighted while it is compared with the pivot" width="100%">

**Sorting:** Bubble, Insertion, Selection, Merge, Quick and Heap Sort, on random, sorted, reversed or few-unique inputs of 5 to 1,000 values. Changing the algorithm keeps the same input, so they can be compared on the same data.

**Controls:** Play/pause (Space), one step forward or back (Right/Left), back to the start (Home), a progress bar to jump anywhere, and a speed from 1 to about 30,000 steps per second.

Requires **JDK 21**. Inside the `v2` folder:

| | Windows | macOS / Linux |
|---|---|---|
| Open the app | `run.bat` | `./run.sh` |
| Run the tests | `test.bat` | `./test.sh` |

---

## v1

The original project, with its bugs fixed and nothing redesigned. The interface is in Spanish, as it was built.

### How to use it

- **Few numbers:** type them in the text field, separated by commas or spaces (`5, 3, 8, -1`), choose the algorithm and press **Ejecutar Algoritmo**. The sorted numbers appear on the right.
- **Many numbers:** press **Abrir Archivo** and choose a `.txt` file. The numbers can be on one line or on many lines, separated by commas, spaces or new lines. When the sort finishes, the app asks where to save the result, one number per line.

Sorting runs in the background, so the window does not freeze with big files.

### What was fixed

| Problem | Before | Now |
|---|---|---|
| Merge Sort from a file | Called with the wrong indexes: the file came out unsorted | Sorted, same code as the text field |
| QuickSort and Merge Sort from a file | The last number was missing from the saved file | Every number is saved |
| Reading a file | Only the last line was read | Every line is read |
| Cancel in the file dialog | `NullPointerException` | Nothing happens |
| Saving the result | Always `C:\Users\<user>\Desktop`, which fails on macOS and Linux, and appended to the old file | You choose where; the file is replaced, asking first if it exists |
| `3, 4` (with a space), letters, empty field | Crashed without telling the user | Clear error message with the wrong value |
| Bubble Sort and Insertion Sort | The array was `static`: two objects shared the same array | Each object has its own array |
| "Bubble Sort" | Was actually exchange sort | Real bubble sort, stops early when already sorted |
| QuickSort with already sorted data | `StackOverflowError` around 50,000 numbers | Middle pivot and bounded recursion: tested with 200,000 |
| Compiled files and personal NetBeans settings | Committed to the repository | Ignored with `.gitignore` |

### Requirements

- **JDK 8 or newer** (the project targets Java 8; tested with 8 and 21).
  - **Windows:** install [Eclipse Temurin](https://adoptium.net/) and check *Set JAVA_HOME* and *Add to PATH* in the installer.
  - **macOS:** install Temurin from the same page, or with Homebrew: `brew install --cask temurin`.
  - Check it with `java -version` and `javac -version`.
- **NetBeans** is optional: you can also run everything from a terminal.

### Run it

**With NetBeans:** *File → Open Project*, choose the `v1` folder and press *Run* (F6).

**From a terminal**, inside the `v1` folder:

Windows (Command Prompt or PowerShell):

```bat
mkdir out
javac -encoding UTF-8 -d out src\Algorithms\*.java src\Menu\*.java
java -cp out Menu.Principal
```

macOS and Linux:

```bash
mkdir -p out
javac -encoding UTF-8 -d out src/Algorithms/*.java src/Menu/*.java
java -cp out Menu.Principal
```

(`mkdir` is needed with Java 8, whose `javac` does not create the `out` folder; on Windows it only warns if the folder already exists.)

### Tests

94 checks with no external library, only the JDK: every algorithm against `Arrays.sort` (empty arrays, one element, repeated and negative values, `Integer.MIN_VALUE`/`MAX_VALUE`, random arrays), QuickSort with 200,000 sorted, reversed and equal numbers, and the reading and writing of numbers. They run on Windows, macOS and Linux with Java 8 and 21 on every push.

Inside the `v1` folder:

```bash
mkdir -p out
javac -encoding UTF-8 -d out src/Algorithms/*.java src/Menu/*.java test/tests/*.java
java -Djava.awt.headless=true -cp out tests.SortingTests
```

(On Windows use `\` instead of `/` in the paths.) In NetBeans: open `Test Packages → tests → SortingTests.java` and choose *Run File* (Shift+F6).

### Project structure

```
v1/
├── src/
│   ├── Algorithms/          The five algorithms, one class each
│   └── Menu/
│       ├── Principal.java   The window (built with the NetBeans GUI Builder) and the sort dispatcher
│       ├── Principal.form   NetBeans layout of the window
│       └── NumberIO.java    Reads, validates and writes the numbers
├── test/tests/SortingTests.java
├── docs/                    ALGORITHMS.md and the screenshot
├── nbproject/, build.xml    NetBeans / Ant project
└── manifest.mf
```
