package adminTool.labeling.roadGraph.triangulation;

import java.io.IOException;

class TriangleAdapter {

    private ProcessBuilder builder;

    public TriangleAdapter(final String polyFilePath) {// use the S switch to set max steiner points?
        builder = new ProcessBuilder("triangle.exe", "-pDnQPB", polyFilePath);
    }

    public void triangulate() throws IOException {
        Process process = builder.start();
        try {
            process.waitFor();
        } catch (final InterruptedException e) {
            // should not happen
            e.printStackTrace();
        }
    }
}
