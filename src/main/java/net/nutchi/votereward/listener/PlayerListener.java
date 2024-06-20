package net.nutchi.votereward.listener;

import lombok.RequiredArgsConstructor;
import net.nutchi.votereward.VoteReward;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@RequiredArgsConstructor
public class PlayerListener implements Listener {
    private final VoteReward plugin;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getRewardStorage().loadPlayerRewardState(event.getPlayer().getUniqueId()).ifPresent(playerRewardState -> plugin.getRewardProvider().getPlayerRewardStates().put(event.getPlayer().getUniqueId(), playerRewardState));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (plugin.getRewardProvider().getPlayerRewardStates().containsKey(event.getPlayer().getUniqueId())) {
            plugin.getRewardStorage().savePlayerRewardState(event.getPlayer().getUniqueId(), plugin.getRewardProvider().getPlayerRewardStates().get(event.getPlayer().getUniqueId()));
            plugin.getRewardProvider().getPlayerRewardStates().remove(event.getPlayer().getUniqueId());
        }
    }
}
