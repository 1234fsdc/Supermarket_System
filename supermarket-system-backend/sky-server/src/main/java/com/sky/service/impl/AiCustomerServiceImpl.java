package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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

        public KnowledgeDocument(String id, String question, String answer, List<String> keywords, String categoryId) {
            this.id = id;
            this.question = question;
            this.answer = answer;
            this.keywords = keywords;
            this.categoryId = categoryId;
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

                    knowledgeBase.add(new KnowledgeDocument(id, question, answer, keywords, categoryId));
                }
            }

            log.info("知识库加载完成，共加载 {} 条知识", knowledgeBase.size());
        } catch (IOException e) {
            log.error("加载知识库失败", e);
        }
    }

    @Override
    public String getAnswer(String question) {
        if (question == null || question.trim().isEmpty()) {
            return "请输入您的问题，我将为您解答。";
        }

        String query = question.trim();

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
            return bestMatch.answer;
        }

        // 未匹配到答案时的默认回复
        return "抱歉，我暂时无法回答您的问题。您可以尝试以下方式获取帮助：\n1. 拨打商家电话咨询\n2. 查看订单详情联系客服\n3. 重新描述您的问题";
    }

    /**
     * 计算查询与文档的相关度分数
     */
    private int calculateRelevance(String query, KnowledgeDocument doc) {
        int score = 0;

        // 1. 关键词匹配（最高权重）
        for (String keyword : doc.keywords) {
            if (query.contains(keyword)) {
                score += 10;
            }
        }

        // 2. 问题文本相似度
        if (query.contains(doc.question) || doc.question.contains(query)) {
            score += 20;
        }

        // 3. 分词匹配
        String[] queryWords = query.split("");
        for (String word : queryWords) {
            if (doc.question.contains(word) && word.length() > 1) {
                score += 2;
            }
            for (String keyword : doc.keywords) {
                if (keyword.contains(word) && word.length() > 1) {
                    score += 1;
                }
            }
        }

        return score;
    }
}
