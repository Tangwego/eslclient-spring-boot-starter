package top.wdcc.eslclient.client;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 普通ESL事件监听
 */
@Component
public interface EslEventListener {
    void onEslEvent(String eventName, Map<String, String> eventMap);
}
