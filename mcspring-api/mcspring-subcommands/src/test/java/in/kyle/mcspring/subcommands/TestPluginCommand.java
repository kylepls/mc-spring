package in.kyle.mcspring.subcommands;

import in.kyle.mcspring.SpringSpigotSupport;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import in.kyle.api.bukkit.entity.TestPlayer;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = SpringSpigotSupport.class)
class TestPluginCommand {

    @Autowired
    TestConsole console;
    @Autowired
    TestPlayer sender;
    List<String> outputMessages;

    @BeforeEach
    void setup() {
        outputMessages = new ArrayList<>();
        sender.getMessages().subscribe(outputMessages::add);
    }

    @Test
    void testDirectExecutor() {
        class Test {

            void exec1(PluginCommand command) {
                command.on("test", this::handler1);
            }

            void exec2(PluginCommand command) {
                command.withString();
                command.on("test2", this::handler2);
            }

            void handler1(Player player) {
                player.sendMessage("Hello World");
            }

            void handler2(Player player, String string) {
                player.sendMessage(string);
            }
        }

        Test test = new Test();
        console.run(sender, "test", test::exec1);
        assertThat(outputMessages).containsExactly("Hello World");
        outputMessages.clear();
        console.run(sender, "hello test2", test::exec2);
        assertThat(outputMessages).containsExactly("hello");
    }

    @Test
    void testSenderArg() {
        class Test {
            void root(PluginCommand command) {
                command.then(this::exec);
            }

            void exec(CommandSender sender) {
                sender.sendMessage("Hello World");
            }
        }
        Test test = new Test();
        console.run(sender, "", test::root);
        assertThat(outputMessages).containsExactly("Hello World");
    }

    @Test
    void testCommandSingleSentenceArg() {
        class Test {
            void root(PluginCommand command) {
                command.withSentence();
                command.then(this::exec);
            }

            void exec(String sentence) {
                sender.sendMessage(sentence);
            }
        }
        Test test = new Test();
        console.run(sender, "Hello to you world", test::root);
        assertThat(outputMessages).containsExactly("Hello to you world");
    }

    @Test
    void testCommandIntArgs() {
        AtomicBoolean ran = new AtomicBoolean();
        class Test {
            void root(PluginCommand command) {
                command.withInt("error");
                command.withInt("error");
                command.withInt("error");
                command.then(this::exec);
            }

            void exec(int i1, int i2, int i3) {
                assertThat(i1).isEqualTo(1);
                assertThat(i2).isEqualTo(2);
                assertThat(i3).isEqualTo(3);
                ran.set(true);
            }
        }
        Test test = new Test();
        console.run(sender, "1 2 3", test::root);
        assertThat(outputMessages).isEmpty();
        assertThat(ran).isTrue();
    }

    @Test
    void testCommandBranching() {
        class Test {
            void root(PluginCommand command) {
                command.on("a", this::a);
            }

            void a(PluginCommand command) {
                command.on("b", this::b);
                command.on("c", this::c);
            }

            private void b(PluginCommand command) {
                command.then(this::exec);
            }

            private void c(PluginCommand command) {
            }

            void exec(CommandSender sender) {
                sender.sendMessage("Works");
            }
        }
        Test test = new Test();
        console.run(sender, "a b", test::root);
        assertThat(outputMessages).containsExactly("Works");
        outputMessages.clear();
        console.run(sender, "a c", test::root);
        assertThat(outputMessages).isEmpty();
    }

    @Test
    void testOtherwise() {
        class Test {
            void root(PluginCommand command) {
                command.on("a", this::a);
                command.otherwise("no subcommand at root");
            }

            void a(PluginCommand command) {
                command.withInt("error");
                command.otherwise("should run if int passed or missing arg");
            }
        }
        Test test = new Test();
        console.run(sender, "", test::root);
        assertThat(outputMessages).containsExactly("no subcommand at root");
        outputMessages.clear();

        console.run(sender, "a", test::root);
        assertThat(outputMessages).containsExactly("should run if int passed or missing arg");
        outputMessages.clear();

        console.run(sender, "a 2", test::root);
        assertThat(outputMessages).containsExactly("should run if int passed or missing arg");
    }

    @Test
    void testWithError() {
        class Test {
            void root(PluginCommand command) {
                command.withInt(s -> s + " is not an int");
            }
        }
        Test test = new Test();
        console.run(sender, "swag", test::root);
        assertThat(outputMessages).containsExactly("swag is not an int");
    }
}
