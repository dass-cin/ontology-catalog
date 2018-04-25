package br.cin.ufpe.dass.ontologycatalog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
public class ApplicationConfig {

    private Initialization initialization = new Initialization();

    public static class Initialization {
        private String wordnetDir = "/usr/local/Cellar/wordnet/3.1/dict";

        private String stopWordsFile = "normalizer/stopwords";;

        public String getWordnetDir() {
            return wordnetDir;
        }

        public void setWordnetDir(String wordnetDir) {
            this.wordnetDir = wordnetDir;
        }

        public String getStopWordsFile() {
            return stopWordsFile;
        }

        public void setStopWordsFile(String stopWordsFile) {
            this.stopWordsFile = stopWordsFile;
        }

    }

    public Initialization getInitialization() {
        return initialization;
    }

    public void setInitialization(Initialization initialization) {
        this.initialization = initialization;
    }

}
