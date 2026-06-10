package top.catnies.firenchantkt.util

import cn.chengzhimeow.ccscheduler.scheduler.CCScheduler
import top.catnies.firenchantkt.FirEnchantPlugin
import java.util.concurrent.atomic.AtomicInteger

object TaskUtils {

    val plugin = FirEnchantPlugin.instance

    // 运行异步任务
    fun runAsyncTask(task: () -> Unit) {
        CCScheduler.getInstance().asyncScheduler.runTask(plugin, task)
    }

    // 运行异步任务并同步执行回调
    fun runAsyncTaskWithSyncCallback(async:() -> Unit, callback: () -> Unit, delay: Long = 0) {
        CCScheduler.getInstance().asyncScheduler.runTaskLater(plugin, delay) { ->
            async()
            CCScheduler.getInstance().globalRegionScheduler.runTask(plugin, callback)
        }
    }

    // 运行异步并行任务
    fun runAsyncTasksLater(vararg tasks: () -> Unit, delay: Long = 0) {
        tasks.forEach { CCScheduler.getInstance().asyncScheduler.runTaskLater(plugin, it, delay) }
    }

    // 运行延迟同步任务
    fun runTaskLater(task: () -> Unit, delay: Long = 0) {
        CCScheduler.getInstance().globalRegionScheduler.runTaskLater(plugin, task, delay)
    }

    // 运行多个异步任务并在全部完成后执行同步回调
    fun runAsyncTasksWithSyncCallback(vararg tasks: () -> Unit, callback: () -> Unit, delay: Long = 0) {
        if (tasks.isEmpty()) {
            // 没任务时直接回调
            CCScheduler.getInstance().globalRegionScheduler.runTask(plugin, callback)
            return
        }

        val counter = AtomicInteger(tasks.size)
        tasks.forEach { task ->
            CCScheduler.getInstance().asyncScheduler.runTaskLater(plugin, delay) { ->
                try {
                    task()
                }
                finally {
                    // 计数器减一并检查是否所有任务完成
                    if (counter.decrementAndGet() == 0) {
                        CCScheduler.getInstance().globalRegionScheduler.runTask(plugin, callback)
                    }
                }
            }
        }
    }
}