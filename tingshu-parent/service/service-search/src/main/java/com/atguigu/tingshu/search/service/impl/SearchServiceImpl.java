package com.atguigu.tingshu.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.ChildScoreMode;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Suggestion;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.album.client.CategoryFeignClient;
import com.atguigu.tingshu.model.album.BaseCategory3;
import com.atguigu.tingshu.model.base.BaseEntity;
import com.atguigu.tingshu.model.search.AlbumInfoIndex;
import com.atguigu.tingshu.model.search.SuggestIndex;
import com.atguigu.tingshu.query.search.AlbumIndexQuery;
import com.atguigu.tingshu.search.service.SearchService;
import com.atguigu.tingshu.vo.search.AlbumInfoIndexVo;
import com.atguigu.tingshu.vo.search.AlbumSearchResponseVo;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;


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

    @SneakyThrows
    @Override
    public Object search(AlbumIndexQuery albumIndexQuery) {
        SearchRequest request = buidQueryParams(albumIndexQuery);
        SearchResponse<AlbumInfoIndex> response = elasticsearchClient.search(request, AlbumInfoIndex.class);
        AlbumSearchResponseVo searchResult = getSearchResult(response);
        // 设置页码相关属性
        searchResult.setPageNo(albumIndexQuery.getPageNo());
        Integer pageSize = albumIndexQuery.getPageSize();
        searchResult.setPageSize(pageSize);
        Long total = searchResult.getTotal();
        searchResult.setTotalPages(total % pageSize == 0 ? total / pageSize : total / pageSize + 1);
        return searchResult;
    }

    private SearchRequest buidQueryParams(AlbumIndexQuery albumIndexQuery) {
        SearchRequest.Builder builder = new SearchRequest.Builder();
        // 设置索引
        builder.index("albuminfo");

        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
        // 获取关键词
        String keyword = albumIndexQuery.getKeyword();
        // 构建查询条件
        if (StringUtils.isNotBlank(keyword)) {
            boolQuery.should(s1 -> s1.match(m -> m.field("albumTitle").query(keyword)))
                    .should(s2 -> s2.match(m -> m.field("albumIntro").query(keyword)));
        }
        Long category1Id = albumIndexQuery.getCategory1Id();
        if (category1Id != null) {
            boolQuery.filter(f -> f.term(t -> t.field("category1Id").value(category1Id)));
        }
        Long category2Id = albumIndexQuery.getCategory2Id();
        if (category2Id != null) {
            boolQuery.filter(f -> f.term(t -> t.field("category2Id").value(category2Id)));
        }
        Long category3Id = albumIndexQuery.getCategory3Id();
        if (category3Id != null) {
            boolQuery.filter(f -> f.term(t -> t.field("category3Id").value(category3Id)));
        }
        List<String> attributeList = albumIndexQuery.getAttributeList();
        if (attributeList != null && !attributeList.isEmpty()) {
            attributeList.forEach(attribute -> {
                String[] attrs = attribute.split(":");
                boolQuery.filter(f -> f.nested(nested -> nested
                        .path("attributeValueIndexList")
                        .query(query -> query.bool(bool -> bool
                                .must(m -> m.term(t -> t.field("attributeValueIndexList.attributeId").value(attrs[0])))
                                .must(m -> m.term(t -> t.field("attributeValueIndexList.valueId").value(attrs[1])))))
                        .scoreMode(ChildScoreMode.None)
                ));
            });
        }

        builder.query(boolQuery.build()._toQuery());
        // 设置分页属性
        Integer pageNo = albumIndexQuery.getPageNo();
        Integer pageSize = albumIndexQuery.getPageSize();
        builder.from((pageNo - 1) * pageSize);
        builder.size(pageSize);
        // 设置排序
        String order = albumIndexQuery.getOrder();
        if (StringUtils.isNotEmpty(order)) {
            String[] orders = order.split(":");
            switch (orders[0]) {
                case "1" -> builder.sort(sort -> sort.field(f -> f.field("hotScore").order(SortOrder.valueOf(orders[1]))));
                case "2" -> builder.sort(sort -> sort.field(f -> f.field("playStatNum").order(SortOrder.valueOf(orders[1]))));
                case "3" -> builder.sort(sort -> sort.field(f -> f.field("subscribeStatNum").order(SortOrder.valueOf(orders[1]))));
            }
        }
        // 高亮
        builder.highlight(high -> high.fields("albumTitle", fn -> fn.preTags("<font style=color:red>").postTags("</font>")));
        return builder.build();
    }

    private AlbumSearchResponseVo getSearchResult(SearchResponse<AlbumInfoIndex> response) {
        AlbumSearchResponseVo result = new AlbumSearchResponseVo();
        // 获取命中的数据
        List<AlbumInfoIndexVo> albumInfoIndexList = response.hits().hits().stream().map(albumInfoIndexHit -> {
            AlbumInfoIndexVo albumInfoIndexVo = new AlbumInfoIndexVo();
            AlbumInfoIndex albumInfoIndex = albumInfoIndexHit.source();
            Map<String, List<String>> highlight = albumInfoIndexHit.highlight();
            if (highlight != null && !highlight.isEmpty()) {
                List<String> albumTitleList = highlight.get("albumTitle");
                if (albumTitleList != null && !albumTitleList.isEmpty()) {
                    String title = "";
                    for (String albumTitle : albumTitleList) {
                        title += albumTitle;
                    }
                    albumInfoIndex.setAlbumTitle(title);
                }
            }
            BeanUtils.copyProperties(albumInfoIndex, albumInfoIndexVo);
            return albumInfoIndexVo;
        }).toList();
        result.setList(albumInfoIndexList);
        result.setTotal(response.hits().total() != null ? response.hits().total().value() : 0);
        return result;
    }

    @SneakyThrows
    @Override
    public Object completeSuggest(String keywords) {
        SearchRequest.Builder builder = new SearchRequest.Builder();
        builder.index("suggestinfo");
        builder.suggest(s1 -> s1
                .suggesters("suggestKeyword", s2 -> s2
                        .prefix(keywords)
                        .completion(s3 -> s3.field("keyword")       // 指定匹配的域
                                .size(10)                           // 指定匹配的数量
                                .skipDuplicates(true)               // 是否去重
                                .fuzzy(s4 -> s4.fuzziness("auto"))  // 设置偏移量：2个
                        )
                )
                .suggesters("suggestKeywordPinyin", s2 -> s2
                        .prefix(keywords)
                        .completion(s3 -> s3.field("keywordPinyin")
                                .size(10)
                                .skipDuplicates(true)
                                .fuzzy(s4 -> s4.fuzziness("auto"))
                        )
                )
                .suggesters("suggestKeywordSequence", s2 -> s2
                        .prefix(keywords)
                        .completion(s3 -> s3.field("keywordSequence")
                                .size(10)
                                .skipDuplicates(true)
                                .fuzzy(s4 -> s4.fuzziness("auto"))
                        )
                )
        );
        SearchResponse<SuggestIndex> response = elasticsearchClient.search(builder.build(), SuggestIndex.class);
        List<String> suggestKeyword = getSuggestResult(response.suggest().get("suggestKeyword"));
        List<String> suggestKeywordPinyin = getSuggestResult(response.suggest().get("suggestKeywordPinyin"));
        List<String> suggestKeywordSequence = getSuggestResult(response.suggest().get("suggestKeywordSequence"));
        return Stream.of(suggestKeyword, suggestKeywordPinyin, suggestKeywordSequence)
                .filter(Objects::nonNull).flatMap(Collection::stream).collect(Collectors.toSet())
                .stream().limit(10).collect(Collectors.toList());
    }

    private List<String> getSuggestResult(List<Suggestion<SuggestIndex>> suggestKeyword) {
        return suggestKeyword.get(0).completion().options()
                .stream().map(s -> s.source().getTitle())
                .collect(Collectors.toList());
    }
}
