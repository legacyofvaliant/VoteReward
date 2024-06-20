package net.nutchi.votereward.listener;

import com.vexsoftware.votifier.model.VotifierEvent;
import lombok.RequiredArgsConstructor;
import net.nutchi.votereward.VoteReward;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@RequiredArgsConstructor
public class VoteListener implements Listener {
    private final VoteReward plugin;

    @EventHandler
    public void onVotifier(VotifierEvent event) {
        Player player = plugin.getServer().getPlayer(event.getVote().getUsername());
        if (player != null) {
            plugin.getRewardProvider().onVote(player.getUniqueId());
            player.sendMessage("Thank you for voting!");
        }
    }
}
