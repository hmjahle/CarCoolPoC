package evaluation.constraint;

public class StrictTimeWindowConstraint extends CustomCriteriaConstraint {

    public StrictTimeWindowConstraint() {
    super(i -> !i.isDepot() && i.isStrict(),
                i -> i.getStartOfServiceNextTask() + i.getVisit().getVisitDuration() <= i.getVisit().getEndTime());
    }
}
