package net.nutchi.votereward.listener;

import lombok.RequiredArgsConstructor;
import net.nutchi.votereward.VoteReward;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

@RequiredArgsConstructor
public class InventoryListener implements Listener {
    private final VoteReward plugin;

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        plugin.getRewardProvider().getRewardInventory(event.getInventory()).ifPresent(rewardInventory -> {
            event.setCancelled(true);

            if (!rewardInventory.isAcquired() && event.getSlot() == rewardInventory.getHeadItemSlot() && event.getCurrentItem() != null) {
                Player player = (Player) event.getWhoClicked();

                if (player.getInventory().firstEmpty() != -1) {
                    player.getInventory().addItem(event.getCurrentItem());
                    event.getInventory().setItem(event.getSlot(), null);
                    player.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);
                    plugin.getRewardProvider().onPlayerGetReward(player.getUniqueId());
                }
            }
        });
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        plugin.getRewardEditor().save(event.getInventory());
        plugin.getRewardProvider().removeRewardInventory(event.getInventory());
    }
}
