package com.fang.base;

/**
 * Created by benren.fj on 6/20/15.
 */
public class RequestUrl {

    public static final String HOST = "http://115.29.17.79/";
    /** 信息获取的API */
    public static final String API_URL = HOST + "icoding/api.php";
    /** 接收post请求的服务器地址 */
    public static final String DEFAULT_POST_URL = HOST + "we/api.php";
    /** 接收post请求的服务器地址 测试地址*/
    public static final String DEFAULT_POST_URL_TEST = HOST + "we/api_test.php";
    /** 接收get请求的服务器地址 */
    public static final String DEFAULT_GET_URL = "";
    /** 版本更新地址 */
    public static final String VERSION_GET_URL = HOST + "we/version.php";
    /** 版本更新地址 测试地址 */
    public static final String VERSION_GET_URL_TEST = HOST + "we/version_test.php";

    /** 历史上的今天 **/
    public static final String HISTORY_OF_TODAY = HOST + "we/reading.php?id=0";

    /** 今天新闻 **/
    public static final String NEWS_OF_TODAY = HOST + "we/reading.php?id=2";
    /** 糗事百科 **/
    public static final String QIUSHI = HOST + "we/reading.php?id=1";

    /** 有意思吧 **/
    public static final String YISI = HOST + "we/reading.php?id=3";
    /** 知乎 **/
    public static final String ZHIHU = HOST + "we/reading.php?id=4";
    /** 下载链接 **/
    public static final String DOWNLOAD = HOST + "we/download.php";
    /** baidu **/
    public static final String BAIDU = "https://www.baidu.com/s?wd=";

}
