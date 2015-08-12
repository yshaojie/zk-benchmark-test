import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;

/**
 * Created by   shaojieyue
 * Created date 2015-08-10 14:50
 */
public class Test {
    public static void main(String[] args) throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .namespace("zktest")
                .connectString("192.168.8.18:2181")
                .connectionTimeoutMs(1000)
                .retryPolicy(new RetryOneTime( 1000))
                .build();
        client.start();
        for (int i = 0; i < 1; i++) {
            String path = "/node"+i;
            client.create().forPath(path);
        }
    }
}
