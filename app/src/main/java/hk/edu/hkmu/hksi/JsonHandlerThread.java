package hk.edu.hkmu.hksi;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class JsonHandlerThread extends Thread {
    private static final String TAG = "JsonHandler";
    // 👇 API地址
    private static String jsonUrl = "https://www.edb.gov.hk/attachment/en/student-parents/sch-info/sch-search/sch-location-info/SCH_LOC_EDB.json";

    @Override
    public void run() {
        String jsonStr = makeRequest();
        if (jsonStr == null) return;

        try {
            // 教育局API直接返回JSON数组，没有外层对象
            JSONArray array = new JSONArray(jsonStr);
            for (int i=0; i<array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                // 👇 解析学校字段（和你给的示例完全对应）
                String name = obj.getString("中文名稱");
                String district = obj.getString("分區");
                String phone = obj.getString("聯絡電話");
                String addr = obj.getString("中文地址");
                String website = obj.getString("WEBSITE");
                String schoolType = obj.getString("學校類型");
                String gender = obj.getString("就讀學生性別");
                String finance = obj.getString("資助種類");

                SchoolInfo.addSchool(name, district, phone, addr, website, schoolType, gender, finance);
            }
        } catch (Exception e) {
            Log.e(TAG, "解析错误："+e.getMessage());
        }
    }

    public static String makeRequest() {
        try {
            URL url = new URL(jsonUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            InputStream in = new BufferedInputStream(conn.getInputStream());
            return streamToString(in);
        } catch (Exception e) {
            Log.e(TAG, "请求错误："+e.getMessage());
            return null;
        }
    }

    private static String streamToString(InputStream is) {
        StringBuilder sb = new StringBuilder();
        try {
            InputStreamReader reader = new InputStreamReader(is);
            char[] buffer = new char[1024];
            int len;
            while ((len = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, len);
            }
            is.close();
        } catch (Exception e) {}
        return sb.toString();
    }
}