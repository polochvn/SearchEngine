package searchengine.config;

import searchengine.model.Page;

import java.util.HashMap;
import java.util.Map;

public class IndexRank {
    public static class IndexRanks {
        private static float maxRAbs = 0;
        private Page page;
        private final Map<String, Float> ranks;
        private float rAbs;
        private float rRel;

        public IndexRanks() {
            ranks = new HashMap<>();
        }

        public Page getPage() {
            return page;
        }

        public void setPage(Page page) {
            this.page = page;
        }

        public Map<String, Float> getRanks() {
            return ranks;
        }

        public void setRanks(String word, Float rank) {
            ranks.put(word, rank);
        }

        public float getRAbs() {
            return rAbs;
        }

        public void setRAbs() {
            ranks.forEach((key, value) -> {
                this.rAbs += value;
            });

            if (this.rAbs > maxRAbs) {
                maxRAbs = rAbs;
            }
        }

        public float getRRel() {
            return rRel;
        }

        public void setRRel() {
            rRel = maxRAbs / rAbs;
        }
    }
}
