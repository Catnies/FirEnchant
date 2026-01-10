package top.catnies.firenchantkt.database.dao;

import com.j256.ormlite.table.TableUtils;
import org.bukkit.Bukkit;
import top.catnies.firenchantkt.FirEnchantPlugin;
import top.catnies.firenchantkt.api.ServiceContainer;
import top.catnies.firenchantkt.database.FirConnectionManager;
import top.catnies.firenchantkt.database.entity.AnvilEnchantLogTable;
import top.catnies.firenchantkt.util.MessageUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static top.catnies.firenchantkt.language.MessageConstants.DATABASE_TABLE_CREATE_ERROR;

public class EnchantLogDao extends AbstractDao<AnvilEnchantLogTable, Integer> implements EnchantLogData {

    private static EnchantLogDao instance;
    private static final int CURRENT_VERSION = 1;

    private EnchantLogDao(){}
    public static EnchantLogDao getInstance() {
        if (instance == null) {
            instance = new EnchantLogDao();
            instance.createTable();
            ServiceContainer.INSTANCE.register(EnchantLogData.class, instance);
        }
        return instance;
    }

    private void createTable() {
        try {
            TableUtils.createTableIfNotExists(FirConnectionManager.getInstance().getConnectionSource(), AnvilEnchantLogTable.class);
        } catch (SQLException e) {
            // ORMLite 中如果表存在还是会重复创建 index 索引,所以需要忽略这个报错
            if (e.getCause() != null && e.getCause().toString().contains("Duplicate key name")) return;
            MessageUtils.INSTANCE.sendTranslatableComponent(Bukkit.getConsoleSender(), DATABASE_TABLE_CREATE_ERROR, AnvilEnchantLogTable.class.getSimpleName());
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(FirEnchantPlugin.getInstance());
        }
    }

    @Override
    public void insert(AnvilEnchantLogTable anvilEnchantLogTable) {
        update(anvilEnchantLogTable, true);
    }

    @Override
    public void delete(int id) {
        AnvilEnchantLogTable item = getById(id);
        if (item != null) {
            delete(item, true);
        }
    }

    @Override
    public void cleanupOldRecords(int daysToKeep) {
        try {
            long cutoffTime = System.currentTimeMillis() - (daysToKeep * 24L * 60L * 60L * 1000L);
            List<AnvilEnchantLogTable> oldRecords = queryForList(getQueryBuilder()
                    .where()
                    .lt("timestamp", cutoffTime)
                    .queryBuilder());
            for (AnvilEnchantLogTable record : oldRecords) {
                delete(record, true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<AnvilEnchantLogTable> getAllList() {
        return getList();
    }

    @Override
    public List<AnvilEnchantLogTable> getCountList(int max) {
        if (max <= 0) {
            return getAllList();
        }
        return queryForList(getQueryBuilder()
                .orderBy("timestamp", false)
                .limit((long) max));
    }

    @Override
    public List<AnvilEnchantLogTable> getByPlayer(UUID uuid) {
        try {
            return queryForList(getQueryBuilder()
                    .where()
                    .eq("player", uuid)
                    .queryBuilder());
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public List<AnvilEnchantLogTable> getByPlayerRecent(UUID uuid, int max) {
        try {
            if (max <= 0) {
                return getByPlayer(uuid);
            }
            return queryForList(getQueryBuilder()
                    .where()
                    .eq("player", uuid)
                    .queryBuilder()
                    .orderBy("timestamp", false)
                    .limit((long) max));
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public List<AnvilEnchantLogTable> getByPlayerAndEnchantment(UUID uuid, String enchantment) {
        try {
            return queryForList(getQueryBuilder()
                    .where()
                    .eq("player", uuid)
                    .and()
                    .eq("usedEnchantment", enchantment)
                    .queryBuilder());
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public List<AnvilEnchantLogTable> getByPlayerAndEnchantmentRecent(UUID uuid, String enchantment, int max) {
        try {
            if (max <= 0) {
                return getByPlayerAndEnchantment(uuid, enchantment);
            }
            return queryForList(getQueryBuilder()
                    .where()
                    .eq("player", uuid)
                    .and()
                    .eq("usedEnchantment", enchantment)
                    .queryBuilder()
                    .orderBy("timestamp", false)
                    .limit((long) max));
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }
}
