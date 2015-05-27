package adminTool;

import java.io.File;

public interface AdminInterface {

    File askOSMFile();

    File askOutputPath();

    void complete();

}