package com.mm.cureenglish;

import java.util.ArrayList;
import java.util.List;

/**
 * author:       dingyu
 * date:         20-4-2
 */
public class PracticeData {
    private String title;

    private ArrayList<Item> items;

    private class Item {
        private String ch;
        private String en;

        public Item(String ch, String en) {
            this.ch = ch;
            this.en = en;
        }
    }

    public PracticeData(String title) {
        this.title = title;
        this.items = new ArrayList<>();
    }

    public void add(String ch, String en) {
        items.add(new Item(ch, en));
    }

    public String getTitle() {
        return title;
    }

    public List<String> getCnList() {
        List<String> list = new ArrayList<>();
        for (Item item : items) {
            list.add(item.ch);
        }
        return list;
    }

    public String getEn(int i) {
        return items.get(i).en;
    }
}
