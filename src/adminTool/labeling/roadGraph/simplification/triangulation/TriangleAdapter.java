package adminTool.labeling.roadGraph.simplification.triangulation;

import java.io.IOException;

public class TriangleAdapter {

    private ProcessBuilder builder;

    public TriangleAdapter(final String polyFilePath) {// use the S switch to set max steiner points?
        builder = new ProcessBuilder("triangle.exe", "-pDnQPB", polyFilePath);
    }

    public void triangulate() throws IOException {
        Process process = builder.start();
        try {
            process.waitFor();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }
}
