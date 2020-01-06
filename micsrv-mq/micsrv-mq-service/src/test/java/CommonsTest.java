import lombok.SneakyThrows;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class CommonsTest {

    @Test
    public void finallyTest() {
        finalTrigger();
    }

    @SneakyThrows
    public static void finalTrigger() {
        for(int i = 0; i < 3; i++) {
            try {
                System.out.println("i - " + i);
            } finally {
                System.out.println("finally - " + i);
            }
        }
        TimeUnit.SECONDS.sleep(600);
    }
}
