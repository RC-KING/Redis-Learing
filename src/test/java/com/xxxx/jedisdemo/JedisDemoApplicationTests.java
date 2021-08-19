package com.xxxx.jedisdemo;

import com.xxxx.jedisdemo.pojo.User;
import com.xxxx.jedisdemo.util.SerializeUtil;
import org.junit.After;
import org.junit.Before;
 import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.params.SetParams;
 import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
 @SpringBootTest
@RunWith(SpringRunner.class)
public class JedisDemoApplicationTests {
     @Autowired
    private JedisPool jedisPool;
     private Jedis jedis =null;
     @Before
    public void initConn3(){
        jedis = jedisPool.getResource();
    }
    @After
    public void closeConn(){
        if (jedis!=null){
            jedis.close();
        }
    }
     @Test
    public void initConn() {
        Jedis jedis = new Jedis("192.168.243.128",6379);
        jedis.auth("jwj422123");
        jedis.select(0);
        String pong = jedis.ping();
        System.out.println(pong);
        jedis.set("name", "jdd");
        System.out.println(jedis.get("name"));
        jedis.close();
    }
     /**
     * 通过连接池连接Jedis
     */
    @Test
    public void initConn2() {
        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(),
                "192.168.243.128",6379,1000,"jwj422123");
        Jedis jedis = jedisPool.getResource();
        jedis.auth("jwj422123");
         jedis.select(0);
        String pong = jedis.ping();
        System.out.println(pong);
        jedis.set("age","81");
        System.out.println(jedis.get("age"));
        jedis.close();
    }
     @Test
    public void testConn3() {
         jedis.select(0);
        String pong = jedis.ping();
        System.out.println(pong);
        jedis.set("addr","sh");
        System.out.println(jedis.get("addr"));
    }
     @Test
    public void testString(){
        // 设置单条值
        jedis.set("name", "jdd");
        // 设置多条值
        jedis.mset("age","18","sex","男","addr","湖北");
        // 获取多条数据
        List<String> list = jedis.mget("age","addr");
        list.forEach(System.out::println);
        // 通用删除
        jedis.del("addr");
        // 目录形式存储结构
        jedis.set("user:1:cart:item:1", "goods1");
        String s = jedis.get("user:1:cart:item:1");
        System.out.println(s);
    }
     /**
     * 操作Hash
     */
    @Test
    public void testHash(){
        jedis.hset("user1", "name", "jdd");
        jedis.hset("user1", "name1", "jxx");
        String user1Name = jedis.hget("user1", "name");
        System.out.println(user1Name);
         // 通过map来添加数据多条
        Map<String, String> map = new HashMap();
        map.put("name", "xxx");
        map.put("age", "66");
        map.put("sex", "女");
        jedis.hset("user2", map);
         // 获取多条值
        List<String> list =jedis.hmget("user2", "name","age","sex");
        list.forEach(System.out::println);
         // Hash删除
        jedis.hdel("user1", "name1");
    }
     /**
     * 操作List
     */
    @Test
    public void testList(){
        // 左添加
        jedis.lpush("students1","金大大","金小小","小妮子");
        // 右添加
        jedis.rpush("students1","金大大","金小小","小妮子");
        // 获取数据
        List<String> list= jedis.lrange("students1", 0, 5);
        list.forEach(System.out::println);
        // 获取长度
        Long aLong = jedis.llen("students2");
        System.out.println(aLong);
        // 删除数据,count是指删除几个
        jedis.lrem("students1", 1, "金大大");
     }
     /**
     * 操作set
     */
    @Test
    public void testSet() {
        // 添加数据
        jedis.sadd("letters", "aaa","bbb","ccc","ddd");
        // 获取数据
        Set<String> set = jedis.smembers("letters");
        set.forEach(System.out::println);
        // 删除数据
        jedis.srem("letters", "aaa", "bbb");
    }
     /**
     * 操作SortedSet
     */
    @Test
    public void testSortedSet() {
        // 添加数据
        Map<String, Double> map = new HashMap<>();
        map.put("Tom", 7d);
        map.put("Jerry", 1d);
        map.put("Mary", 3d);
        map.put("Jack", 10d);
        // 添加数据
        jedis.zadd("score", map);
        // 获取数据
        Set<String> set =  jedis.zrange("score",0,6);
        set.forEach(System.out::println);
        // 长度
        Long scoreLength = jedis.zcard("score");
        System.out.println(scoreLength);
        // 删除数据
        jedis.zrem("score", "Jerry", "Mary");
    }
     /**
     * xx与nx 和 超时
     */
    @Test
    public void testExpire(){
        // 设置失效时间 秒
        jedis.setex("code", 20, "test");
        // 获取失效时间 秒
        Long ttl = jedis.ttl("code");
        // 获取失效时间 毫秒
        Long pttl = jedis.pttl("code");
        System.out.println(ttl+pttl);
         // 给已存在的key设置失效时间
        jedis.expire("score", 20);
         // nx 与 xx
        // NX命令: 仅当key不存在时，set才会生效。
        // XX命令: 仅当key存在时，set才会生效。
        SetParams setParamsNX = new SetParams();
        SetParams setParamsXX = new SetParams();
        setParamsNX.ex(50).nx();
        setParamsXX.ex(20).xx();
        jedis.set("code","testNX",setParamsNX);
        jedis.set("code","testXX",setParamsXX);
    }
     /**
     * 获取所有key
     */
    @Test
    public void testGetAllKey(){
        // 获取所有的key
        Set<String> set =  jedis.keys("*");
        set.forEach(System.out::println);
         // 查询数据库中有多少个key
        Long aLong = jedis.dbSize();
        System.out.println(aLong);
    }
     /**
     * 事务(Redis事务很弱!无法多表回滚!)
     */
    @Test
    public void testTX(){
        // 开启事务
        Transaction tx = jedis.multi();
        // 储存
        tx.set("name", "111");
        // 提交事务
        tx.exec();
        // 取消事务
        tx.discard();
    }
     /**
     * Redis操作字节
     */
    @Test
    public void testByte(){
        User user =new User();
        user.setUsername("jdd");
        user.setId(1);
        user.setPassword("123");
        // 存字节数组
        jedis.set(SerializeUtil.serialize("user"), SerializeUtil.serialize(user));
        // 取值
        byte[] users = jedis.get(SerializeUtil.serialize("user"));
        // 反序列化
        System.out.println(SerializeUtil.unserialize(users));
     }
 }
