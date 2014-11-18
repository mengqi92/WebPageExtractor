package entity;

/**
 * Created by mengqipei on 2014/11/18 0018.
 */
public class Article {
    private String articleText; //文章文本部分
    private String articleHtml; //文章HTML字符（带标签）

    public String getArticleText() {
        return articleText;
    }

    public void setArticleText(String articleText) {
        this.articleText = articleText;
    }

    public String getArticleHtml() {
        return articleHtml;
    }

    public void setArticleHtml(String articleHtml) {
        this.articleHtml = articleHtml;
    }
}
