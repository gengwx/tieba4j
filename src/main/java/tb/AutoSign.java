package tb;

import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 签到
 *
 * @author gengwenxuan
 */
public class AutoSign {
    //推荐创建不可变静态类成员变量
    private static final Log log = LogFactory.get();
    public static final String LIKIE_URL = "http://c.tieba.baidu.com/c/f/forum/like";
    public static final String TBS_URL = "http://tieba.baidu.com/dc/common/tbs";
    public static final String SIGN_URL = "http://c.tieba.baidu.com/c/c/forum/sign";
    public static final String PUSH_PLUS_URL = "http://www.pushplus.plus/send";
    private static final String TG_PUSH_URL = "https://api.telegram.org/bot";


    public static final String FORUM_LIST = "forum_list";
    public static final String NON_GCONFORUM = "non-gconforum";
    public static final String GCONFORUM = "gconforum";

    public static final String BDUSS = "BDUSS";
    public static final String EQUAL = "=";
    public static final String EMPTY_STR = "";
    public static final String TBS = "tbs";
    public static final String TIMESTAMP = "timestamp";
    public static final String FID = "fid";
    public static final String SIGN_KEY = "tiebaclient!!!";
    public static final String UTF8 = "utf-8";
    public static final String SIGN = "sign";
    public static final String KW = "kw";
    private static final String HAS_MORE = "has_more";



    /**
     * 已签到
     */
    public static final String SIGNED = "160002";
    /**
     * 签到失败，该吧可能已被封禁
     */
    public static final String ERROR = "340006";
    /**
     * 签到成功
     */
    public static final String SUCCESS = "0";

    public AutoSign() {
    }


