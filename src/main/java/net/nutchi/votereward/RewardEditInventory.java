package net.nutchi.votereward;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.Inventory;

import java.time.Month;

@Getter
@RequiredArgsConstructor
public class RewardEditInventory {
    private final Inventory inventory;
    private final Month month;
}
