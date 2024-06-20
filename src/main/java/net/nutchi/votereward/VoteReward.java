package net.nutchi.votereward;

import lombok.Getter;
import net.nutchi.votereward.listener.InventoryListener;
import net.nutchi.votereward.listener.VoteListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public final class VoteReward extends JavaPlugin {
    private final RewardEditor rewardEditor = new RewardEditor(this);
    private final RewardProvider rewardProvider = new RewardProvider(this);
    private RewardStorage rewardStorage;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        String mysqlHost = getConfig().getString("mysql.host");
        int mysqlPort = getConfig().getInt("mysql.port");
        String mySqlDatabase = getConfig().getString("mysql.database");
        String mysqlUsername = getConfig().getString("mysql.username");
        String mysqlPassword = getConfig().getString("mysql.password");
        String mysqlTablePrefix = getConfig().getString("mysql.table-prefix");

        PluginManager pm = getServer().getPluginManager();

        if (mysqlHost == null || mySqlDatabase == null || mysqlUsername == null || mysqlPassword == null) {
            getLogger().warning("Missing configuration values. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        rewardStorage = new RewardStorage(mysqlHost, mysqlPort, mySqlDatabase, mysqlUsername, mysqlPassword, mysqlTablePrefix);

        if (!rewardStorage.init()) {
            getLogger().warning("Failed to initialize storage. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getScheduler().runTaskTimer(this, rewardProvider::reloadRewardItems, 0, 20 * 60);

        pm.registerEvents(new InventoryListener(this), this);
        pm.registerEvents(new VoteListener(this), this);
    }

    @Override
    public void onDisable() {
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (command.getName().equals("votereward") && sender instanceof Player) {
            rewardProvider.open((Player) sender);

            return true;
        } else if (command.getName().equals("voterewardedit") && sender instanceof Player && args.length == 1) {
            try {
                rewardEditor.open((Player) sender, Month.valueOf(args[0]));

                return true;
            } catch (IllegalArgumentException e) {
                sender.sendMessage("Invalid month.");

                return false;
            }
        } else if (command.getName().equals("voterewardvote") && sender instanceof Player) {
            rewardProvider.onVote(((Player) sender).getUniqueId());
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (command.getName().equals("voterewardedit") && args.length == 1) {
            return Stream.of(Month.values()).map(Enum::name).filter(s -> s.startsWith(args[0])).collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
