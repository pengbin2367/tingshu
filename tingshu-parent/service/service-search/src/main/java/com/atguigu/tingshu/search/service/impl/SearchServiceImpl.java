package com.atguigu.tingshu.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.album.client.CategoryFeignClient;
import com.atguigu.tingshu.model.album.BaseCategory3;
import com.atguigu.tingshu.model.base.BaseEntity;
import com.atguigu.tingshu.model.search.AlbumInfoIndex;
import com.atguigu.tingshu.search.service.SearchService;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class SearchServiceImpl implements SearchService {

    @Resource
    private CategoryFeignClient categoryFeignClient;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @SneakyThrows
    @Override
    public Object channel(Long category1Id) {
        // 根据一级分类获取前7个三级分类数据
        List<BaseCategory3> category3List = categoryFeignClient.getBaseCategory3(category1Id);
        Map<Long, BaseCategory3> category3Map = category3List.stream().collect(Collectors.toMap(
                BaseEntity::getId,
                value -> value
        ));
        List<FieldValue> termList = category3List.stream().map(
                baseCategory3 -> new FieldValue.Builder().longValue(baseCategory3.getId()).build()
        ).collect(Collectors.toList());
        // 拼接条件
        SearchRequest.Builder builder = new SearchRequest.Builder();
        builder.index("albuminfo");
        // 将这7个分类作为查询条件，类似MySQL的in
        // 将满足的结果分桶，类似group by（每个三级分类只取前六条数据）
        builder.aggregations("aggCategory3Id",
                fn -> fn.terms(t -> t.field("category3Id"))
                        .aggregations("aggHotScore",
                                subFn -> subFn.topHits(top -> top.sort(
                                        s -> s.field(f -> f.field("hotScore").order(SortOrder.Desc))
                                )))
        );
        builder.query(query -> query.terms(
                terms -> terms.field("category3Id").terms(fn -> fn.value(termList))
        ));
        // 查出结果
        SearchResponse<AlbumInfoIndex> response = elasticsearchClient.search(builder.build(), AlbumInfoIndex.class);
        Aggregate aggregate = response.aggregations().get("aggCategory3Id");
        return aggregate.lterms().buckets().array().stream().map(buck -> {
            JSONObject result = new JSONObject();
            long category3Id = buck.key();
            result.put("baseCategory", category3Map.get(category3Id));
            List<AlbumInfoIndex> albumInfoIndexList = buck.aggregations().get("aggHotScore").topHits().hits().hits().stream().map(
                    subBuk -> subBuk.source().to(AlbumInfoIndex.class))
                    .toList();
            result.put("list", albumInfoIndexList);
            return result;
        });
    }
}
