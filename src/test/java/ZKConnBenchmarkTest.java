import com.self.ZKConnBenchmark;

/**
 * Created by   shaojieyue
 * Created date 2015-08-10 11:44
 */
public class ZKConnBenchmarkTest {
    public static void main(String[] args) throws Exception {
        ZKConnBenchmark zkConnBenchmark = new ZKConnBenchmark("192.168.8.18:2181");
        zkConnBenchmark.connections(5);
        zkConnBenchmark.setDataAndGet(512, 3);
        System.out.println("work end");
//        zkConnBenchmark.watch(1,5*1000L);
        Thread.sleep(10000000L);
    }
}
