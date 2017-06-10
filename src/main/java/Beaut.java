/**
 * Created by p9j7 on 2017/6/10.
 */

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.List;

public class Beaut implements PageProcessor {

    private Site site = Site.me().setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.89 Safari/537.36").setRetryTimes(5).setSleepTime(500).setTimeOut(10000);
    //正则匹配某个图册的链接http://www.nanrencd.cc/190015.html/2
    private static final String URL_DETAIL = "http://www\\.nanrencd\\.cc/\\d+\\.html(/\\d+)?";
    //正则匹配翻页链接http://www.nanrencd.cc/page/2
    private static final String URL_INDEX = "http://www\\.nanrencd\\.cc/(page/\\d+)?";

    @Override
    public void process(Page page) {
        if (page.getUrl().regex(URL_INDEX).match()) {
            //xpath得到列表页上所有图册的链接
            List<String> urlList = page.getHtml().xpath("//div[@class=mainleft]/ul/li/div[@class=article]/h2/a/@href").all();
            //加入到后续爬取队列
            page.addTargetRequests(urlList);
            //xpath得到列表页上下一页的链接
            List<String> urlNext = page.getHtml().xpath("//div[@class='mainleft']/div[@class='navigation container']/div[@class=pagination]/a/@href").all();
            //加入到后续爬取队列
            if (urlNext != null) {
                page.addTargetRequests(urlNext);
            }
        }

        //如果当前爬取的为某个图册页
        if (page.getUrl().regex(URL_DETAIL).match()) {
            //xpath得到图册页上的翻页链接
            List<String> nextUrl = page.getHtml().xpath("//div[@class=mainleft]//div[@class=pagelist]/ul/li/a/@href").all();
            //得到图册的标题
            String title = page.getHtml().xpath("//div[@class=mainleft]/[@class='article_container row  box']/h1/text()").toString();
            //将图册的翻页链接加入到后续爬取队列
            page.addTargetRequests(nextUrl);
            //得到图册页上的图片链接
            String url = page.getHtml().xpath("//div[@class=mainleft]//div[@class=context]//a/img/@src").toString();
            //定义本地保存的目录
            String filePath = "D:\\美图\\";
            try {
                //以标题分文件夹保存图片
                downLoadPics(url, title, filePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        //爬虫的初始入口
        String startUrl = "http://www.nanrencd.cc/";
        Spider.create(new Beaut())
                .addUrl(startUrl)
                .thread(20)
                .run();
    }

    //图片下载工具
    private static void downLoadPics(String imgUrl, String title, String filePath) throws Exception {
        // 文件路径+标题
        String dir = filePath + title;
        // 创建文件夹
        File fileDir = new File(dir);
        fileDir.mkdirs();
        URL url = new URL(imgUrl);
        // 打开网络输入流
        DataInputStream dis = new DataInputStream(url.openStream());
        //防止图片重名
        int x = (int) (Math.random() * 1000000);
        String newImageName = dir + "/" + x + "pic" + ".jpg";
        // 建立一个新的文件
        FileOutputStream fos = new FileOutputStream(new File(newImageName));
        byte[] buffer = new byte[1024];
        int length;
        // 开始下载数据
        while ((length = dis.read(buffer)) > 0) {
            fos.write(buffer, 0, length);
        }
        dis.close();
        fos.close();
    }

}
