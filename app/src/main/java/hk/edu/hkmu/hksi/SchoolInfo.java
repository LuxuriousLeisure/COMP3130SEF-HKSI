package hk.edu.hkmu.hksi;

import java.util.ArrayList;
import java.util.HashMap;

public class SchoolInfo {
    public static String NAME = "name";
    public static String DISTRICT = "district";
    public static String PHONE = "phone";
    public static String ADDR = "address";
    public static String WEBSITE = "website";

    public static ArrayList<HashMap<String, String>> schoolList = new ArrayList<>();

    public static void addSchool(String name, String district, String phone, String address, String website) {
        HashMap<String, String> map = new HashMap<>();
        map.put(NAME, name);
        map.put(DISTRICT, district);
        map.put(PHONE, phone);
        map.put(ADDR, address);
        map.put(WEBSITE, website);
        schoolList.add(map);
    }
}
