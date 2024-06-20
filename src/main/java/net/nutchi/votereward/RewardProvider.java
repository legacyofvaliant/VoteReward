package net.nutchi.votereward;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;

@RequiredArgsConstructor
public class RewardProvider {
    private final VoteReward plugin;

    private @Nullable RewardItems rewardItems;
    private final List<RewardInventory> openedRewardInventories = new ArrayList<>();
    @Getter
    private final Map<UUID, PlayerRewardState> playerRewardStates = new HashMap<>();

    public void reloadRewardItems() {
        Month currentMonth = LocalDate.now().getMonth();
        if (rewardItems == null || rewardItems.hasExpired(currentMonth)) {
            plugin.getRewardStorage().loadRewardItems(currentMonth).ifPresent(items -> rewardItems = new RewardItems(currentMonth, items));
        }
    }

    public void setRewardItems(Month month, List<ItemStack> items) {
        rewardItems = new RewardItems(month, items);
    }

    public void open(Player player) {
        if (rewardItems != null) {
            RewardInventory rewardInventory = rewardItems.getInventory(plugin, player.getUniqueId());

            player.openInventory(rewardInventory.getInventory());

            openedRewardInventories.add(rewardInventory);
        }
    }

    public Optional<RewardInventory> getRewardInventory(Inventory inventory) {
        return openedRewardInventories.stream().filter(i -> i.getInventory().equals(inventory)).findAny();
    }

    public void removeRewardInventory(Inventory inventory) {
        openedRewardInventories.removeIf(i -> i.getInventory().equals(inventory));
    }

    public void onPlayerGetReward(UUID uuid) {
        if (playerRewardStates.containsKey(uuid)) {
            playerRewardStates.get(uuid).incrementAcquiredCount();
        } else {
            playerRewardStates.put(uuid, new PlayerRewardState());
            playerRewardStates.get(uuid).incrementAcquiredCount();
        }
    }

    public void onVote(UUID uuid) {
        if (playerRewardStates.containsKey(uuid)) {
            playerRewardStates.get(uuid).setVotedAfterLastAcquisition(true);
        }
    }
}
