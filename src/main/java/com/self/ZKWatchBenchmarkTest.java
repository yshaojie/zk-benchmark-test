package com.self;

/**
 * Created by   shaojieyue
 * Created date 2015-08-12 09:59
 */
public class ZKWatchBenchmarkTest {
    public static void main(String[] args) throws Exception {
        String zkString = args[0];
        int dataLen = Integer.valueOf(args[1]);
        int conns = Integer.valueOf(args[2]);
//        int count = Integer.valueOf(args[3]);

        ZKConnBenchmark zkConnBenchmark = new ZKConnBenchmark(zkString,1);
        final int count = 0;
        zkConnBenchmark.watch(dataLen,conns, count);
//        zkConnBenchmark.watch(1,5*1000L);
        Thread.sleep(10000000L);
    }
}
