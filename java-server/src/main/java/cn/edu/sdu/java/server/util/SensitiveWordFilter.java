package cn.edu.sdu.java.server.util;

import cn.edu.sdu.java.server.models.BbsSensitiveWord;
import cn.edu.sdu.java.server.repositorys.BbsSensitiveWordRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SensitiveWordFilter {

    @Autowired
    private BbsSensitiveWordRepository sensitiveWordRepository;

    private List<String> normalWords;
    private List<String> severeWords;

    @PostConstruct
    public void init() {
        refreshWords();
    }

    public void refreshWords() {
        normalWords = sensitiveWordRepository.findByLevel(1)
                .stream()
                .map(BbsSensitiveWord::getWord)
                .toList();

        severeWords = sensitiveWordRepository.findByLevel(2)
                .stream()
                .map(BbsSensitiveWord::getWord)
                .toList();
    }

    public String filterNormalWord(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        String result = content;
        for (String word : normalWords) {
            if (result.contains(word)) {
                result = result.replace(word, "***");
            }
        }
        return result;
    }

    public boolean checkSevereWord(String content) {
        if (content == null || content.isEmpty()) {
            return false;
        }
        for (String word : severeWords) {
            if (content.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
