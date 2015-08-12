package com.self;

import com.self.ZKConnBenchmark;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;

/**
 * Created by   shaojieyue
 * Created date 2015-08-10 11:44
 */
public class ZKConnBenchmarkTest {
    public static void main(String[] args) throws Exception {
        String zkString = args[0];
        int connections = Integer.valueOf(args[1]);

        ZKConnBenchmark zkConnBenchmark = new ZKConnBenchmark(zkString,3);
//        zkConnBenchmark.createConn();
        zkConnBenchmark.connections(connections);
//        System.out.println(conn);
//        zkConnBenchmark.connections(5);
//        zkConnBenchmark.setDataAndGet(512, 3);
        System.out.println("work end");
//        zkConnBenchmark.watch(1,5*1000L);
        Thread.sleep(10000000L);
    }
}
