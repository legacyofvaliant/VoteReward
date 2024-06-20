package net.nutchi.votereward;

import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class RewardStorage {
    private final String mysqlHost;
    private final int mysqlPort;
    private final String mysqlDatabase;
    private final String mysqlUsername;
    private final String mysqlPassword;
    private final String mysqlTablePrefix;

    public boolean init() {
        try {
            Connection connection = getConnection();

            String query1 = String.format(
                    "CREATE TABLE IF NOT EXISTS %srewards (" +
                    "id int(10) NOT NULL PRIMARY KEY," +
                    "month int(2) NOT NULL," +
                    "items text NOT NULL" +
                    ")", mysqlTablePrefix);
            PreparedStatement statement1 = connection.prepareStatement(query1);
            statement1.executeUpdate();
            statement1.close();

            String query2 = String.format(
                    "CREATE TABLE IF NOT EXISTS %splayers (" +
                    "uuid varchar(36) NOT NULL PRIMARY KEY," +
                    "acquired_count int(10) NOT NULL," +
                    "voted_after_last_acquisition tinyint(1) NOT NULL" +
                    ")", mysqlTablePrefix);
            PreparedStatement statement2 = connection.prepareStatement(query2);
            statement2.executeUpdate();
            statement2.close();

            connection.close();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();

            return false;
        }
    }

    public Optional<PlayerRewardState> loadPlayerRewardState(UUID uuid) {
        try {
            Connection connection = getConnection();

            PlayerRewardState playerRewardState = new PlayerRewardState();

            String query = String.format("SELECT acquired_count, voted_after_last_acquisition FROM %splayers WHERE uuid = ?", mysqlTablePrefix);
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                playerRewardState.setAcquiredCount(resultSet.getInt("acquired_count"));
                playerRewardState.setVotedAfterLastAcquisition(resultSet.getBoolean("voted_after_last_acquisition"));
            }

            statement.close();

            connection.close();

            return Optional.of(playerRewardState);
        } catch (SQLException e) {
            e.printStackTrace();

            return Optional.empty();
        }
    }

    public void savePlayerRewardState(UUID uuid, PlayerRewardState playerRewardState) {
        try {
            Connection connection = getConnection();

            String query = String.format("REPLACE INTO %splayers (uuid, acquired_count, voted_after_last_acquisition) VALUES (?, ?, ?)", mysqlTablePrefix);
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, uuid.toString());
            statement.setInt(2, playerRewardState.getAcquiredCount());
            statement.setBoolean(3, playerRewardState.isVotedAfterLastAcquisition());
            statement.executeUpdate();

            statement.close();

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Optional<List<ItemStack>> loadRewardItems(Month month) {
        try {
            Connection connection = getConnection();

            List<ItemStack> items = new ArrayList<>();

            String query = String.format("SELECT items FROM %srewards WHERE month = ?", mysqlTablePrefix);
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, month.getValue());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String itemsString = resultSet.getString("items");
                convertStringToItemStacks(itemsString).ifPresent(items::addAll);
            }

            statement.close();

            connection.close();

            return Optional.of(items);
        } catch (SQLException e) {
            e.printStackTrace();

            return Optional.empty();
        }
    }

    public void saveRewardItems(Month month, List<ItemStack> items) {
        convertItemStacksToString(items).ifPresent(itemsString -> {
            try {
                Connection connection = getConnection();

                String query = String.format("REPLACE INTO %srewards (id, month, items) VALUES (1, ?, ?)", mysqlTablePrefix);
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, month.getValue());
                statement.setString(2, itemsString);
                statement.executeUpdate();

                statement.close();

                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public Optional<List<ItemStack>> convertStringToItemStacks(String itemsString) {

        try (
                ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(itemsString));
                BukkitObjectInputStream bukkitInputStream = new BukkitObjectInputStream(inputStream)
        ) {
            List<ItemStack> items = new ArrayList<>();

            int size = bukkitInputStream.readInt();

            for (int i = 0; i < size; i++) {
                items.add((ItemStack) bukkitInputStream.readObject());
            }

            return Optional.of(items);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public Optional<String> convertItemStacksToString(List<ItemStack> items) {
        try (
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                BukkitObjectOutputStream bukkitOutputStream = new BukkitObjectOutputStream(outputStream)
        ) {
            bukkitOutputStream.writeInt(items.size());

            for (ItemStack item : items) {
                bukkitOutputStream.writeObject(item);
            }

            return Optional.of(Base64Coder.encodeLines(outputStream.toByteArray()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    private Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + mysqlDatabase;
        return DriverManager.getConnection(url, mysqlUsername, mysqlPassword);
    }
}
