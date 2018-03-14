package br.cin.ufpe.dass.ontologycatalog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
public class ApplicationConfig {

    private String wordnetDir;

    public String getWordnetDir() {
        return wordnetDir;
    }

    public void setWordnetDir(String wordnetDir) {
        this.wordnetDir = wordnetDir;
    }
}
