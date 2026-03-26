package hk.edu.hkmu.hksi;

import java.util.ArrayList;
import java.util.HashMap;

public class SchoolInfo {
    // 原有列表展示字段
    public static String NAME = "name";
    public static String DISTRICT = "district";
    public static String PHONE = "phone";
    public static String ADDR = "address";
    public static String WEBSITE = "website";

    // ✅ 修复：筛选字段 = JSON 原生繁体中文字段（100%匹配）
    public static final String SCHOOL_LEVEL = "學校類型";       // 小學/中學
    public static final String STUDENTS_GENDER = "就讀學生性別"; // 男女/男/女
    public static final String FINANCE_TYPE = "資助種類";       // 資助/官立/私立

    public static ArrayList<HashMap<String, String>> schoolList = new ArrayList<>();

    // ✅ 修复：新增带筛选参数的方法（解析JSON时必须用这个！）
    public static void addSchool(
            String name, String district, String phone,
            String address, String website,
            String schoolType, String gender, String finance  // 新增3个筛选字段
    ) {
        HashMap<String, String> map = new HashMap<>();
        // 原有数据
        map.put(NAME, name);
        map.put(DISTRICT, district);
        map.put(PHONE, phone);
        map.put(ADDR, address);
        map.put(WEBSITE, website);
        // ✅ 关键：存入筛选数据
        map.put(SCHOOL_LEVEL, schoolType);
        map.put(STUDENTS_GENDER, gender);
        map.put(FINANCE_TYPE, finance);

        schoolList.add(map);
    }

    // 清空数据（用于筛选）
    public static void clearList() {
        schoolList.clear();
    }
}