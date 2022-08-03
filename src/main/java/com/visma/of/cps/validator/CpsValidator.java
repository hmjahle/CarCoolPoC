package com.visma.of.cps.validator;

import com.visma.of.solverapi.Validator;
import com.visma.of.solverapi.ValidatorProvider;
import org.json.simple.JSONObject;

public class CpsValidator extends Validator {

    static {
        ValidatorProvider.registerValidator(CpsValidator.class);
    }

    public CpsValidator(){ super();}

    @Override
    public boolean validate(JSONObject openApiObject, JSONObject requestObject1) throws Exception {
        return validatePayload(openApiObject, requestObject1);
    }

}
