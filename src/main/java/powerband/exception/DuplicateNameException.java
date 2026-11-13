package powerband.exception;

import powerband.constants.Constants;

//El nombre de la grabación ya existe: el enunciado dice que se rechaza y se pide otro
public class DuplicateNameException extends PowerBandException {

    public DuplicateNameException(String name) {
        super(Constants.MSG_DUPLICATE + name);
    }
}
