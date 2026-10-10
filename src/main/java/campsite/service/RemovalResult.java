package campsite.service;

/**
 * The outcome of removing a plot: how many pending bookings could be
 * moved to another plot and how many had to be cancelled.
 */
public class RemovalResult {

    private final int movedCount;
    private final int cancelledCount;

    public RemovalResult(int movedCount, int cancelledCount) {
        this.movedCount = movedCount;
        this.cancelledCount = cancelledCount;
    }

    public int getMovedCount() { return movedCount; }
    public int getCancelledCount() { return cancelledCount; }

    @Override
    public String toString() {
        return movedCount + " booking(s) moved to another plot, "
                + cancelledCount + " booking(s) cancelled (no suitable plot)";
    }
}
