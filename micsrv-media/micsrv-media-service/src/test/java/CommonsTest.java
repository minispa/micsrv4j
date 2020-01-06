import com.github.minispa.micsrv.media.handler.ComplexMarkHandler;
import lombok.SneakyThrows;
import org.junit.Test;

public class CommonsTest {

    @Test
    public void replaceAll() {
        System.out.println("a.mp4".replaceAll("\\.\\S{3}$", "_qr_.jpg"));
    }

    @Test
    @SneakyThrows
    public void complexMark() {
        ComplexMarkHandler complexMarkHandler = new ComplexMarkHandler();
        complexMarkHandler.handle("C:\\Users\\Mr.Y\\Videos\\filehub\\origin.mp4");
        Thread.sleep(10000000);
    }

}
