package in.kyle.mcspring.subcommands;

import lombok.SneakyThrows;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.stream.Stream;

public interface Executors {

    @SneakyThrows
    default SerializedLambda getSerializedLambda() {
        Method writeReplace = this.getClass().getDeclaredMethod("writeReplace");
        writeReplace.setAccessible(true);
        return (SerializedLambda) writeReplace.invoke(this);
    }

    @SneakyThrows
    default Method getMethod(int argCount) {
        SerializedLambda sl = getSerializedLambda();
        String methodName = sl.getImplMethodName();
        Class<?> clazz = Class.forName(sl.getImplClass().replace("/", "."));
        return Stream.of(clazz.getMethods(), clazz.getDeclaredMethods())
                .flatMap(Stream::of)
                .filter(m -> m.getName().equals(methodName))
                .filter(m -> m.getParameters().length == argCount)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Method not found"));
    }
    
    interface E1<A> extends Executors, Serializable {
        void handle(A a1);
    }
    
    interface E2<A, B> extends Executors, Serializable {
        void handle(A a, B b);
    }
    
    interface E3<A, B, C> extends Executors, Serializable {
        void handle(A a, B b, C c);
    }
    
    interface E4<A, B, C, D> extends Executors, Serializable {
        void handle(A a, B b, C c, D d);
    }
    
    interface E5<A, B, C, D, E> extends Executors, Serializable {
        void handle(A a, B b, C c, D d, E e);
    }
    
    interface E6<A, B, C, D, E, F> extends Executors, Serializable {
        void handle(A a, B b, C c, D d, E e, F f);
    }
    
    interface O0 extends Executors, Serializable {
        Object handle();
    }
    
    interface O1<A> extends Executors, Serializable {
        Object handle(A a1);
    }
    
    interface O2<A, B> extends Executors, Serializable {
        Object handle(A a, B b);
    }
    
    interface O3<A, B, C> extends Executors, Serializable {
        Object handle(A a, B b, C c);
    }
    
    interface O4<A, B, C, D> extends Executors, Serializable {
        Object handle(A a, B b, C c, D d);
    }
    
    interface O5<A, B, C, D, E> extends Executors, Serializable {
        Object handle(A a, B b, C c, D d, E e);
    }
    
    interface O6<A, B, C, D, E, F> extends Executors, Serializable {
        void handle(A a, B b, C c, D d, E e, F f);
    }
}
