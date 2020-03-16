package in.kyle.mcspring.command.registration;

import com.google.common.annotations.VisibleForTesting;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import in.kyle.mcspring.command.Command;
import in.kyle.mcspring.util.SpringScanner;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.var;

@Component
@AllArgsConstructor
class CommandScanner implements ApplicationContextAware {
    
    private final SpringScanner scanner;
    private final CommandRegistration commandRegistration;
    
    @VisibleForTesting
    @Getter(AccessLevel.PACKAGE)
    private final Set<Method> registeredCommands = new HashSet<>();
    
    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        Map<Method, Object> scan = scanner.scanMethods(Command.class);
        for (var e : scan.entrySet()) {
            if (!registeredCommands.contains(e.getKey())) {
                Command command = e.getKey().getAnnotation(Command.class);
                commandRegistration.register(command, e.getKey(), e.getValue());
                registeredCommands.add(e.getKey());
            }
        }
    }
    
}
