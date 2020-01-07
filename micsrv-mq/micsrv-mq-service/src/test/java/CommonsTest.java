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
        for (int i = 0; i < 3; i++) {
            try {
                System.out.println("i - " + i);
                if(i == 1) {
                    throw new NullPointerException("i == 1");
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                System.out.println("finally - " + i);
            }
        }
    }



}
