package top.wdcc.eslclient.client;

import org.apache.commons.lang3.StringUtils;
import org.freeswitch.esl.client.inbound.Client;
import org.freeswitch.esl.client.transport.CommandResponse;
import org.freeswitch.esl.client.transport.SendMsg;
import org.freeswitch.esl.client.transport.message.EslMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.channels.IllegalBlockingModeException;
import java.util.List;

/**
 * Esl客户端API
 */
public class EslClient {

    private static final Logger logger = LoggerFactory.getLogger(EslClient.class);

    @Autowired
    private EslClientObjectPool objectPool;

    /**
     * 发送同步命令
     * @param command
     * @param args
     * @return
     */
    public List<String> api(String command, String args) {
        Client client = getClient();
        logger.debug("execute api command: {}, args:{}", command, args);
        EslMessage msg = client.sendSyncApiCommand(command, args);
        releaseClient(client);
        return msg.getBodyLines();
    }

    /**
     * 发送异步命令
     * @param command
     * @param args
     * @return job-uuid
     */
    public String bgapi(String command, String args){
        Client client = getClient();
        logger.debug("execute bgapi command: {}, args:{}", command, args);
        String uuid = client.sendAsyncApiCommand(command, args);
        releaseClient(client);
        return uuid;
    }

    /**
     * 发送消息
     * @param msg
     * @return
     */
    public CommandResponse sendMessage(SendMsg msg){
        Client client = getClient();
        logger.debug("send a message: {}", msg);
        CommandResponse commandResponse = client.sendMessage(msg);
        releaseClient(client);
        return commandResponse;
    }

    /**
     * 执行一个FreeSWITCH App
     * @param uuid
     * @param appName
     * @param appArgs
     * @return
     */
    public CommandResponse execute(String uuid, String appName, String appArgs){
        logger.debug("execute an application --- uuid: {},  name: {}, args: {}",
                uuid, appName, appArgs);
        SendMsg msg = new SendMsg(uuid);
        msg.addCallCommand("execute");
        msg.addExecuteAppName(appName);
        if(StringUtils.isNotEmpty(appArgs)) {
            msg.addExecuteAppArg(appArgs);
        }
        return sendMessage(msg);
    }

    /**
     * 是否已连接上服务器
     * @return
     */
    public boolean isConnected(){
        try {
            return (!objectPool.isClosed() && objectPool.borrowObject(30000) != null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 获取客户端对象
     * @return
     */
    private Client getClient(){
        try {
            return objectPool.borrowObject(30000);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot get more esl client from pool!" + e);
        }
    }

    /**
     * 归还客户端对象
     * @param client
     */
    private void releaseClient(Client client){
        objectPool.returnObject(client);
    }

}
