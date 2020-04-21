package com.atguigu.gmall.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.Movie;
import com.atguigu.gmall.beans.PmsSearchParam;
import com.atguigu.gmall.beans.PmsSearchSkuInfo;
import com.atguigu.gmall.service.SearchService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SearchServiceImpl implements SearchService{

    @Autowired
    JestClient jestClient;


    @Override
    public List<PmsSearchSkuInfo> search(PmsSearchParam pmsSearchParam) {

        String query = getMyQuery(pmsSearchParam);

        Search search = new Search.Builder(query).addIndex("gmallsearch").addType("pmsSearchSkuInfo").build();

        SearchResult execute = null;
        try {
            execute = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 解析搜索结果
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();
        if(execute!=null){
            List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = execute.getHits(PmsSearchSkuInfo.class);
            for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
                PmsSearchSkuInfo source = hit.source;
                // 替换高亮字段
                Map<String, List<String>> highlight = hit.highlight;
                if(highlight!=null){
                    String skuName = highlight.get("skuName").get(0);
                    source.setSkuName(skuName);
                }
                pmsSearchSkuInfos.add(source);
            }
        }



        return pmsSearchSkuInfos;
    }

    private String getMyQuery(PmsSearchParam pmsSearchParam) {

        // 查询工具
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        if(StringUtils.isNotBlank(pmsSearchParam.getCatalog3Id())){
            TermsQueryBuilder termsQueryBuilder = new TermsQueryBuilder("catalog3Id",pmsSearchParam.getCatalog3Id());// term条件过滤
            boolQueryBuilder.filter(termsQueryBuilder);
        }

        if(StringUtils.isNotBlank(pmsSearchParam.getKeyword())){
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName",pmsSearchParam.getKeyword());
            boolQueryBuilder.must(matchQueryBuilder);
        }

        if(pmsSearchParam.getValueId()!=null&&pmsSearchParam.getValueId().length>0){
            for (String valueId : pmsSearchParam.getValueId()) {
                String[] a = new String[0];
                TermsQueryBuilder termsQueryBuilder = new TermsQueryBuilder("skuAttrValueList.valueId",a);// term条件过滤

                boolQueryBuilder.filter(termsQueryBuilder);
            }
        }

        searchSourceBuilder.query(boolQueryBuilder);

        // 分页
        searchSourceBuilder.from(0);// （当前页码-1） * 每页多少条 = 起始位置
        searchSourceBuilder.size(20);

        // 高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<span style='color:red;font-weight:bolder ;'>");
        highlightBuilder.field("skuName");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlight(highlightBuilder);

        String query = searchSourceBuilder.toString();

        System.out.println(query);

        return query;
    }
}
