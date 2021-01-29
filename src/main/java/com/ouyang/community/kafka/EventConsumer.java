package com.ouyang.community.kafka;

import com.alibaba.fastjson.JSONObject;
import com.ouyang.community.entity.DiscussPost;
import com.ouyang.community.entity.Event;
import com.ouyang.community.entity.Message;
import com.ouyang.community.service.DiscussPostService;
import com.ouyang.community.service.MessageService;
import com.ouyang.community.utils.CommunityUtil;
import com.ouyang.community.utils.Constant;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.logging.log4j.util.StringMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author coolsen
 * @Description 事件消费者
 */
@Slf4j
@Component
public class EventConsumer {
    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostService discussPostService;

//    @Autowired
//    private ElasticsearchService elasticsearchService;

//    @Autowired
//    private ThreadPoolTaskScheduler taskScheduler;

    /**
     * 评论，点赞，关注的共用处理器
     * 发送的时候指定了topic和value，都为String类型，所以这里泛型都为String
     *
     * @param record 记录
     */
    @KafkaListener(topics = {Constant.TOPIC_COMMENT, Constant.TOPIC_FOLLOW, Constant.TOPIC_LIKE})
    public void handleCommentMessage(ConsumerRecord<String, String> record) {
        if (record == null || record.value() == null) {
            log.error("消息的内容为空");
            return;
        }

        // 将json数据转化为事件对象
        Event event = JSONObject.parseObject(record.value(), Event.class);
        if (event == null) {
            log.error("消息格式错误");
        }

        // 发送站内通知
        Message message = new Message();
        message.setFromId(Constant.SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        // 此处存的是主题
        message.setConversationId(event.getTopic());
        message.setCreateTime(System.currentTimeMillis());

        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());

        // 如果说event有其他的数据，也存放到message的map里一起传过去
        if (!event.getData().isEmpty()) {
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }

        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }

    // 消费发帖事件
    @KafkaListener(topics = {Constant.TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            log.error("消息的内容为空");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            log.error("消息格式错误");
        }
//        DiscussPost post = discussPostService.findDiscussPostById(event.getEntityId());
//        elasticsearchService.saveDiscussPost(post);
    }

    // 消费分享事件
    @KafkaListener(topics = Constant.TOPIC_SHARE)
    public void handleShareMessage(ConsumerRecord record) {
//        if (record == null || record.value() == null) {
//            log.error("消息的内容为空!");
//            return;
//        }
//
//        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
//        if (event == null) {
//            log.error("消息格式错误!");
//            return;
//        }
//
//        String htmlUrl = (String) event.getData().get("htmlUrl");
//        String fileName = (String) event.getData().get("fileName");
//        String suffix = (String) event.getData().get("suffix");
//
//        String cmd = wkImageCommand + " --quality 75 "
//                + htmlUrl + " " + wkImageStorage + "/" + fileName + suffix;
//        try {
//            Runtime.getRuntime().exec(cmd);
//            log.info("生成长图成功: " + cmd);
//        } catch (IOException e) {
//            log.error("生成长图失败: " + e.getMessage());
//        }
//
//        // 启动定时器，监视该图片，一旦生成了，上传云服务器
//        // 如果超时(30s),或者上传失败(>=3次) 取消任务.返回值future可以取消task
//        UploadTask task = new UploadTask(fileName, suffix);
//        Future future = taskScheduler.scheduleAtFixedRate(task, 500);
//        task.setFuture(future);
    }

    class UploadTask implements Runnable {
        // 文件名称
        private String fileName;
        // 文件后缀
        private String suffix;
        // 启动任务的返回值
        private Future future;
        // 开始时间
        private long startTime;
        // 上传次数
        private int uploadTimes;

        public UploadTask(String fileName, String suffix) {
            this.fileName = fileName;
            this.suffix = suffix;
            this.startTime = System.currentTimeMillis();
        }

        public void setFuture(Future future) {
            this.future = future;
        }

        @Override
        public void run() {

            // 生成失败
            if (System.currentTimeMillis() - startTime > 30000) {
                log.error("执行时间过长,终止任务:" + fileName);
                future.cancel(true);
                return;
            }

            // 上传失败，一般是上传七牛云失败
            if (uploadTimes >= 3) {
                log.error("上传次数过多,终止任务:" + fileName);
                future.cancel(true);
                return;
            }

//            String path = wkImageStorage + "/" + fileName + suffix;
//            File file = new File(path);
//            if (file.exists()) {
//                log.info(String.format("开始第%d次上传[%s].", ++uploadTimes, fileName));
//                // 设置响应信息
//                StringMap policy = new StringMap();
//                policy.put("returnBody", CommunityUtil.getJSONString(0));
//                // 生成上传凭证
//                Auth auth = Auth.create(accessKey, secretKey);
//                String uploadToken = auth.uploadToken(shareBucketName, fileName, 3600, policy);
//                // 指定上传机房
//                UploadManager manager = new UploadManager(new Configuration(Zone.zone1()));
//                try {
//                    // 开始上传图片
//                    Response response = manager.put(
//                            path, fileName, uploadToken, null, "image/" + suffix, false);
//                    // 处理响应结果
//                    JSONObject json = JSONObject.parseObject(response.bodyString());
//                    if (json == null || json.get("code") == null || !json.get("code").toString().equals("0")) {
//                        log.info(String.format("第%d次上传失败[%s].", uploadTimes, fileName));
//                    } else {
//                        log.info(String.format("第%d次上传成功[%s].", uploadTimes, fileName));
//                        future.cancel(true);
//                    }
//                } catch (QiniuException e) {
//                    log.info(String.format("第%d次上传失败[%s].", uploadTimes, fileName));
//                }
//            } else {
//                log.info("等待图片生成[" + fileName + "].");
//            }
        }
    }
}
