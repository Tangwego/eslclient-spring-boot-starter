package top.wdcc.eslclient.client;

import org.apache.commons.lang3.StringUtils;
import org.freeswitch.esl.client.inbound.InboundConnectionFailure;
import org.freeswitch.esl.client.inbound.InboundPipelineFactory;
import org.freeswitch.esl.client.internal.IEslProtocolListener;
import org.freeswitch.esl.client.transport.CommandResponse;
import org.freeswitch.esl.client.transport.event.EslEvent;
import org.freeswitch.esl.client.transport.message.EslMessage;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Esl事件监听客户端
 */
public enum EslEventClient implements IEslProtocolListener{
    INSTANCE;

    private static final Logger logger = LoggerFactory.getLogger(EslEventClient.class);

    private ApplicationContext context;

    private Channel channel;
    private AtomicBoolean authenticatorResponded = new AtomicBoolean(false);
    private boolean authenticated;
    private CommandResponse authenticationResponse;

    class InboundClientHandler extends org.freeswitch.esl.client.inbound.InboundClientHandler{

        public InboundClientHandler(String password, IEslProtocolListener listener) {
            super(password, listener);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
            logger.error("error in esl event client: {}", e);
            super.exceptionCaught(ctx, e);
        }
    }

    public void connect(String host, int port, String password, int timeoutSeconds) throws InboundConnectionFailure {
        if (this.canSend()) {
            this.close();
        }

        ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
        InboundClientHandler handler = new InboundClientHandler(password, this);
        InboundPipelineFactory inboundPipelineFactory = new InboundPipelineFactory(handler);

        bootstrap.setPipelineFactory(inboundPipelineFactory);
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));

        if (!future.awaitUninterruptibly((long)timeoutSeconds, TimeUnit.SECONDS)) {
            throw new InboundConnectionFailure("Timeout connecting to " + host + ":" + port);
        } else {
            this.channel = future.getChannel();
            if (!future.isSuccess()) {
                logger.warn("Failed to connect to [{}:{}]", host, port);
                logger.warn("  * reason: {}", future.getCause());
                this.channel = null;
                bootstrap.releaseExternalResources();
                throw new InboundConnectionFailure("Could not connect to " + host + ":" + port, future.getCause());
            } else {
                while(!this.authenticatorResponded.get()) {
                    try {
                        Thread.sleep(250L);
                    } catch (InterruptedException var9) {
                    }
                }

                if (!this.authenticated) {
                    throw new InboundConnectionFailure("Authentication failed: " + this.authenticationResponse.getReplyText());
                }
            }
        }
    }

    /**
     * 是否可正常发送命令
     * 隐藏方法: 只作为监听器不使用该方法
     * @return
     */
    private boolean canSend() {
        return this.channel != null && this.channel.isConnected() && this.authenticated;
    }

    /**
     * 是否已连接
     * @return
     */
    public boolean isConnected() {
        return this.canSend();
    }

    /**
     * 订阅事件
     * @param format
     * @param events
     * @return
     */
    public CommandResponse setEventSubscriptions(String format, String ... events){
        if (events == null || events.length <= 0) {
            throw new IllegalStateException("You must offer least one event.");
        }
        StringBuilder sb = new StringBuilder();
        for (String event: events){
            sb.append(" ");
            sb.append(event);
            sb.append(" ");
        }
        CommandResponse commandResponse = setEventSubscriptions(format, sb.toString());
        return commandResponse;
    }

    /**
     * 订阅事件
     * @param format
     * @param event
     * @return
     */
    public CommandResponse setEventSubscriptions(String format, String event) {
        if (!format.equals("plain")) {
            throw new IllegalStateException("Only 'plain' event format is supported at present");
        } else {
            this.checkConnected();
            InboundClientHandler handler = (InboundClientHandler)this.channel.getPipeline().getLast();
            StringBuilder sb = new StringBuilder();
            if (format != null && !format.isEmpty()) {
                sb.append("event ");
                sb.append(format);
            }

            if (event != null && !event.isEmpty()) {
                sb.append(' ');
                sb.append(event);
            }

            EslMessage response = handler.sendSyncSingleLineCommand(this.channel, sb.toString());
            return new CommandResponse(sb.toString(), response);
        }
    }

    /**
     * 取消订阅所有事件
     * @return
     */
    public CommandResponse cancelEventSubscriptions() {
        this.checkConnected();
        InboundClientHandler handler = (InboundClientHandler)this.channel.getPipeline().getLast();
        EslMessage response = handler.sendSyncSingleLineCommand(this.channel, "noevents");
        return new CommandResponse("noevents", response);
    }

    /**
     * 添加过滤器
     * @param eventHeader
     * @param valueToFilter
     * @return
     */
    public CommandResponse addEventFilter(String eventHeader, String valueToFilter) {
        this.checkConnected();
        InboundClientHandler handler = (InboundClientHandler)this.channel.getPipeline().getLast();
        StringBuilder sb = new StringBuilder();
        if (eventHeader != null && !eventHeader.isEmpty()) {
            sb.append("filter ");
            sb.append(eventHeader);
        }

        if (valueToFilter != null && !valueToFilter.isEmpty()) {
            sb.append(' ');
            sb.append(valueToFilter);
        }

        EslMessage response = handler.sendSyncSingleLineCommand(this.channel, sb.toString());
        return new CommandResponse(sb.toString(), response);
    }

    /**
     * 删除过滤器
     * @param eventHeader
     * @param valueToFilter
     * @return
     */
    public CommandResponse deleteEventFilter(String eventHeader, String valueToFilter) {
        this.checkConnected();
        InboundClientHandler handler = (InboundClientHandler)this.channel.getPipeline().getLast();
        StringBuilder sb = new StringBuilder();
        if (eventHeader != null && !eventHeader.isEmpty()) {
            sb.append("filter delete ");
            sb.append(eventHeader);
        }

        if (valueToFilter != null && !valueToFilter.isEmpty()) {
            sb.append(' ');
            sb.append(valueToFilter);
        }

        EslMessage response = handler.sendSyncSingleLineCommand(this.channel, sb.toString());
        return new CommandResponse(sb.toString(), response);
    }

    /**
     * 设置日志等级
     * @param level
     * @return
     */
    public CommandResponse setLoggingLevel(String level) {
        this.checkConnected();
        InboundClientHandler handler = (InboundClientHandler)this.channel.getPipeline().getLast();
        StringBuilder sb = new StringBuilder();
        if (level != null && !level.isEmpty()) {
            sb.append("log ");
            sb.append(level);
        }

        EslMessage response = handler.sendSyncSingleLineCommand(this.channel, sb.toString());
        return new CommandResponse(sb.toString(), response);
    }

    /**
     * 取消日志
     * @return
     */
    public CommandResponse cancelLogging() {
        this.checkConnected();
        InboundClientHandler handler = (InboundClientHandler)this.channel.getPipeline().getLast();
        EslMessage response = handler.sendSyncSingleLineCommand(this.channel, "nolog");
        return new CommandResponse("nolog", response);
    }

    /**
     * 关闭连接
     * @return
     */
    public CommandResponse close() {
        this.checkConnected();
        InboundClientHandler handler = (InboundClientHandler)this.channel.getPipeline().getLast();
        EslMessage response = handler.sendSyncSingleLineCommand(this.channel, "exit");
        return new CommandResponse("exit", response);
    }

    private void checkConnected() {
        if (!this.canSend()) {
            throw new IllegalStateException("Not connected to FreeSWITCH Event Socket");
        }
    }



    @Override
    public void authResponseReceived(CommandResponse response) {
        this.authenticatorResponded.set(true);
        this.authenticated = response.isOk();
        this.authenticationResponse = response;
        if(authenticated){
            // 认证成功
            this.logger.debug("Auth response success={}, message=[{}]", this.authenticated, response.getReplyText());
            // 认证成功自动订阅相关事件, 只要类实现了EslEventListener接口并且标注了EslListener注解的事件都会被接受
            this.setEventSubscriptions("plain", "all");
            // 通过添加了EslListener注解的类获取到事件和事件子类过滤事件
            Map<String, Object> listeners = this.context.getBeansWithAnnotation(EslListener.class);
            for (Map.Entry<String, Object> entry: listeners.entrySet()) {
                if (entry.getValue() instanceof EslEventListener) {
                    // 添加ESL事件过滤
                    EslEventListener eslEventListener = (EslEventListener) entry.getValue();
                    EslListener annotation = eslEventListener.getClass().getAnnotation(EslListener.class);
                    if (annotation != null){
                        if (StringUtils.isNotEmpty(annotation.eventName())) {
                            this.addEventFilter("Event-Name", annotation.eventName());
                        }
                        if (StringUtils.isNotEmpty(annotation.subClassName())) {
                            this.addEventFilter("Event-Subclass", annotation.subClassName());
                        }
                    }
                } else if (entry.getValue() instanceof EslBackgroundJobListener) {
                    // 添加BGAPI返回结果事件过滤
                    this.addEventFilter("Event-Name", "BACKGROUND_JOB");
                } else {
                    logger.error("no such event named: {}", entry.getKey());
                }
            }
        }
    }

    /**
     * 事件接收
     * @param event
     */
    @Override
    public void eventReceived(EslEvent event) {
        logger.debug("Event received [{}]", event);
        if ("BACKGROUND_JOB".equals(event.getEventName())) {
            // 收到bgapi返回结果
            logger.debug("received background job handle result: {}", event.getEventBodyLines());
            Map<String, EslBackgroundJobListener> listeners = this.context.getBeansOfType(EslBackgroundJobListener.class);
            for (Map.Entry<String, EslBackgroundJobListener> entry: listeners.entrySet()) {
                entry.getValue().onBackgroundResult(event.getEventHeaders().get("Job-UUID"), event.hasEventBody(), event.getEventBodyLines());
            }
        } else {
            // 收到普通ESL事件
            String eventName = event.getEventName();
            Map<String, EslEventListener> listeners = this.context.getBeansOfType(EslEventListener.class);
            logger.debug("received event:[{}] , {}", event.getEventName(), listeners.size());
            for (Map.Entry<String, EslEventListener> entry: listeners.entrySet()) {
                EslEventListener listener = entry.getValue();
                if (listener != null) {
                    // 只推送给标注了 EslListener注解 且 事件名和事件子类相同的监听类
                    EslListener annotation = listener.getClass().getAnnotation(EslListener.class);
                    if (annotation != null){
                        if (StringUtils.equals(annotation.eventName(), eventName)) {
                            String subClassName = event.getEventHeaders().get("Event-Subclass");
                            if (StringUtils.equals(annotation.subClassName(), subClassName)){
                                listener.onEslEvent(eventName, event.getEventHeaders());
                            } else if (StringUtils.isEmpty(annotation.subClassName()) && StringUtils.isEmpty(subClassName)) {
                                listener.onEslEvent(eventName, event.getEventHeaders());
                            } else {
                                continue;
                            }
                        }
                    }
                }
            }
        }
    }

    public void setApplicationContext(ApplicationContext applicationContext){
        this.context = applicationContext;
    }

    /**
     * 断开连接
     */
    @Override
    public void disconnected() {
        logger.error("Disconnected ..");
    }
}
