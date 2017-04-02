package model.routing;

import model.targets.AccessPoint;

public interface IAccessPointConverter {

    InterNode convert(AccessPoint point);

}
