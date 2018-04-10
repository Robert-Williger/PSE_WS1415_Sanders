package adminTool.labeling.roadGraph;

import java.io.IOException;

public class Triangulator {

    private ProcessBuilder builder;

    public Triangulator(final String polyFilePath) {
        builder = new ProcessBuilder("triangle.exe", "-pDnQ", polyFilePath);
    }

    public void triangulate() throws IOException {
        Process process = builder.start();
        try {
            process.waitFor();
            if (process.exitValue() != 0)
                System.out.println("fail");
        } catch (InterruptedException e) {
            // should not happen
            e.printStackTrace();
        }
    }
}
