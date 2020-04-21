package com.atguigu.gmall.search;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.Movie;
import com.atguigu.gmall.beans.PmsSearchSkuInfo;
import com.atguigu.gmall.beans.PmsSkuInfo;
import com.atguigu.gmall.service.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallSearchServiceApplicationTests {

	@Autowired
	JestClient jestClient;

	@Reference
	SkuService skuService;

	@Test
	public void contextLoads() throws IOException {

		// 先从mysql中查询es想要的数据
		List<PmsSkuInfo> pmsSkuInfos = skuService.getSearchSkuInfo();

		// 将mysql的数据结构转化成es的数据结构
		List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();
		for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {
			PmsSearchSkuInfo pmsSearchSkuInfo = new PmsSearchSkuInfo();
			BeanUtils.copyProperties(pmsSkuInfo,pmsSearchSkuInfo);// apache的工具类

			pmsSearchSkuInfos.add(pmsSearchSkuInfo);
		}

		// 将数据导入到es中
		System.out.println(pmsSearchSkuInfos.size());
		for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {

			Index index = new Index.Builder(pmsSearchSkuInfo).index("gmallsearch").type("pmsSearchSkuInfo").id(pmsSearchSkuInfo.getId() + "").build();

			jestClient.execute(index);
		}


	}

	public void query() throws IOException {

		// 查询工具
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("name","红海");

		searchSourceBuilder.query(matchQueryBuilder);

		String query = searchSourceBuilder.toString();

		System.out.println(query);

		Search search = new Search.Builder(query).addIndex("movie_chn").addType("movie").build();

		SearchResult execute = jestClient.execute(search);

		List<Movie> movies = new ArrayList<>();

		List<SearchResult.Hit<Movie, Void>> hits = execute.getHits(Movie.class);

		for (SearchResult.Hit<Movie, Void> hit : hits) {
			Movie source = hit.source;
			System.out.println(source.getName());
			movies.add(source);
		}

		//  can not get a resource from the pool
		// protected mod no
	}

}
