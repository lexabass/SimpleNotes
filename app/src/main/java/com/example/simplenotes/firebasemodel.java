package com.example.simplenotes;

public class firebasemodel  {

    private String title;
    private String search;
    private String content;
    private String create_date;
    private String image;

    public firebasemodel() {

    }

    public firebasemodel(String title, String search, String content, String create_date, String image) {
        this.title = title;
        this.search = search.toLowerCase();
        this.content = content;
        this.create_date = create_date;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreate_date() {
        return create_date;
    }

    public void setCreate_date(String create_note) {
        this.create_date = create_note;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}

