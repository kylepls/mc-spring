package in.kyle.mcspring.command.registration;

import org.bukkit.command.CommandSender;

import lombok.Value;

public interface CommandResolver {
    
    Resolver makeResolver(Command command);
    
    @Value
    class Command {
        CommandSender sender;
        String[] args;
        String label;
    }
}
