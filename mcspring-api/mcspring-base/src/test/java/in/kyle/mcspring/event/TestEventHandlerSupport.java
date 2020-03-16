package in.kyle.mcspring.event;

import org.bukkit.event.EventHandler;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;

import in.kyle.mcspring.util.SpringScanner;

import static org.mockito.Mockito.*;

@SpringBootTest
@Import({TestEventHandlerSupport.Config.class, SpringScanner.class, EventHandlerSupport.class,
         TestEventHandlerSupport.TestEvent.class})
class TestEventHandlerSupport {
    
    @Autowired
    EventHandlerSupport support;
    @Autowired
    EventService eventService;
    
    @Test
    void testScanAndRegister() {
        // should scan all beans and register methods with @EventHandler
        Method method = TestEvent.class.getDeclaredMethods()[0];
        verify(eventService, times(1)).registerEvent(eq(method), any());
    }
    
    static class TestEvent {
        @EventHandler
        public void test() {
        }
    }
    
    @SpringBootConfiguration
    @Order(1) // Need this config to load first
    static class Config {
        @Bean
        EventService eventService() {
            return mock(EventService.class);
        }
        
        @Bean
        Plugin plugin() {
            return mock(Plugin.class);
        }
    }
}