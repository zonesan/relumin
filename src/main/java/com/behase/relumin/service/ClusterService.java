package com.behase.relumin.service;

import java.io.IOException;
import java.util.Set;

import com.behase.relumin.model.Cluster;
import com.behase.relumin.model.ClusterNode;

public interface ClusterService {
	Set<String> getClusters();

	Cluster getCluster(String clusterName) throws IOException;

	Cluster getClusterByHostAndPort(String hostAndPort) throws IOException;

	boolean existsClusterName(String clusterName);

	void setCluster(String clusterName, String node) throws IOException;

	void deleteCluster(String clusterName);

	void refreshClusters();

	ClusterNode getActiveClusterNode(String clusterName) throws IOException;

	ClusterNode getActiveClusterNodeWithExcludeNodeId(String clusterName, String nodeId) throws IOException;
}
