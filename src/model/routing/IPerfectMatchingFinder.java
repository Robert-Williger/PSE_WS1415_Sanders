package model.routing;

import java.util.Set;

public interface IPerfectMatchingFinder {

    Set<Long> calculatePerfectMatching(final IGraph graph);

}
