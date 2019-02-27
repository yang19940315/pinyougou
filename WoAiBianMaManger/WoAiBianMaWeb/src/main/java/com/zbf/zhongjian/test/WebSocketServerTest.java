package com.zbf.zhongjian.test;

import com.zbf.utils.SpringUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@ServerEndpoint("/webSocketTest/sendMessage/{userid}")
public class WebSocketServerTest {

    private static RedisTemplate redisTemplate= SpringUtil.getBean("redisTemplate",RedisTemplate.class);

   public static Map<String,Session> sessionMap=new HashMap<String,Session>();
     private  static Log  logger= LogFactory.getLog(WebSocketServerTest.class);

    /**
     * 建立连接
     * @param session
     * @param userid
     */
        @OnOpen
        public void onOpen(Session session,@PathParam("userid")String userid){
                logger.info("====建立连接=====");
                //存储Session
                sessionMap.put(userid,session);
                //检测离线消息
            List<String> range = redisTemplate.opsForList().range("userid"+userid, 0, -1);
            range.forEach((msg)->{
                //发送消息
                WebSocketServerTest.sendMessage(session,msg,userid);
                //删除消息
                redisTemplate.opsForList().remove("userid"+userid,1,msg);
            });

        }

    /**
     * 接收客户端传递的消息
     * @param session
     * @param userid
     */
        @OnMessage
        public void onMessage(Session session,String message,@PathParam("userid")String userid){
                logger.info("===接收到的数据message"+message);
        }

    /**
     * 关闭连接
     * @param session
     * @param userid
     */
    @OnClose
    public void onClose(Session session,@PathParam("userid")String userid){
            logger.info("====关闭连接=====");
            //清楚Session
            sessionMap.remove(userid);
        }

    /**
     * 错误异常
     * @param session
     * @param throwable
     * @param userid
     *
     */
        @OnError
        public void onError(Session session,Throwable throwable,@PathParam("userid")String userid){

        }


    /**
     * 发送消息到客户端
     * @param session
     * @param message
     * @param userid
     */

        public static void sendMessage(Session session,String message,String userid){

            try {
                session.getAsyncRemote().setSendTimeout(2000);
                session.getAsyncRemote().sendText(message);
            }catch (Exception e){
                e.printStackTrace();
                redisTemplate.expire("userid"+userid,1000, TimeUnit.SECONDS);
                redisTemplate.opsForList().leftPush("userid"+userid,message);


            }
        }



}
