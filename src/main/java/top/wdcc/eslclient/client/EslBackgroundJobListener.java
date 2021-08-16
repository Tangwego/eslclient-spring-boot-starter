package top.wdcc.eslclient.client;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * bgapi返回结果监听
 */
@Component
public interface EslBackgroundJobListener {
    void onBackgroundResult(String jobUuid, boolean hasBody, List<String> bodyLines);
}
