package net.nutchi.votereward;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.Inventory;

@Getter
@RequiredArgsConstructor
public class RewardInventory {
    private final RewardItems rewardItems;
    private final Inventory inventory;
    private final boolean acquired;
    private final int headItemSlot;
}
