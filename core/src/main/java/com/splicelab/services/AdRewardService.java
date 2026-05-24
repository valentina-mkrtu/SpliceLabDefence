package com.splicelab.services;

public final class AdRewardService {
    private com.splicelab.telemetry.TelemetryBus telemetry;

    public void setTelemetry(com.splicelab.telemetry.TelemetryBus telemetry) {
        this.telemetry = telemetry;
    }

    // Stub hook: call this when a placement is viewed/watched.
    public void onPlacementViewed(String placementId) {
        if (telemetry == null) return;
        telemetry.track("ad_placement_viewed", java.util.Map.of("placementId", placementId == null ? "" : placementId));
    }

    public boolean isRewardedAdAvailable() {
        return true;
    }

    public boolean grantTubeInstantCooldown() {
        return true;
    }

    public boolean grantExtraIngredient() {
        return true;
    }

    public boolean grantDoubleReward() {
        return true;
    }

    // UI-only stub: immediately grants reward.
    public void showRewardedAd(Runnable onReward) {
        onPlacementViewed("rewarded_double");
        if (onReward != null) onReward.run();
    }
}
