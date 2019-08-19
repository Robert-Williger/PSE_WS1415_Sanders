import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.ServiceLoader;

import scpsolver.lpsolver.LinearProgramSolver;
import scpsolver.lpsolver.SolverFactory;

public class OutTest {
    public static void main(String[] args) {
        PrintStream orig = System.out;
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {}
        }));
        System.out.println("Keine Ausgabe");
        System.setOut(orig);

        LinearProgramSolver s = null;
        ServiceLoader<LinearProgramSolver> loader = ServiceLoader.load(LinearProgramSolver.class);
        for (final LinearProgramSolver solver : loader) {
            s = solver;
            if (solver.getName().equals("LPSOLVE")) {
                break;
            }
        }
        for (final Iterator<LinearProgramSolver> it = loader.iterator(); it.hasNext();) {
            LinearProgramSolver lpSolver = it.next();
            System.out.println(lpSolver.getName());
        }
        final LinearProgramSolver solver = SolverFactory.getSolver("LPSOLVE");
        System.out.println(s.getName());

    }
}