    public void mainHandler(KeyValueClass kv) {
        String[] bdusses = System.getenv("BDUSS").split("#");

        if (bdusses.length == 0) {
            log.error("没有设置BDUSS");
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("本次贴吧自动签到共计").append(bdusses.length).append("个用户。");
        for (int i = 0; i < bdusses.length; i++) {
            String tbs = getTbs(bdusses[i]);
            List<JSONObject> favorite = getFavorite(bdusses[i],"1");

            StringBuilder sb = new StringBuilder();
            int successCount = 0;
            int errorCount = 0;
            int signedCount = 0;
            for (JSONObject jsonObject : favorite) {
                JSONObject signJson = clientSign(tbs, jsonObject.getStr("id"), jsonObject.getStr("name"), bdusses[i]);
                if (signJson.getStr("error_code").equals(SUCCESS)) {
                    successCount += 1;
                } else if (signJson.getStr("error_code").equals(ERROR)) {
                    errorCount += 1;
                    sb.append(jsonObject.getStr("name")).append("、");
                } else if (signJson.getStr("error_code").equals(SIGNED)) {
                    signedCount += 1;
                } else {
                    errorCount += 1;
                    sb.append(jsonObject.getStr("name")).append("、");
                }
            }

            String names = sb.toString();
            if (errorCount == 0 && StrUtil.isBlank(names)) {
                stringBuilder.append("第").append(i + 1).append("个用户签到完成，共计").append(favorite.size()).append("个贴吧，成功签到")
                        .append(successCount).append("个吧，已经签过到").append(signedCount).append("个贴吧，签到失败")
                        .append(errorCount).append("个贴吧！")
                        .append("<hr/>");
            } else if (StrUtil.isNotBlank(names) && errorCount >0) {
                names = names.substring(0, names.lastIndexOf("、"));
                stringBuilder.append("第").append(i + 1).append("个用户签到完成，共计").append(favorite.size()).append("个贴吧，成功签到")
                        .append(successCount).append("个吧，已经签过到").append(signedCount).append("个贴吧，签到失败")
                        .append(errorCount).append("个贴吧！").append("签到失败名单如下：").append(names)
                        .append("。失败原因可能是该贴吧已经被封禁！")
                        .append("<hr/>")
                ;
            }
            pushMsg(stringBuilder.toString(),errorCount);
        }
    }


    private static JSONObject clientSign(String tbs, String id, String name, String bduss) {
        log.info("开始签到贴吧：" + name);
        JSONObject data = generateSignData();
        data.set(BDUSS, bduss);
        data.set(FID, id);
        data.set(KW, name);
        data.set(TBS, tbs);
        data.set(TIMESTAMP, String.valueOf(System.currentTimeMillis() / 1000));
        encodeData(data);
        String body = HttpRequest.post(SIGN_URL).form(data)
                .execute().body();
        body = UnicodeUtil.toString(body);
        return new JSONObject(body);
    }

    /**
     * 获取收藏列表
     *
     * @return 收藏列表
     */
    private static List<JSONObject> getFavorite(String bduss,String pageNoValue) {
        log.info("开始获取关注贴吧");
        JSONObject params = new JSONObject();
        params.set(BDUSS, bduss);
        params.set("_client_type", "2");
        params.set("_client_id", "wappc_1534235498291_488");
        params.set("_client_version", "9.7.8.0");
        params.set("_phone_imei", "000000000000000");
        params.set("from", "1008621y");
        params.set("page_no", pageNoValue);
        params.set("page_size", "200");
        params.set("model", "MI+5");
        params.set("net_type", "1");
        params.set("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        params.set("vcode_tag", "11");
        encodeData(params);
        try {

            String strBody = UnicodeUtil.toString(HttpRequest.post(LIKIE_URL).form(params)
                    .execute().body());
            JSONObject body = new JSONObject(strBody);
            JSONArray nonJsonArray = new JSONArray();
            JSONArray gcoJsonArray = new JSONArray();
            List<JSONObject> finalData = new ArrayList<>();
            if (body.containsKey(FORUM_LIST)) {
                JSONObject forumJson = body.getJSONObject(FORUM_LIST);
                if (forumJson.containsKey(NON_GCONFORUM)) {
                    nonJsonArray = forumJson.getJSONArray(NON_GCONFORUM);
                }
                if (forumJson.containsKey(GCONFORUM)) {
                    gcoJsonArray = forumJson.getJSONArray(GCONFORUM);
                }
            }
            if (body.containsKey(HAS_MORE) && body.getStr(HAS_MORE).equals("1")) {
                pageNoValue += 1;
                getFavorite(bduss, pageNoValue);
            }
            processorData(nonJsonArray, finalData);
            processorData(gcoJsonArray, finalData);
            log.info("获取关注的贴吧结束");
            return finalData;
        } catch (Exception e) {
            log.error("获取关注的贴吧出错" + e);
        }
        return null;
    }

    private static void processorData(JSONArray jsonArray, List<JSONObject> finalData) {
        for (Object o : jsonArray) {
            if (o instanceof JSONArray) {
                processorData((JSONArray) o, finalData);
            } else {
                finalData.add((JSONObject) o);
            }
        }
    }

    /**
     * 获取tbs
     *
     * @return tbs
     */
    private static String getTbs(String bduss) {
        log.info("开始获取tbs");
        Map<String, String> stringStringMap = generateBaseHeaders();
        stringStringMap.put("COOKIE", String.join(EMPTY_STR, BDUSS, EQUAL, bduss));
        try {
            String body = HttpRequest.get(TBS_URL).headerMap(stringStringMap, true).execute().body();
            return new JSONObject(body).getStr(TBS);
        } catch (Exception e) {
            log.info("获取tbs出错");
            return null;
        }
    }

    /**
     * 生成加密sign并放入原数据
     *
     * @param oldData 原数据
     */
    private static void encodeData(JSONObject oldData) {
        StringBuilder sb = new StringBuilder();
        List<String> keys = new ArrayList<>(oldData.size());
        for (Map.Entry<String, Object> stringObjectEntry : oldData.entrySet()) {
            keys.add(stringObjectEntry.getKey());
        }
        keys = keys.stream().sorted().collect(Collectors.toList());
        for (String string : keys) {
            sb.append(string).append(EQUAL).append(oldData.getStr(string));
        }
        String sign = DigestUtil.md5Hex(sb.append(SIGN_KEY).toString(), UTF8).toUpperCase();
        oldData.set(SIGN, sign);
    }

    /**
     * 生成header
     *
     * @return header
     */
    private static Map<String, String> generateBaseHeaders() {
        Map<String, String> headers = new HashMap<>(2);
        headers.put("Host", "tieba.baidu.com");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36");
        return headers;
    }

    private static JSONObject generateSignData() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("_client_type", "2");
        jsonObject.set("_client_version", "9.7.8.0");
        jsonObject.set("_phone_imei", "000000000000000");
        jsonObject.set("model", "MI+5");
        jsonObject.set("net_type", "1");
        return jsonObject;
    }


    private void pushMsg(String msg,int errorCount) {
        String y = "Y";
        String pushPlusToken = System.getenv("PUSH_PLUS_TOKEN");
        String tgToken = System.getenv("TG_TOKEN");
        String tgChatId = System.getenv("TG_CHAT_ID");
        String errorSend = System.getenv("ERROR_SEND");
        if (StrUtil.isNotBlank(pushPlusToken)) {
            JSONObject msgObject = new JSONObject();
            msgObject.set("token", pushPlusToken);
            msgObject.set("title", "百度贴吧自动签到");
            msgObject.set("content", msg);
            if (StrUtil.isNotBlank(errorSend) && errorSend.equalsIgnoreCase(y)) {
                log.info("已开启errorSend开关，将进行错误识别。");
                if (errorCount > 0) {
                    log.info("已出现错误，本次将进行推送");
                    String pushPlusBody = HttpRequest.post(PUSH_PLUS_URL).body(msgObject.toString()).execute().body();
                    log.info("推送加推送完毕，推送结果为{}", pushPlusBody);
                } else {
                    log.info("未出现错误，本次不进行推送");
                }
            } else {
                log.info("未开启errorSend开关，不进行错误识别。");
                String pushPlusBody = HttpRequest.post(PUSH_PLUS_URL).body(msgObject.toString()).execute().body();
                log.info("推送加推送完毕，推送结果为{}", pushPlusBody);
            }
        }
        if (StrUtil.isNotBlank(tgToken) && StrUtil.isNotBlank(tgChatId)) {
            Map<String, Object> map = new HashMap<>(2);
            map.put("chat_id", tgChatId);
            map.put("text", "贴吧签到任务简报\n" + msg);
            if (StrUtil.isNotBlank(errorSend) && errorSend.equalsIgnoreCase(y)) {
                log.info("已开启errorSend开关，将进行错误识别。");
                if (errorCount > 0) {
                    log.info("已出现错误，本次将进行推送");
                    String tgBody = HttpRequest.post(TG_PUSH_URL+tgToken+"/sendMessage").form(map).execute().body();
                    log.info("TG推送完毕，推送结果为{}", tgBody);
                } else {
                    log.info("未出现错误，本次不进行推送");
                }
            } else {
                log.info("未开启errorSend开关，不进行错误识别。");
                String tgBody = HttpRequest.post(TG_PUSH_URL+tgToken+"/sendMessage").form(map).execute().body();
                log.info("TG推送完毕，推送结果为{}", tgBody);
            }
        }

    }
}
