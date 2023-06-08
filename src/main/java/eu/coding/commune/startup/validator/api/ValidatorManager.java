package eu.coding.commune.startup.validator.api;

import eu.coding.commune.startup.validator.impl.FieldValidator;

import java.util.List;

public interface ValidatorManager {
    List<FieldValidator> getFieldValidators();
}
