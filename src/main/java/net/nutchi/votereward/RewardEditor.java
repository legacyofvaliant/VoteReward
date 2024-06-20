package net.nutchi.votereward;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class RewardEditor {
    private final VoteReward plugin;

    private final List<RewardEditInventory> openedRewardEditInventories = new ArrayList<>();

    public void open(Player player, Month month) {
        plugin.getRewardStorage().loadRewardItems(month).ifPresent(items -> {
            String title = String.format("[編集中] %d月のJMS投票報酬", month.getValue());
            Inventory inventory = plugin.getServer().createInventory(null, 54, title);

            for (int i = 0; i < items.size(); i++) {
                inventory.setItem(i, items.get(i));
            }

            openedRewardEditInventories.add(new RewardEditInventory(inventory, month));

            player.openInventory(inventory);
        });
    }

    public void save(Inventory inventory) {
        openedRewardEditInventories.stream().filter(i -> i.getInventory().equals(inventory)).findAny().ifPresent(i -> {
            List<ItemStack> items = Arrays.asList(inventory.getContents());
            plugin.getRewardStorage().saveRewardItems(i.getMonth(), items);
            plugin.getRewardProvider().setRewardItems(i.getMonth(), items);
        });

        openedRewardEditInventories.removeIf(i -> i.getInventory().equals(inventory));
    }
}
