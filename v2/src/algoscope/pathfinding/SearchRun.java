package algoscope.pathfinding;

import algoscope.engine.Timeline;

/**
 * A recorded search: the timeline to replay it step by step, and its result
 * (path, cost, cells visited) to show at the end.
 */
public record SearchRun(Timeline<SearchState, SearchStep> timeline, SearchResult result) {

    /**
     * Runs the algorithm on a copy of the grid and records it. Editing the grid
     * afterwards does not change the recording.
     */
    public static SearchRun record(PathfindingAlgorithm algorithm, Grid grid) {
        Grid copy = grid.copy();
        SearchTracker tracker = new SearchTracker(copy);
        algorithm.search(tracker);
        Timeline<SearchState, SearchStep> timeline = new Timeline<>(new SearchState(copy), tracker.steps());
        return new SearchRun(timeline, tracker.result());
    }
}
