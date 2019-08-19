package adminTool.labeling.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.PrimitiveIterator;

import adminTool.PointLocator;
import adminTool.elements.CutPerformer;
import adminTool.elements.CutPerformer.Cut;
import adminTool.elements.MultiElement;
import adminTool.elements.PointAccess;
import adminTool.labeling.Embedding;
import adminTool.labeling.INameInfo;
import adminTool.labeling.Label;
import adminTool.labeling.LabelPath;
import adminTool.labeling.roadMap.LabelSection;
import adminTool.labeling.roadMap.RoadMap;
import adminTool.util.ElementAdapter;
import util.IntList;

public class LabelCreator {
    private final Embedding embedding;
    private final PointAccess points;
    private final INameInfo nameInfo;
    private final CutPerformer cutPerformer;
    private final PointLocator locator;
    private final List<Cut> cuts;

    public LabelCreator(RoadMap roadMap, PointAccess points) {
        super();
        this.embedding = roadMap.getEmbedding();
        this.points = points;
        this.nameInfo = roadMap.getNameInfo();
        this.locator = new PointLocator(points);
        this.cutPerformer = new CutPerformer();
        this.cuts = new ArrayList<Cut>(2);
        for (int i = 0; i < 2; ++i)
            this.cuts.add(new Cut(0, 0, 0));
    }

    public Label createLabel(final LabelPath path) {
        final IntList fullPath = new IntList();

        final IntList edgePath = path.getEdgePath();
        if (edgePath.size() > 1) {
            fullPath.addAll(createPartialPath(edgePath.get(0), path.getHeadPosition(), 1));
            for (int i = 1; i < edgePath.size() - 1; ++i)
                appendSection(fullPath, embedding.getSection(edgePath.get(i)).iterator());

            appendSection(fullPath,
                    createPartialPath(edgePath.get(edgePath.size() - 1), path.getTailPosition(), 0).iterator());
        } else {
            final MultiElement section = embedding.getSection(edgePath.get(0));
            cuts.set(0, createCut(section, path.getHeadPosition()));
            cuts.set(1, createCut(section, path.getTailPosition()));
            fullPath.addAll(cutPerformer.performCuts(section, cuts).get(1));
        }

        final LabelSection section = embedding.getSection(path.getEdgePath().get(0));
        return new Label(fullPath, section.getType(), nameInfo.getName(section.getRoadId()));
    }

    private IntList createPartialPath(final int edge, final double length, final int piece) {
        final MultiElement section = embedding.getSection(edge);
        final Cut cut = createCut(section, length);

        return cutPerformer.performCut(section, cut).get(piece);
    }

    private Cut createCut(final MultiElement head, final double length) {
        final ElementAdapter adapter = new ElementAdapter(points);
        adapter.setMultiElement(head);
        locator.locate(head, length);
        points.addPoint(locator.getX(), locator.getY());
        return new Cut(points.size() - 1, locator.getSegment(), locator.getOffset());
    }

    private void appendSection(final IntList fullPath, final PrimitiveIterator.OfInt sectionIt) {
        sectionIt.next();
        while (sectionIt.hasNext())
            fullPath.add(sectionIt.nextInt());
    }
}
