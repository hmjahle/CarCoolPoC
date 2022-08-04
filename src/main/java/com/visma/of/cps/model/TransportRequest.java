package com.visma.of.cps.model;

/**
 * Aggregated visit-node used when trying to insert a pick-up and drop-off into a
 * motorized shift. This is used to enforce that the pick-up comes before the drop-off in the
 * motorized shift.
 *
 * Transport requests are always from a Pick-up to a Drop-of
 */
public class TransportRequest implements IVisit {
    private int visitDuration;
    private Integer timeWindowStart;
    private Integer timeWindowEnd;
    private  boolean isSynced;

    // Transport Request specific variables
    private Visit pickUp;
    private Visit dropOff;

    public TransportRequest(Visit pickUp, Visit dropOff, int travelTimeBetween) {
       this.timeWindowStart = pickUp.getTimeWindowStart();
       this.visitDuration = pickUp.getVisitDuration() + travelTimeBetween + dropOff.getVisitDuration();
       this.timeWindowEnd = dropOff.getTimeWindowEnd() - this.visitDuration;

       if (timeWindowEnd < timeWindowStart) {
           throw new IllegalArgumentException(
                   "Time window end cannot be less than time window start, check time" +
                   "windows and traveltime between PickUp and DropOff."
           );
       }
    }

    @Override
    public int getVisitDuration() {
        return this.visitDuration;
    }

    @Override
    public Integer getTimeWindowStart() {
        return this.timeWindowStart;
    }

    @Override
    public Integer getTimeWindowEnd() {
        return this.timeWindowEnd;
    }

    @Override
    public boolean isSynced() {
        return this.isSynced;
    }
}
