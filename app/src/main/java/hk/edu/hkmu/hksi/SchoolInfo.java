package hk.edu.hkmu.hksi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class SchoolInfo {
    // ====================== 核心字段定义（和API返回key100%匹配） ======================
    // 名称字段（搜索+收藏用，固定唯一）
    public static final String NAME_CN = "name_cn";   // 对应API 中文名稱
    public static final String NAME_EN = "name_en";   // 对应API ENGLISH NAME
    // 地址字段
    public static final String ADDR_CN = "addr_cn";   // 对应API 中文地址
    public static final String ADDR_EN = "addr_en";   // 对应API ENGLISH ADDRESS
    // 分区字段
    public static final String DISTRICT_CN = "district_cn"; // 对应API 分區
    public static final String DISTRICT_EN = "district_en"; // 对应API DISTRICT
    // 通用字段（中英文一致，无需切换）
    public static final String PHONE = "phone";       // 对应API 聯絡電話
    public static final String FAX = "fax";           // 对应API 傳真號碼
    public static final String WEBSITE = "website";   // 对应API WEBSITE
    public static final String LATITUDE = "latitude"; // 对应API 緯度
    public static final String LONGITUDE = "longitude";// 对应API 經度

    // 筛选字段（中文key固定用于后台匹配，保证筛选稳定，UI显示随语言切换）
    public static final String SCHOOL_LEVEL_CN = "學校類型";
    public static final String SCHOOL_LEVEL_EN = "SCHOOL LEVEL";
    public static final String STUDENTS_GENDER_CN = "就讀學生性別";
    public static final String STUDENTS_GENDER_EN = "STUDENTS GENDER";
    public static final String FINANCE_TYPE_CN = "資助種類";
    public static final String FINANCE_TYPE_EN = "FINANCE TYPE";

    // 扩展字段（详情页用）
    public static final String RELIGION_CN = "宗教";
    public static final String RELIGION_EN = "RELIGION";
    public static final String SESSION_CN = "學校授課時間";
    public static final String SESSION_EN = "SESSION";

    // 全量学校数据集合
    public static ArrayList<HashMap<String, String>> schoolList = new ArrayList<>();

    // ====================== 完善数据入库方法 ======================
    public static void addSchool(
            // 名称
            String name_cn, String name_en,
            // 地址
            String addr_cn, String addr_en,
            // 分区
            String district_cn, String district_en,
            // 通用联系信息
            String phone, String fax, String website,
            // 筛选字段
            String schoolLevel_cn, String schoolLevel_en,
            String gender_cn, String gender_en,
            String finance_cn, String finance_en,
            // 经纬度
            String lat, String lng,
            // 扩展字段
            String religion_cn, String religion_en,
            String session_cn, String session_en
    ) {
        HashMap<String, String> map = new HashMap<>();
        // 名称
        map.put(NAME_CN, name_cn);
        map.put(NAME_EN, name_en);
        // 地址
        map.put(ADDR_CN, addr_cn);
        map.put(ADDR_EN, addr_en);
        // 分区
        map.put(DISTRICT_CN, district_cn);
        map.put(DISTRICT_EN, district_en);
        // 通用信息
        map.put(PHONE, phone);
        map.put(FAX, fax);
        map.put(WEBSITE, website);
        // 筛选字段
        map.put(SCHOOL_LEVEL_CN, schoolLevel_cn);
        map.put(SCHOOL_LEVEL_EN, schoolLevel_en);
        map.put(STUDENTS_GENDER_CN, gender_cn);
        map.put(STUDENTS_GENDER_EN, gender_en);
        map.put(FINANCE_TYPE_CN, finance_cn);
        map.put(FINANCE_TYPE_EN, finance_en);
        // 经纬度
        map.put(LATITUDE, lat);
        map.put(LONGITUDE, lng);
        // 扩展字段
        map.put(RELIGION_CN, religion_cn);
        map.put(RELIGION_EN, religion_en);
        map.put(SESSION_CN, session_cn);
        map.put(SESSION_EN, session_en);

        schoolList.add(map);
    }

    // 清空数据（筛选专用）
    public static void clearList() {
        schoolList.clear();
    }

    // ====================== 核心：语言适配工具方法 ======================
    // 判断当前是否为中文模式
    private static boolean isZhMode() {
        return Locale.getDefault().getLanguage().equals("zh");
    }

    // 根据当前语言，返回对应字段的key（UI统一调用，自动切换）
    public static String getNameKey() { return isZhMode() ? NAME_CN : NAME_EN; }
    public static String getAddrKey() { return isZhMode() ? ADDR_CN : ADDR_EN; }
    public static String getDistrictKey() { return isZhMode() ? DISTRICT_CN : DISTRICT_EN; }
    public static String getSchoolLevelKey() { return isZhMode() ? SCHOOL_LEVEL_CN : SCHOOL_LEVEL_EN; }
    public static String getGenderKey() { return isZhMode() ? STUDENTS_GENDER_CN : STUDENTS_GENDER_EN; }
    public static String getFinanceKey() { return isZhMode() ? FINANCE_TYPE_CN : FINANCE_TYPE_EN; }
    public static String getReligionKey() { return isZhMode() ? RELIGION_CN : RELIGION_EN; }
    public static String getSessionKey() { return isZhMode() ? SESSION_CN : SESSION_EN; }
}