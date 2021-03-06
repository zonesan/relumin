package com.behase.relumin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cluster {
    private String clusterName;
    private String status;
    private Map<String, String> info;
    private List<ClusterNode> nodes;
    private List<SlotInfo> slots;

    public ClusterNode getNodeByHostAndPort(String hostAndPort) {
        return nodes.stream()
                .filter(node -> StringUtils.equalsIgnoreCase(hostAndPort, node.getHostAndPort()))
                .findFirst()
                .orElse(null);
    }

    public ClusterNode getNodeByNodeId(String nodeId) {
        return nodes.stream()
                .filter(node -> StringUtils.equalsIgnoreCase(nodeId, node.getNodeId()))
                .findFirst()
                .orElse(null);
    }

    public String getStatus() {
        if (status != null) {
            return status;
        }

        String clusterState = info.get("cluster_state");
        if (StringUtils.equalsIgnoreCase(clusterState, "fail")) {
            status = "fail";
            return status;
        }

        boolean existsFailNode = nodes.stream()
                .filter(node -> node.hasFlag("fail"))
                .findAny()
                .isPresent();

        if (existsFailNode) {
            status = "warn";
            return status;
        }

        status = "ok";
        return status;
    }
}
