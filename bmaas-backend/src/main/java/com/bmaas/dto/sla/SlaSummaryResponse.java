package com.bmaas.dto.sla;

public class SlaSummaryResponse {

    private long onTrack;
    private long atRisk;
    private long breached;
    private long total;

    public SlaSummaryResponse() {}

    public SlaSummaryResponse(long onTrack, long atRisk, long breached, long total) {
        this.onTrack = onTrack;
        this.atRisk = atRisk;
        this.breached = breached;
        this.total = total;
    }

    public long getOnTrack() { return onTrack; }
    public void setOnTrack(long onTrack) { this.onTrack = onTrack; }

    public long getAtRisk() { return atRisk; }
    public void setAtRisk(long atRisk) { this.atRisk = atRisk; }

    public long getBreached() { return breached; }
    public void setBreached(long breached) { this.breached = breached; }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
}
