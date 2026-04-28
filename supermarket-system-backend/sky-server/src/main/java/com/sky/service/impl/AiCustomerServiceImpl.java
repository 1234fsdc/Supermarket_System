package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sky.dto.CustomerServiceResult;
import com.sky.service.AiCustomerServiceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI客服服务实现类 - 基于自定义RAG方案
 */
@Slf4j
@Service
public class AiCustomerServiceImpl implements AiCustomerServiceService {

    /**
     * 知识库文档实体
     */
    private static class KnowledgeDocument {
        String id;
        String question;
        String answer;
        List<String> keywords;
        String categoryId;
        List<ProductInfo> products;

        public KnowledgeDocument(String id, String question, String answer, List<String> keywords, String categoryId) {
            this.id = id;
            this.question = question;
            this.answer = answer;
            this.keywords = keywords;
            this.categoryId = categoryId;
            this.products = new ArrayList<>();
        }

        static class ProductInfo {
            String name;
            Double price;
            String desc;

            public ProductInfo(String name, Double price, String desc) {
                this.name = name;
                this.price = price;
                this.desc = desc;
            }
        }
    }

    private List<KnowledgeDocument> knowledgeBase = new ArrayList<>();

    @PostConstruct
    public void init() {
        loadKnowledgeBase();
    }

    /**
     * 从JSON文件加载知识库
     */
    private void loadKnowledgeBase() {
        try {
            ClassPathResource resource = new ClassPathResource("knowledge_base.json");
            InputStream inputStream = resource.getInputStream();
            String jsonStr = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))
                    .lines().collect(Collectors.joining("\n"));

            JSONObject root = JSON.parseObject(jsonStr);
            JSONArray categories = root.getJSONArray("categories");

            for (int i = 0; i < categories.size(); i++) {
                JSONObject category = categories.getJSONObject(i);
                String categoryId = category.getString("id");
                JSONArray documents = category.getJSONArray("documents");

                for (int j = 0; j < documents.size(); j++) {
                    JSONObject doc = documents.getJSONObject(j);
                    String id = doc.getString("id");
                    String question = doc.getString("question");
                    String answer = doc.getString("answer");
                    List<String> keywords = doc.getJSONArray("keywords").toJavaList(String.class);

                    KnowledgeDocument knowledgeDoc = new KnowledgeDocument(id, question, answer, keywords, categoryId);

                    // 加载推荐商品
                    if (doc.containsKey("products")) {
                        JSONArray products = doc.getJSONArray("products");
                        for (int k = 0; k < products.size(); k++) {
                            JSONObject p = products.getJSONObject(k);
                            knowledgeDoc.products.add(new KnowledgeDocument.ProductInfo(
                                    p.getString("name"),
                                    p.getDouble("price"),
                                    p.getString("desc")
                            ));
                        }
                    }

                    knowledgeBase.add(knowledgeDoc);
                }
            }

            log.info("知识库加载完成，共加载 {} 条知识", knowledgeBase.size());
        } catch (IOException e) {
            log.error("加载知识库失败", e);
        }
    }

    @Override
    public CustomerServiceResult getAnswer(String question) {
        if (question == null || question.trim().isEmpty()) {
            return CustomerServiceResult.builder()
                    .answer("请输入您的问题，我将为您解答。")
                    .build();
        }

        String query = question.trim().toLowerCase();

        // 检测是否为无关消息
        if (isIrrelevant(query)) {
            log.info("检测到无关消息: {}", query);
            return CustomerServiceResult.builder()
                    .answer("我主要是凡栋超市的AI客服，可以为您解答商品、订单、配送等问题，还可以为您推荐超市商品。请问有什么我可以帮您的吗？")
                    .build();
        }

        // RAG检索：关键词匹配
        int maxScore = 0;
        KnowledgeDocument bestMatch = null;

        for (KnowledgeDocument doc : knowledgeBase) {
            int score = calculateRelevance(query, doc);
            if (score > maxScore) {
                maxScore = score;
                bestMatch = doc;
            }
        }

        if (bestMatch != null && maxScore > 0) {
            log.info("匹配到答案: {} (得分: {})", bestMatch.question, maxScore);

            List<CustomerServiceResult.ProductRecommend> productRecommends = new ArrayList<>();
            for (KnowledgeDocument.ProductInfo p : bestMatch.products) {
                productRecommends.add(CustomerServiceResult.ProductRecommend.builder()
                        .name(p.name)
                        .price(p.price)
                        .desc(p.desc)
                        .build());
            }

            return CustomerServiceResult.builder()
                    .answer(bestMatch.answer)
                    .products(productRecommends)
                    .build();
        }

        // 未匹配到答案时的默认回复
        return CustomerServiceResult.builder()
                .answer("抱歉，我暂时无法回答您的问题。您可以尝试以下方式获取帮助：\n1. 拨打商家电话咨询\n2. 查看订单详情联系客服\n3. 重新描述您的问题")
                .build();
    }

    /**
     * 检测是否为无关消息
     */
    private boolean isIrrelevant(String query) {
        // 无关关键词列表
        String[] irrelevantKeywords = {
            "天气", "股票", "新闻", "电影", "游戏", "股票", "彩票", "新闻", "八卦",
            "唱歌", "跳舞", "运动", "健身", "减肥", "编程", "代码", "学习", "考试",
            "工作", "老板", "同事", "爱情", "恋爱", "结婚", "生孩子", "政治", "战争",
            "股票", "基金", "比特币", "区块链", "ai", "人工智能", "机器人", "聊天",
            "唱歌", "跳舞", "画画", "写字", "读书", "小说", "电视剧", "综艺",
            "笑话", "段子", "八卦", "绯闻", "明星", "娱乐圈", "体育", "篮球", "足球"
        };

        for (String keyword : irrelevantKeywords) {
            if (query.contains(keyword.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 计算查询与文档的相关度分数
     */
    private int calculateRelevance(String query, KnowledgeDocument doc) {
        int score = 0;

        // 1. 关键词匹配（最高权重）- 单个关键词匹配即可
        for (String keyword : doc.keywords) {
            if (query.contains(keyword.toLowerCase())) {
                score += 15; // 提高单个匹配分数
            }
        }

        // 2. 问题文本相似度
        if (query.contains(doc.question.toLowerCase()) || doc.question.toLowerCase().contains(query)) {
            score += 20;
        }

        // 3. 模糊匹配：任意关键词的部分字符匹配
        for (String keyword : doc.keywords) {
            if (keyword.length() >= 2) {
                // 只要query中包含关键词的前2个字符就算部分匹配
                String prefix = keyword.substring(0, 2);
                if (query.contains(prefix)) {
                    score += 3;
                }
            }
        }

        // 4. 推荐类问题的通用词匹配（来点、买点、看看等）
        String[] recommendTriggers = {"来点", "买点", "看看", "推荐点", "介绍一下", "有什么", "推荐", "介绍"};
        for (String trigger : recommendTriggers) {
            if (query.contains(trigger)) {
                // 检查是否属于推荐类别
                if ("recommend".equals(doc.categoryId)) {
                    score += 10;
                }
            }
        }

        return score;
    }
}
