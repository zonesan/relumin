package com.behase.relumin.controller;

import com.behase.relumin.exception.InvalidParameterException;
import com.behase.relumin.model.Cluster;
import com.behase.relumin.model.Notice;
import com.behase.relumin.model.PagerData;
import com.behase.relumin.model.SlowLog;
import com.behase.relumin.service.ClusterService;
import com.behase.relumin.service.LoggingOperationService;
import com.behase.relumin.service.NodeService;
import com.behase.relumin.webconfig.WebConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doReturn;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class ClusterApiControllerTest {
    @InjectMocks
    @Spy
    private ClusterApiController tested = new ClusterApiController();

    @Mock
    private ClusterService clusterService;

    @Mock
    private NodeService nodeService;

    @Mock
    private LoggingOperationService loggingOperationService;

    @Spy
    private ObjectMapper mapper = WebConfig.MAPPER;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void getClusterList_full_is_false_then_return_sorted_clusterNames() throws Exception {
        doReturn(Sets.newHashSet("test2", "test1"))
                .when(clusterService)
                .getClusters();

        List<String> result = (List<String>) tested.getClusterList("");
        log.info("result={}", result);
        assertThat(result, contains("test1", "test2"));
    }

    @Test
    public void getClusterList_full_is_true_then_return_sorted_clusters() throws Exception {
        doReturn(Sets.newHashSet("test3", "test2", "test1"))
                .when(clusterService)
                .getClusters();
        doReturn(null)
                .when(clusterService)
                .getCluster("test3");
        doReturn(Cluster.builder()
                .clusterName("test2")
                .info(Maps.newHashMap())
                .nodes(Lists.newArrayList())
                .build())
                .when(clusterService)
                .getCluster("test2");
        doReturn(Cluster.builder()
                .clusterName("test1")
                .info(Maps.newHashMap())
                .nodes(Lists.newArrayList())
                .build())
                .when(clusterService)
                .getCluster("test1");

        List<Cluster> result = (List<Cluster>) tested.getClusterList("true");
        log.info("result={}", result);
        assertThat(result.get(0).getClusterName(), is("test1"));
        assertThat(result.get(1).getClusterName(), is("test2"));
    }

    @Test
    public void getCluster() throws Exception {
        doReturn(Cluster.builder()
                .clusterName("test1")
                .info(Maps.newHashMap())
                .nodes(Lists.newArrayList())
                .build())
                .when(clusterService)
                .getCluster("test1");

        Cluster result = tested.getCluster("test1");
        log.info("result={}", result);
        assertThat(result.getClusterName(), is("test1"));
    }

    @Test(expected = InvalidParameterException.class)
    public void setCluster_clusterName_exist_then_throw_exception() throws Exception {
        doReturn(true).when(clusterService).existsClusterName("test1");
        tested.setCluster(null, "test1", "localhost:10000");
    }

    @Test
    public void setCluster() throws Exception {
        doReturn(false).when(clusterService).existsClusterName("test1");
        doReturn(Cluster.builder()
                .clusterName("test1")
                .info(Maps.newHashMap())
                .nodes(Lists.newArrayList())
                .build())
                .when(clusterService)
                .getCluster("test1");

        Cluster result = tested.setCluster(null, "test1", "localhost:10000");
        log.info("result={}", result);
        assertThat(result.getClusterName(), is("test1"));
    }

    @Test
    public void changeClusterName() throws Exception {
        doReturn(Cluster.builder()
                .clusterName("test2")
                .info(Maps.newHashMap())
                .nodes(Lists.newArrayList())
                .build())
                .when(clusterService)
                .getCluster("test2");

        Cluster result = tested.changeClusterName(null, "test1", "test2");
        log.info("result={}", result);
        assertThat(result.getClusterName(), is("test2"));
    }

    @Test
    public void deleteClusterByPost() throws Exception {
        Map<String, Boolean> result = tested.deleteClusterByPost(null, "test1");
        log.info("result={}", result);
        assertThat(result.get("isSuccess"), is(true));
    }

    @Test
    public void getMetrics_start_is_not_number_then_throw_exception() throws Exception {
        expectedEx.expect(InvalidParameterException.class);
        expectedEx.expectMessage(containsString("start must be number"));

        tested.getMetrics("test1", "node1,node2", "used_memory,instantaneous_ops_per_sec", "hoge", "100");
    }

    @Test
    public void getMetrics_end_is_not_number_then_throw_exception() throws Exception {
        expectedEx.expect(InvalidParameterException.class);
        expectedEx.expectMessage(containsString("end must be number"));

        tested.getMetrics("test1", "node1,node2", "used_memory,instantaneous_ops_per_sec", "100", "hoge");
    }

    @Test
    public void getMetrics_nodes_is_empty_then_throw_exception() throws Exception {
        expectedEx.expect(InvalidParameterException.class);
        expectedEx.expectMessage(containsString("nodes is empty."));

        tested.getMetrics("test1", "", "used_memory,instantaneous_ops_per_sec", "100", "200");
    }

    @Test
    public void getMetrics_fields_is_empty_then_throw_exception() throws Exception {
        expectedEx.expect(InvalidParameterException.class);
        expectedEx.expectMessage(containsString("fields is empty."));

        tested.getMetrics("test1", "node1,node2", "", "100", "200");
    }

    @Test
    public void getMetrics() throws Exception {
        doReturn(Maps.newHashMap()).when(clusterService).getClusterStaticsInfoHistory(anyString(), anyList(), anyList(), anyLong(), anyLong());

        Object result = tested.getMetrics("test1", "node1,node2", "used_memory,instantaneous_ops_per_sec", "100", "200");
        log.info("result={}", result);
        assertThat(result, is(Maps.newHashMap()));
    }

    @Test
    public void getClusterNotice_notice_is_null_then_return_empty_notice() throws Exception {
        doReturn(null).when(clusterService).getClusterNotice("test1");

        Notice result = tested.getClusterNotice("test1");
        log.info("result={}", result);
        assertThat(result, is(new Notice()));
    }

    @Test
    public void getClusterNotice() throws Exception {
        Notice notice = new Notice();

        doReturn(notice).when(clusterService).getClusterNotice("test1");

        Notice result = tested.getClusterNotice("test1");
        log.info("result={}", result);
        assertThat(result, is(sameInstance(notice)));
    }

    @Test
    public void setClusterNotice_notice_is_blank_then_throw_exception() throws Exception {
        expectedEx.expect(InvalidParameterException.class);
        expectedEx.expectMessage(containsString("notice must not be blank"));

        tested.setClusterNotice(null, "test1", "");
    }

    @Test
    public void setClusterNotice_notice_is_invalid_format_then_throw_exception() throws Exception {
        expectedEx.expect(InvalidParameterException.class);
        expectedEx.expectMessage(containsString("notice is invalid format."));

        tested.setClusterNotice(null, "test1", "hoge");
    }

    @Test
    public void setClusterNotice() throws Exception {
        Notice notice = new Notice();

        doReturn(notice).when(clusterService).getClusterNotice("test1");

        Notice result = tested.setClusterNotice(null, "test1", "{}");
        log.info("result={}", result);
        assertThat(result, is(sameInstance(notice)));
    }

    @Test
    public void getSlowLogs_offset_is_not_numeric() throws Exception {
        expectedEx.expect(InvalidParameterException.class);
        expectedEx.expectMessage(containsString("offset must be number."));

        tested.getSlowLogs("clusterName", "hoge", "10");
    }

    @Test
    public void getSlowLogs_limit_is_not_numeric() throws Exception {
        expectedEx.expect(InvalidParameterException.class);
        expectedEx.expectMessage(containsString("limit must be number."));

        tested.getSlowLogs("clusterName", "0", "hoge");
    }

    @Test
    public void getSlowLogs() {
        // given
        PagerData<SlowLog> pagerData = new PagerData(0L, 10L, 10L, Lists.newArrayList());
        doReturn(pagerData).when(clusterService).getClusterSlowLogHistory(anyString(), anyLong(), anyLong());

        // when
        PagerData<SlowLog> result = tested.getSlowLogs("clusterName", "0", "10");

        // then
        assertThat(result, is(pagerData));
    }
}
