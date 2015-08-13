package model;

public interface IProgressListener {

    void progressDone(int progress);

    void stepCommenced(String step);

    void errorOccured(String message);

}