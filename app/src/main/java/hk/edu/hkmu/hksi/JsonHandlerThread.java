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
    private static final String JSON_URL = "https://www.edb.gov.hk/attachment/en/student-parents/sch-info/sch-search/sch-location-info/SCH_LOC_EDB.json";

    @Override
    public void run() {
        String jsonStr = makeRequest();
        if (jsonStr == null) return;
        try {
            JSONArray array = new JSONArray(jsonStr);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                // 完整解析所有中英文对应字段（和API key完全匹配）
                String name_cn = obj.getString("中文名稱");
                String name_en = obj.getString("ENGLISH NAME");
                String addr_cn = obj.getString("中文地址");
                String addr_en = obj.getString("ENGLISH ADDRESS");
                String district_cn = obj.getString("分區");
                String district_en = obj.getString("DISTRICT");
                String phone = obj.getString("聯絡電話");
                String fax = obj.getString("傳真號碼");
                String website = obj.getString("WEBSITE");
                String schoolLevel_cn = obj.getString("學校類型");
                String schoolLevel_en = obj.getString("SCHOOL LEVEL");
                String gender_cn = obj.getString("就讀學生性別");
                String gender_en = obj.getString("STUDENTS GENDER");
                String finance_cn = obj.getString("資助種類");
                String finance_en = obj.getString("FINANCE TYPE");
                String lat = obj.getString("緯度");
                String lng = obj.getString("經度");
                String religion_cn = obj.getString("宗教");
                String religion_en = obj.getString("RELIGION");
                String session_cn = obj.getString("學校授課時間");
                String session_en = obj.getString("SESSION");

                // 入库
                SchoolInfo.addSchool(
                        name_cn, name_en,
                        addr_cn, addr_en,
                        district_cn, district_en,
                        phone, fax, website,
                        schoolLevel_cn, schoolLevel_en,
                        gender_cn, gender_en,
                        finance_cn, finance_en,
                        lat, lng,
                        religion_cn, religion_en,
                        session_cn, session_en
                );
            }
        } catch (Exception e) {
            Log.e(TAG, "JSON解析错误："+e.getMessage());
        }
    }

    private String makeRequest() {
        try {
            URL url = new URL(JSON_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            InputStream in = new BufferedInputStream(conn.getInputStream());
            return streamToString(in);
        } catch (Exception e) {
            Log.e(TAG, "网络请求错误："+e.getMessage());
            return null;
        }
    }

    private String streamToString(InputStream is) {
        StringBuilder sb = new StringBuilder();
        try {
            InputStreamReader reader = new InputStreamReader(is, "UTF-8");
            char[] buffer = new char[1024];
            int len;
            while ((len = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, len);
            }
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}