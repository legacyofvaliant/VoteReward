package net.nutchi.votereward;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class PlayerRewardState {
    private int acquiredCount = 0;
    private boolean votedAfterLastAcquisition = false;

    public void incrementAcquiredCount() {
        acquiredCount++;
    }
}
