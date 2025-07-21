package top.catnies.firenchantkt.database.entity;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.saicone.rtag.item.ItemTagStream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.catnies.firenchantkt.api.FirEnchantAPI;

import java.util.Arrays;
import java.util.UUID;

/**
 * 道具修复记录表
 * 用于记录玩家正在修复的道具信息
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DatabaseTable(tableName = "firenchant_item_repair")
public class ItemRepairTable {


    public ItemRepairTable(UUID playerId, byte[] itemData, long duration) {
        this.playerId = playerId;
        this.itemData = itemData;
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
    }
    
    @DatabaseField(generatedId = true)
    public int id; // A23: 改为public
    
    @DatabaseField(canBeNull = false, index = true, dataType = DataType.UUID)
    private UUID playerId;
    
    @DatabaseField(canBeNull = false, dataType = DataType.BYTE_ARRAY)
    public byte[] itemData; // 序列化的破损物品数据, A23: 改为public

    @DatabaseField(canBeNull = false, dataType = DataType.LONG)
    private long startTime;
    
    @DatabaseField(canBeNull = false, dataType = DataType.LONG)
    private long duration; // 修复所需时间（毫秒）
    
    @DatabaseField(canBeNull = false, defaultValue = "false", dataType = DataType.BOOLEAN)
    public boolean received = false; // A23: 改为public
    
    /**
     * 检查修复是否已经完成
     * @return 如果当前时间已超过修复完成时间则返回true
     */
    public boolean isCompleted() {
        return System.currentTimeMillis() >= (startTime + duration);
    }

    /**
     * 获取剩余时间（毫秒）
     * @return 剩余修复时间，如果已完成则返回0
     */
    public long getRemainingTime() {
        long remaining = (startTime + duration) - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    /**
     * 获取破损状态的物品
     */
    public ItemStack getBrokenItem() {
        return ItemTagStream.INSTANCE.fromBytes(itemData);
    }

    /**
     * 获取修复完成的物品
     */
    public ItemStack getRepairedItem() {
        return FirEnchantAPI.INSTANCE.repairBrokenGear(getBrokenItem());
    }

    @Override
    public String toString() {
        return "id:"+id+",player:"+playerId+",startTime:"+startTime+",duration:"+duration+",received:"+received+",itemData:"+Arrays.toString(itemData);
    }
}
