package com.android.i18n.phonenumbers;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class RegexCache {
    private LRUCache<String, Pattern> cache;

    private static class LRUCache<K, V> {
        private LinkedHashMap<K, V> map;
        private int size;

        /* renamed from: com.android.i18n.phonenumbers.RegexCache.LRUCache.1 */
        class AnonymousClass1 extends LinkedHashMap<K, V> {
            AnonymousClass1(int $anonymous0, float $anonymous1, boolean $anonymous2) {
                super($anonymous0, $anonymous1, $anonymous2);
            }

            protected boolean removeEldestEntry(Entry<K, V> entry) {
                return size() > LRUCache.this.size;
            }
        }

        public LRUCache(int size) {
            this.size = size;
            this.map = new AnonymousClass1(((size * 4) / 3) + 1, 0.75f, true);
        }

        public synchronized V get(K key) {
            return this.map.get(key);
        }

        public synchronized void put(K key, V value) {
            this.map.put(key, value);
        }

        public synchronized boolean containsKey(K key) {
            return this.map.containsKey(key);
        }
    }

    public RegexCache(int size) {
        this.cache = new LRUCache(size);
    }

    public Pattern getPatternForRegex(String regex) {
        Pattern pattern = (Pattern) this.cache.get(regex);
        if (pattern != null) {
            return pattern;
        }
        pattern = Pattern.compile(regex);
        this.cache.put(regex, pattern);
        return pattern;
    }

    boolean containsRegex(String regex) {
        return this.cache.containsKey(regex);
    }
}
