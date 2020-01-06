import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.DbImpl;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.iq80.leveldb.util.Slice;
import org.junit.Test;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.iq80.leveldb.impl.Iq80DBFactory.asString;
import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;

public class CommonsTest {

    // C:\Users\Mr.Y\AppData\Local\Temp\leveldb
    private final File databaseDir = new File(System.getProperty("java.io.tmpdir"), "leveldb");

    private final DBFactory factory = Iq80DBFactory.factory;

    @Test
    @SneakyThrows
    public void leveldb() {
        Options options = new Options().createIfMissing(true).compressionType(CompressionType.NONE);
        File path = new File(databaseDir, "test")/*getSchemaDir("test")*/;

        DB db = factory.open(path, options);
//
//        System.out.println("Adding");
//        for (int i = 0; i < 1000 * 1000; i++) {
//            if (i % 100000 == 0) {
//                System.out.println("  at: " + i);
//            }
//            db.put(bytes("key" + i), bytes("value" + i));
//        }
//
        db.close();

        db = factory.open(path, options);

        System.out.println("Deleting");
        for (int i = 0; i < 1000 * 1000; i++) {
            if (i % 100000 == 0) {
                System.out.println("  at: " + i);
            }
            db.delete(bytes("key" + i));
        }
        ((DbImpl) db).compactRange(1, new Slice("key1".getBytes()), new Slice("key1000000000".getBytes()));
        db.close();
    }

    @SneakyThrows
    File getSchemaDir(String name) {
        File rc = new File(databaseDir, name);
        factory.destroy(rc, new Options().createIfMissing(true));
        rc.mkdirs();
        return rc;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class User implements Serializable {
        private int age;
        private String name;
    }

    @Test
    public void fastjson() {
        User user = new User(18, "jack");
        System.out.println(user);
        final byte[] bytes = JSON.toJSONBytes(user);
        System.out.println(new String(bytes));
        final User o = JSON.parseObject(bytes, User.class);
        System.out.println(o);
    }

}
