package com.self;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.data.Stat;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * Created by   shaojieyue
 * Created date 2015-08-09 18:24
 */
public class ZKConnBenchmark {
    //缓存连接
    private List<CuratorFramework> connections = new ArrayList<CuratorFramework>();

    private String zkString = null;
    private ExecutorService executor;
    private int threads;
    public ZKConnBenchmark(String zkString,int threads) {
        executor = new ThreadPoolExecutor(1, threads,
                1, TimeUnit.DAYS, new ArrayBlockingQueue<Runnable>(
                10000), Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy());
        this.zkString = zkString;
        this.threads = threads;
    }

    /**
     * 创建连接
     * @return
     */
    public boolean createConn() {
        boolean success = true;
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .namespace("zktest")
                .connectString(zkString)
                .connectionTimeoutMs(1000)
                .retryPolicy(new RetryOneTime( 1000))
                .build();
        client.start();
        try {
            client.blockUntilConnected();
        } catch (InterruptedException e) {
            success = false;
            e.printStackTrace();
        }
        connections.add(client);
        return success;
    }

    public void getData(int dataLen, final int count) throws Exception {
        final AtomicInteger inde = new AtomicInteger();
        final AtomicInteger counter = new AtomicInteger();
        final AtomicInteger fail = new AtomicInteger();
        final AtomicLong cost = new AtomicLong();
        final byte[] data = createData(dataLen);
        System.out.println("init test env...");
        connections(threads);
        if (connections.size() <1) {
            throw new IllegalStateException("no connection");
        }
        for (int i = 0; i < threads; i++) {
            connections.get(0).setData().forPath("/node"+i,data);
        }

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        System.out.println("init env success..");
        System.out.println("start getdata test threads="+threads+" count="+count+" data="+dataLen+"KB");
        for (int i = 0; i < count; i++) {
            executor.submit(new Runnable() {
                public void run() {
                    final int index = inde.incrementAndGet() % threads;
                    final CuratorFramework curatorFramework = connections.get(index);
                    try {
                        long start = System.currentTimeMillis();
                        final byte[] data = curatorFramework.getData().forPath("/node" + index);
                        long costTime = System.currentTimeMillis() - start;
                        cost.addAndGet(costTime);
                        if (data == null) {
                            fail.incrementAndGet();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        fail.incrementAndGet();
                    }finally {
                        final int i1 = counter.incrementAndGet();
                        if (i1%1000==0) {
                            System.out.println("proccess success count = "+i1);
                        }
                        if (i1 == count) {
                            countDownLatch.countDown();
                        }
                    }
                }
            });
        }

        countDownLatch.await();
        System.out.println("end getdata test average time="+(cost.longValue()/count)+" fail="+fail.intValue());
    }

    public void setData(int dataLen, final int count) throws Exception {
        final AtomicInteger inde = new AtomicInteger();
        final AtomicInteger counter = new AtomicInteger();
        final AtomicInteger fail = new AtomicInteger();
        final AtomicLong cost = new AtomicLong();
        final byte[] data = createData(dataLen);
        System.out.println("init test env...");
        connections(threads);


        if (connections.size() <1) {
            throw new IllegalStateException("no connection");
        }
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        System.out.println("init env success..");
        System.out.println("start setdata test threads="+threads+" count="+count+" data="+dataLen+"KB");
        for (int i = 0; i < count; i++) {
            executor.submit(new Runnable() {
                public void run() {
                    final int index = inde.incrementAndGet() % threads;
                    final CuratorFramework curatorFramework = connections.get(index);
                    try {
                        long start = System.currentTimeMillis();
                        final Stat stat = curatorFramework.setData().forPath("/node" + index, data);
                        long costTime = System.currentTimeMillis() - start;
                        cost.addAndGet(costTime);
                        if (stat == null) {
                            fail.incrementAndGet();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        fail.incrementAndGet();
                    }finally {
                        final int i1 = counter.incrementAndGet();
                        if (i1%1000==0) {
                            System.out.println("proccess success count = "+i1);
                        }
                        if (i1 == count) {
                            countDownLatch.countDown();
                        }
                    }
                }
            });
        }

        countDownLatch.await();
        System.out.println("end setdata test average time="+(cost.longValue()/count)+" fail="+fail.intValue());

    }


    public final void connections(int count){
        if (count<1) {
            throw new IllegalArgumentException("count="+count+" param error.");
        }
        long start = System.currentTimeMillis();
        for (int i = 1; i <= count; i++) {
            final boolean conn = createConn();
            if (!conn) {
                System.out.println("i="+i+" connection fail.");
            }
            if (i % 100 == 0) {
                System.out.println("connections count="+i+" cost time="+(System.currentTimeMillis()-start));
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("connections test count="+count+" cost time ="+(end-start)+" average time="+((end-start)/count));
    }

    private byte[] createData(int length){
        //length kb 的数据
        byte[] data = new byte[1024*length];
        for (int i = 0; i < data.length; i++) {
            data[i]='a';
        }
        return data;
    }

    public void watch(int dataLen,int conns,int count) throws Exception {
        String path = "/node1";
        final List<Long> tims = new CopyOnWriteArrayList<Long>();
        System.out.println("start init zk watch....");
        System.out.println("need init zk conn count=" + conns);
        connections(conns);

        for (int i = 1; i <= 1; i++) {
            path="/node"+i;
            doWatch(path, tims);
        }
        System.out.println("init zk watch end,watch node count="+connections.size());

        Thread.sleep(2000L);
        //设置数据
        String data = System.currentTimeMillis()+"";
        connections.get(0).setData().forPath(path, data.getBytes());
        System.out.println("set data success, dataLen="+dataLen);
    }

    private void doWatch(final String path, final List<Long> tims) throws Exception {
        System.out.println("start watch path="+path);
        final AtomicInteger counter = new AtomicInteger();
        final AtomicLong costTime = new AtomicLong();
        //初始化监听
        for (final CuratorFramework connection : connections) {
            connection.getData().usingWatcher(new CuratorWatcher() {
                public void process(final WatchedEvent watchedEvent) throws Exception {
                    new Thread(new Runnable() {
                        public void run() {
                            long current = System.currentTimeMillis();
                            final byte[] bytes;
                            try {
                                bytes = connection.getData().forPath(path);
                                long setTime = Long.valueOf(new String(bytes).substring(0,13)) ;
                                final long ts = current - setTime;
                                final long cost = costTime.addAndGet(ts);
                                final int count = counter.incrementAndGet();
                                if (count%100==0) {
                                    System.out.println(" count="+count+" average cost time="+(cost/count));
                                }
                                tims.add(ts);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();


                }
            }).forPath(path);
        }
        System.out.println("success watch path="+path);

    }


    public void setDataAndGet(int dataLen, int nodeCount) throws Exception {
        byte[] data = createData(dataLen);
        Random random = new Random();
        int size = connections.size();
        List<String> nodes = new ArrayList<String>(nodeCount);
        long start = System.currentTimeMillis();
        long min = 10000;
        long max = 0;
        long all = 0;
        for (CuratorFramework connection : connections) {
            for (int i = 1; i <= nodeCount; i++) {
                //随机节点
                String path = "/node"+i;
                try {
                    long s1=System.currentTimeMillis();
                    final Stat stat = connection.setData().forPath(path, data);
                    long s2=System.currentTimeMillis();
                    long ts = s2-s1;
                    all = all + ts;
                    if (min>ts) {
                        min = ts;
                    }
                    if(max<ts){
                        max = ts;
                    }

                    nodes.add(path);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        long end = System.currentTimeMillis();
        System.out.println("set data cost min="+min+" max="+max+" average time="+((all))/(connections.size()*nodeCount));
        start = System.currentTimeMillis();
        min = 100000;
        max = 0;
        all = 0;
        for (CuratorFramework connection : connections) {
            for (int i = 1; i <= nodeCount; i++) {
                //随机节点
                String path = "/node"+i;
                long s1=System.currentTimeMillis();
                final byte[] bytes = connection.getData().forPath(path);
                System.out.println(new String(bytes).length());
                long s2=System.currentTimeMillis();
                long ts = s2-s1;
                all = all + ts;
                if (min>ts) {
                    min = ts;
                }
                if(max<ts){
                    max = ts;
                }
            }
        }
        end = System.currentTimeMillis();
        System.out.println("get data cost min="+min+" max="+max+" average time="+((all))/(connections.size()*nodeCount));
        System.out.println("set data success");
    }

}
