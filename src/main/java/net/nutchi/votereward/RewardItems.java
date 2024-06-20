package net.nutchi.votereward;

import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.time.Month;
import java.util.*;

@RequiredArgsConstructor
public class RewardItems {
    private final Month month;
    private final List<ItemStack> items;

    public RewardInventory getInventory(VoteReward plugin, UUID uuid) {
        int headItemSlot = 0;
        boolean acquired = false;

        if (plugin.getRewardProvider().getPlayerRewardStates().containsKey(uuid)) {
            PlayerRewardState playerRewardState = plugin.getRewardProvider().getPlayerRewardStates().get(uuid);

            headItemSlot = playerRewardState.getAcquiredCount();
            acquired = !playerRewardState.isVotedAfterLastAcquisition();
        }

        String title = String.format("%d月のJMS投票報酬 (%s)", month.getValue(), acquired ? "取得済み" : "未取得");
        Inventory inventory = plugin.getServer().createInventory(null, 54, title);

        for (int i = 0; i < items.size(); i++) {
            if (i >= headItemSlot) {
                inventory.setItem(i, items.get(i));
            } else {
                inventory.setItem(i, new ItemStack(Material.BARRIER));
            }
        }

        return new RewardInventory(this, inventory, acquired, headItemSlot);
    }

    public boolean hasExpired(Month currentMonth) {
        return currentMonth != month;
    }
}
