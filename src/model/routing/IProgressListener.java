package model.routing;

public interface IProgressListener {

    void progressDone(int progress);

    void errorOccured(String message);

}