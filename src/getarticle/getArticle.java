package getarticle;

import entity.Article;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * Created by mengqipei on 2014/11/17 0017.
 */
public class getArticle {
    private static int _depth = 6;          //抽取深度
    private static int _limitCount = 180;   //文本字符阈值，当分析的文本数量超出此阈值数则认为已进入文本内容
    private static int _headEmptyLines = 2; //连续两行空行则认为是文章起始部分
    private static int _endLimitCharCount = 20; //用于确定文章结束的字符数

    //从给定HTML中抽取正文文章
    public static Article getArticle(String url) {
        Document document = null;
        try {
            document = Jsoup.connect(url).timeout(20 * 1000).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Elements elements = document.select("body");
        String html = elements.html();
        //如果换行符小于10个，则认为HTML为压缩后的HTML
        //为HTML标签添加换行符以便后续处理
        if (html.length() - html.replace("\n", "").length() < 10)
            html = html.replace(">", ">\n");

        //过滤掉script标签
        html = html.replaceAll("(?is)<script.*?>.*?</script>", "");
        //过滤掉style标签
        html = html.replaceAll("(?is)<style.*?>.*?</style>", "");
        //过滤掉注释
        html = html.replaceAll("(?is)<!--.*?-->", "");

//        //标签规整化处理
//        html = html.replaceAll("(<+)\\s*\n\\s*", "");

        return getContent(html);
    }

    private static Article getContent(String html) {
        String[] linesHTML = null;
        String[] linesText = null;

        linesHTML = html.split("\n");
        linesText = new String[linesHTML.length];

        for (int i = 0; i < linesHTML.length; i++) {
            String lineInfo = linesHTML[i];
            //使用"[crlf]"作为回车标记符
            lineInfo = lineInfo.replaceAll("(?is)</p>|<br.*?/>", "[crlf]");
            //TODO: "<code>"便签代码段要保留

            //剔除标签
            linesText[i] = lineInfo.replaceAll("(?is)<.*?>", "").trim();
        }

        String articleText = "";
        String articleHTML = "";

        int preTextLength = 0;
        int startPos = -1;
        for (int lineIndex = 0; lineIndex < linesText.length - _depth; lineIndex++) {
            int len = 0;
            for (int pointer = 0; pointer < _depth; pointer++) {
                len += linesText[lineIndex + pointer].length();
            }

            //还没有找到文章起始位置，需要判断起始位置
            if (startPos == -1) {
                //上次查找文本数量超过阈值，且当前行非空行，则认为是开始位置
                if (preTextLength > _limitCount && len > 0) {
                    int emptyCount = 0;
                    //向上查找，发现 2 行连续空行则认为是文章起始位置
                    for (int j = lineIndex - 1; j > 0; j--) {
                        if (linesText[j].isEmpty())
                            emptyCount++;
                        else
                            emptyCount = 0;

                        if (emptyCount == _headEmptyLines) {
                            startPos = j + _headEmptyLines;
                            break;
                        }
                    }

                    //没有定位到文章起始位置，以当前查找位置为文章起始
                    if (startPos == -1) {
                        startPos = lineIndex;
                    }

                    //填充文章的起始部分
                    for (int i = startPos; i < lineIndex; i++) {
                        articleText += linesText[i];
                        articleHTML += linesHTML[i];
                    }
                }
            } else {    //已找到文章起始位置

                //当前长度过小，且上一个长度也过小，则认为已经结束
                if (len <= _endLimitCharCount && preTextLength < _endLimitCharCount) {
                    startPos = -1;
                }
                articleText += linesText[lineIndex];
                articleHTML += linesHTML[lineIndex];

            }
            preTextLength = len;
        }

        articleText = articleText.replaceAll("\\[crlf\\]", "\n");
        Article result = new Article();
        result.setArticleText(articleText);
        result.setArticleHtml(articleHTML);
        return result;
    }

    public static void main(String[] args) {
        //http://jsoup.org/
        //http://blog.jobbole.com/38493/
        Article article = getArticle("http://news.sohu.com/20131229/n392604462.shtml");
        System.out.println("正文文本：\n" + article.getArticleText());
        System.out.println("正文HTML: \n" + article.getArticleHtml());

//        System.out.println("<a\nhref='http://www.baidu.com'\nclass='test'>");
//        System.out.println(("<a\nhref='http://www.baidu.com'\nclass='test'>").replaceAll("(<+)\\s*\n\\s*", "dd"));
    }
}
