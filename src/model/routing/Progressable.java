package model.routing;

public interface Progressable {

    void addProgressListener(IProgressListener listener);

    void removeProgressListener(IProgressListener listener);

    void cancelCalculation();

}