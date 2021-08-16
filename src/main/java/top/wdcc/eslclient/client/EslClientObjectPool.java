package top.wdcc.eslclient.client;

import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.freeswitch.esl.client.inbound.Client;

public class EslClientObjectPool extends GenericObjectPool<Client> {
    private EslClientFactory factory;
    public EslClientObjectPool(EslClientFactory factory) {
        super(factory);
        this.factory = factory;
    }

    public EslClientObjectPool(EslClientFactory factory, GenericObjectPoolConfig<Client> config) {
        super(factory, config);
        this.factory = factory;
    }

    public EslClientObjectPool(EslClientFactory factory, GenericObjectPoolConfig<Client> config, AbandonedConfig abandonedConfig) {
        super(factory, config, abandonedConfig);
        this.factory = factory;
    }

    @Override
    public EslClientFactory getFactory(){
        return this.factory;
    }

}
