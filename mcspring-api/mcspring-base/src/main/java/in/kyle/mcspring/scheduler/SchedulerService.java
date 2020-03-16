package in.kyle.mcspring.scheduler;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import in.kyle.mcspring.RequiresSpigot;
import lombok.RequiredArgsConstructor;

@Lazy
@Service
@RequiredArgsConstructor
@RequiresSpigot
public class SchedulerService {
    
    private final BukkitScheduler scheduler;
    private final Plugin plugin;
    
    public BukkitTask asyncTask(Runnable task) {
        return scheduler.runTaskAsynchronously(plugin, task);
    }
    
    public BukkitTask syncTask(Runnable task) {
        return scheduler.runTask(plugin, task);
    }
    
    public BukkitTask asyncDelayedTask(Runnable task, long delayTicks) {
        return scheduler.runTaskLaterAsynchronously(plugin, task, delayTicks);
    }
    
    public BukkitTask syncDelayedTask(Runnable task, long delayTicks) {
        return scheduler.runTaskLater(plugin, task, delayTicks);
    }
    
    public BukkitTask asyncRepeatingTask(Runnable task, long delayTicks, long periodTicks) {
        return scheduler.runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks);
    }
    
    public BukkitTask syncRepeatingTask(Runnable task, long delayTicks, long periodTicks) {
        return scheduler.runTaskTimer(plugin, task, delayTicks, periodTicks);
    }
    
    public void cancelTask(BukkitTask task) {
        scheduler.cancelTask(task.getTaskId());
    }
    
    public boolean isCurrentlyRunning(BukkitTask task) {
        return scheduler.isCurrentlyRunning(task.getTaskId());
    }
    
    public boolean isQueued(BukkitTask task) {
        return scheduler.isQueued(task.getTaskId());
    }
}
