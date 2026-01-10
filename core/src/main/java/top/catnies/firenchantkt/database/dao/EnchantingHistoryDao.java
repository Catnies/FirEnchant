package top.catnies.firenchantkt.database.dao;

import com.j256.ormlite.table.TableUtils;
import org.bukkit.Bukkit;
import top.catnies.firenchantkt.FirEnchantPlugin;
import top.catnies.firenchantkt.api.ServiceContainer;
import top.catnies.firenchantkt.database.FirConnectionManager;
import top.catnies.firenchantkt.database.entity.EnchantingHistoryTable;
import top.catnies.firenchantkt.util.MessageUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static top.catnies.firenchantkt.language.MessageConstants.DATABASE_TABLE_CREATE_ERROR;

public class EnchantingHistoryDao extends AbstractDao<EnchantingHistoryTable, Integer> implements EnchantingHistoryData {

    private static EnchantingHistoryDao instance;
    private static final int CURRENT_VERSION = 1;

    private EnchantingHistoryDao(){}
    public static EnchantingHistoryDao getInstance() {
        if (instance == null) {
            instance = new EnchantingHistoryDao();
            instance.createTable();
            ServiceContainer.INSTANCE.register(EnchantingHistoryData.class, instance);
        }
        return instance;
    }

    private void createTable() {
        try {
            TableUtils.createTableIfNotExists(FirConnectionManager.getInstance().getConnectionSource(), EnchantingHistoryTable.class);
        } catch (SQLException e) {
            // ORMLite 中如果表存在还是会重复创建 index 索引,所以需要忽略这个报错
            if (e.getCause() != null && e.getCause().toString().contains("Duplicate key name")) return;
            MessageUtils.INSTANCE.sendTranslatableComponent(Bukkit.getConsoleSender(), DATABASE_TABLE_CREATE_ERROR, EnchantingHistoryTable.class.getSimpleName());
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(FirEnchantPlugin.getInstance());
        }
    }


    @Override
    public void create(EnchantingHistoryTable historyData) {
        update(historyData, true);
    }

    @Override
    public void delete(int id) {
        EnchantingHistoryTable item = getById(id);
        if (item != null) {
            delete(item, true);
        }
    }

    @Override
    public void cleanupOldRecords(int daysToKeep) {
        try {
            long cutoffTime = System.currentTimeMillis() - (daysToKeep * 24L * 60L * 60L * 1000L);
            List<EnchantingHistoryTable> oldRecords = queryForList(getQueryBuilder()
                    .where()
                    .lt("timestamp", cutoffTime)
                    .queryBuilder());
            for (EnchantingHistoryTable record : oldRecords) {
                delete(record, true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<EnchantingHistoryTable> getByPlayer(UUID playerId) {
        try {
            return queryForList(getQueryBuilder()
                    .where()
                    .eq("playerId", playerId)
                    .queryBuilder());
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public List<EnchantingHistoryTable> getByPlayerRecent(UUID playerId, int limit) {
        try {
            if (limit <= 0) {
                return getByPlayer(playerId);
            }
            return queryForList(getQueryBuilder()
                    .where()
                    .eq("playerId", playerId)
                    .queryBuilder()
                    .orderBy("timestamp", false)
                    .limit((long) limit));
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public List<EnchantingHistoryTable> getByTimeRange(long startTime, long endTime, int limit) {
        try {
            if (limit <= 0) {
                return queryForList(getQueryBuilder()
                        .where()
                        .between("timestamp", startTime, endTime)
                        .queryBuilder()
                        .orderBy("timestamp", false));
            }
            return queryForList(getQueryBuilder()
                    .where()
                    .between("timestamp", startTime, endTime)
                    .queryBuilder()
                    .orderBy("timestamp", false)
                    .limit((long) limit));
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public List<EnchantingHistoryTable> getRecent(int limit) {
        if (limit <= 0) {
            return queryForList(getQueryBuilder()
                    .orderBy("timestamp", false));
        }
        return queryForList(getQueryBuilder()
                .orderBy("timestamp", false)
                .limit((long) limit));
    }
}
