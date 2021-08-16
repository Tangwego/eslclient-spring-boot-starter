package top.wdcc.eslclient.client;


import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.freeswitch.esl.client.inbound.Client;
import org.freeswitch.esl.client.inbound.InboundConnectionFailure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.wdcc.eslclient.config.EslProperties;


/**
 * Client客户端工厂
 */
public class EslClientFactory extends BasePooledObjectFactory<Client> {

    private static final Logger logger = LoggerFactory.getLogger(EslClientFactory.class);

    private EslProperties properties;

    public EslClientFactory(EslProperties eslProperties){
        this.properties = eslProperties;
    }

    @Override
    public Client create() throws Exception {
        // 创建Client对象
        return connect();
    }

    @Override
    public PooledObject<Client> wrap(Client obj) {
        return new DefaultPooledObject<>(obj);
    }

    @Override
    public void destroyObject(PooledObject<Client> p) throws Exception {
        // 获取Client对象
        Client client = p.getObject();
        // 关闭连接
        if (client != null) {
            client.close();
        }
        // 销毁对象
        super.destroyObject(p);
    }

    /**
     * 连接
     * @return
     */
    public Client connect() {
        Client client = new Client();
        String host = this.properties.getHost();
        int port = this.properties.getPort();
        try {
            client.connect(host, port, this.properties.getPassword(), this.properties.getTimeoutSec());
            return client;
        } catch (InboundConnectionFailure e) {
            logger.error("esl client connect server failure: host:{}, port: {}, cause:{}", host, port, e.getMessage());
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 校验对象的有效性
     * @param p
     * @return
     */
    @Override
    public boolean validateObject(PooledObject<Client> p) {
        // 获取Client对象
        Client client = p.getObject();
        if (client != null) {
            return client.canSend();
        }
        return false;
    }

    @Override
    public PooledObject<Client> makeObject() throws Exception {
        return super.makeObject();
    }

}
