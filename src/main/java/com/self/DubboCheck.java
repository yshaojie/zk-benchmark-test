package com.self;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;

import java.net.URLDecoder;
import java.util.List;

/**
 * Created by shaojieyue on 8/31/15.
 */
public class DubboCheck {
    public static void main(String[] args) throws Exception {
        final String excelue = "10.10.20";//要排除的
        final String connectString = "10.10.20.50:2181";
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .namespace("")
                .connectString(connectString)
                .connectionTimeoutMs(1000)
                .retryPolicy(new RetryOneTime(1000))
                .build();
        client.start();
        String root = "/dubbo";
        final List<String> strings = client.getChildren().forPath(root);
        for (String string : strings) {
            String providers = root+"/"+string+"/providers";
            String consumers = root+"/"+string+"/consumers";
            if (client.checkExists().forPath(consumers)!=null) {
                List<String> strings1 = client.getChildren().forPath(consumers);
                for (String s : strings1) {
                    final String decode = URLDecoder.decode(s);
                    if (decode.contains(excelue)) {
                        System.out.println(decode);
                    }
                }

            }

            if (client.checkExists().forPath(providers)!=null) {
                List<String> strings1 = client.getChildren().forPath(providers);
                for (String s : strings1) {
                    final String decode = URLDecoder.decode(s);
                    if (decode.contains(excelue)) {
                        System.out.println(decode);
                    }
                }
            }
        }
    }
}
