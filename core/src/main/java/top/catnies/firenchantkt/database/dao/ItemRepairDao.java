package top.catnies.firenchantkt.database.dao;

import com.j256.ormlite.table.TableUtils;
import org.bukkit.Bukkit;
import top.catnies.firenchantkt.FirEnchantPlugin;
import top.catnies.firenchantkt.api.ServiceContainer;
import top.catnies.firenchantkt.database.FirConnectionManager;
import top.catnies.firenchantkt.database.entity.ItemRepairTable;
import top.catnies.firenchantkt.util.MessageUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static top.catnies.firenchantkt.language.MessageConstants.DATABASE_TABLE_CREATE_ERROR;

public class ItemRepairDao extends AbstractDao<ItemRepairTable, Integer> implements ItemRepairData {

    private static ItemRepairDao instance;
    private static final int CURRENT_VERSION = 1;

    private ItemRepairDao(){}
    public static ItemRepairDao getInstance() {
        if (instance == null) {
            instance = new ItemRepairDao();
            instance.createTable();
            ServiceContainer.INSTANCE.register(ItemRepairData.class, instance);
        }
        return instance;
    }

    private void createTable() {
        try {
            TableUtils.createTableIfNotExists(FirConnectionManager.getInstance().getConnectionSource(), ItemRepairTable.class);
        } catch (SQLException e) {
            // ORMLite 中如果表存在还是会重复创建 index 索引,所以需要忽略这个报错
            if (e.getCause() != null && e.getCause().toString().contains("Duplicate key name")) return;
            MessageUtils.INSTANCE.sendTranslatableComponent(Bukkit.getConsoleSender(), DATABASE_TABLE_CREATE_ERROR, ItemRepairTable.class.getSimpleName());
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(FirEnchantPlugin.getInstance());
        }
    }

    @Override
    public void insert(ItemRepairTable repairData) {
        update(repairData);
    }

    @Override
    public void remove(ItemRepairTable repairData) {
        delete(repairData, true);
    }

    @Override
    public void markAsReceived(int id) {
        ItemRepairTable item = getById(id);
        if (item != null) {
            item.setReceived(true);
            update(item, true);
        }
    }

    @Override
    public List<ItemRepairTable> getAllList() {
        return super.getList();
    }

    @Override
    public List<ItemRepairTable> getAllActiveList() {
        try {
            return queryForList(getQueryBuilder()
                    .where()
                    .eq("received", false)
                    .and()
                    .raw("(startTime + duration) > " + System.currentTimeMillis())
                    .queryBuilder());
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public List<ItemRepairTable> getAllCompletedList() {
        try {
            return queryForList(getQueryBuilder()
                    .where()
                    .eq("received", false)
                    .and()
                    .raw("(startTime + duration) <= " + System.currentTimeMillis())
                    .queryBuilder());
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public List<ItemRepairTable> getAllReceivedList() {
        try {
            return queryForList(getQueryBuilder()
                    .where()
                    .eq("received", true)
                    .queryBuilder());
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public List<ItemRepairTable> getAllActiveAndCompletedList() {
        try {
            return queryForList(getQueryBuilder()
                    .where()
                    .eq("received", false)
                    .queryBuilder());
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public List<ItemRepairTable> getByPlayer(UUID playerId) {
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
    public List<ItemRepairTable> getByPlayerActive(UUID playerId) {
        try {
            return queryForList(getQueryBuilder()
                    .where()
                    .eq("playerId", playerId)
                    .and()
                    .eq("received", false)
                    .and()
                    .raw("(startTime + duration) > " + System.currentTimeMillis())
                    .queryBuilder());
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public List<ItemRepairTable> getByPlayerCompleted(UUID playerId) {
        try {
            return queryForList(getQueryBuilder()
                    .where()
                    .eq("playerId", playerId)
                    .and()
                    .eq("received", false)
                    .and()
                    .raw("(startTime + duration) <= " + System.currentTimeMillis())
                    .queryBuilder());
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public List<ItemRepairTable> getByPlayerReceived(UUID playerId) {
        try {
            return queryForList(getQueryBuilder()
                    .where()
                    .eq("playerId", playerId)
                    .and()
                    .eq("received", true)
                    .queryBuilder());
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public List<ItemRepairTable> getByPlayerActiveAndCompletedList(UUID playerId) {
        try {
            return queryForList(getQueryBuilder()
                    .where()
                    .eq("playerId", playerId)
                    .and()
                    .eq("received", false)
                    .queryBuilder());
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }
}
