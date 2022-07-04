package evaluation;

import java.util.function.Function;
import evaluation.info.RouteEvaluationInfoAbstract;

public class CustomCriteriaAbstract {

    private final Function<RouteEvaluationInfoAbstract, Boolean> criteriaFunction;

    public CustomCriteriaAbstract(Function<RouteEvaluationInfoAbstract, Boolean> criteriaFunction) {
        this.criteriaFunction = criteriaFunction;
    }

    protected boolean criteriaIsFulfilled(RouteEvaluationInfoAbstract info) {
        return criteriaFunction.apply(info);
    }
    
}
