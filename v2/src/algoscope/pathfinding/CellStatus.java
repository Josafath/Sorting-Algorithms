package algoscope.pathfinding;

/** What the search knows about a cell at a given moment. */
public enum CellStatus {

    /** Not reached yet. */
    UNSEEN,

    /** Discovered and waiting to be explored (in the queue, stack or priority queue). */
    FRONTIER,

    /** Explored: its neighbours have been looked at. */
    VISITED,

    /** Part of the path that was found. */
    PATH
}
