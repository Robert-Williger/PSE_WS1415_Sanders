import model.Application;
import model.IApplication;
import view.ApplicationView;
import view.IApplicationView;
import controller.ApplicationController;

public class Main {

    public static void main(final String[] args) {
        final IApplication model = new Application();
        final IApplicationView view = new ApplicationView(model);
        new ApplicationController(view, model);
    }

}
