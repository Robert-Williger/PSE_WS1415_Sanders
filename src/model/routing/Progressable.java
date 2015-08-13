package model.routing;

import model.IProgressListener;

public interface Progressable {

    void addProgressListener(IProgressListener listener);

    void removeProgressListener(IProgressListener listener);

    void cancelCalculation();

}