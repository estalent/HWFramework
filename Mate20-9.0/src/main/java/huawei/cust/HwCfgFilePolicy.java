package huawei.cust;

import android.common.HwFrameworkFactory;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

public class HwCfgFilePolicy {
    public static final int BASE = 3;
    private static String[] CFG_DIRS = null;
    public static final int CLOUD_APN = 7;
    public static final int CLOUD_DPLMN = 6;
    public static final int CLOUD_MCC = 5;
    public static final int CUST = 4;
    public static final int CUST_TYPE_CONFIG = 0;
    public static final int CUST_TYPE_MEDIA = 1;
    public static final int DEFAULT_SLOT = -2;
    public static final int EMUI = 1;
    public static final int GLOBAL = 0;
    public static final String HW_ACTION_CARRIER_CONFIG_CHANGED = "com.huawei.action.CARRIER_CONFIG_CHANGED";
    public static final String HW_CARRIER_CONFIG_CHANGE_STATE = "state";
    public static final String HW_CARRIER_CONFIG_OPKEY = "opkey";
    public static final String HW_CARRIER_CONFIG_SLOT = "slot";
    public static final int HW_CONFIG_STATE_PARA_UPDATE = 3;
    public static final int HW_CONFIG_STATE_SIM_ABSENT = 2;
    public static final int HW_CONFIG_STATE_SIM_LOADED = 1;
    private static String[] MEDIA_DIRS = null;
    public static final int PC = 2;
    private static String TAG = "CfgFilePolicy";
    private static final int TXTSECTION = 2;
    private static final String[] VERSION_MARK = {"global_cfg_version", "emui_cfg_version", "pc_cfg_version", " ", "carrier_cfg_version"};
    private static IHwCarrierConfigPolicy hwCarrierConfigPolicy = HwFrameworkFactory.getHwCarrierConfigPolicy();
    private static HashMap<String, String> mCfgVersions = new HashMap<>();
    private static int mCotaFlag = 0;

    static {
        String policy = System.getenv("CUST_POLICY_DIRS");
        if (policy == null || policy.length() == 0) {
            Log.e(TAG, "****ERROR: env CUST_POLICY_DIRS not set, use default");
            policy = "/system/emui:/system/global:/system/etc:/oem:/data/cust:/cust_spec";
        }
        refreshCustDirPolicy(policy);
    }

    public static ArrayList<File> getCfgFileList(String fileName, int type) throws NoClassDefFoundError {
        return getCfgFileListCommon(fileName, type, -2);
    }

    public static ArrayList<File> getCfgFileList(String fileName, int type, int slotId) throws NoClassDefFoundError {
        return getCfgFileListCommon(fileName, type, slotId);
    }

    private static ArrayList<File> getCfgFileListCommon(String fileName, int type, int slotId) throws NoClassDefFoundError {
        ArrayList<File> res = new ArrayList<>();
        if (fileName == null || fileName.length() == 0) {
            Log.e(TAG, "Error: file = [" + fileName + "]");
            return res;
        }
        String[] dirs = getCfgPolicyDir(type, slotId);
        for (String file : dirs) {
            File file2 = new File(file, fileName);
            if (file2.exists()) {
                res.add(file2);
            }
        }
        return res;
    }

    public static File getCfgFile(String fileName, int type) throws NoClassDefFoundError {
        return getCfgFileCommon(fileName, type, -2);
    }

    public static File getCfgFile(String fileName, int type, int slotId) throws NoClassDefFoundError {
        return getCfgFileCommon(fileName, type, slotId);
    }

    private static File getCfgFileCommon(String fileName, int type, int slotId) throws NoClassDefFoundError {
        String[] dirs = getCfgPolicyDir(type, slotId);
        for (int i = dirs.length - 1; i >= 0; i--) {
            File file = new File(dirs[i], fileName);
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }

    public static String[] getCfgPolicyDir(int type) throws NoClassDefFoundError {
        return getCfgPolicyDirCommon(type, -2);
    }

    public static String[] getCfgPolicyDir(int type, int slotId) throws NoClassDefFoundError {
        return getCfgPolicyDirCommon(type, slotId);
    }

    private static String[] getCfgPolicyDirCommon(int type, int slotId) throws NoClassDefFoundError {
        String[] dirs;
        if (mCotaFlag != 1) {
            File custPolicyDirsFile = new File("/data/cota/cota_cfg/cust_policy_dirs.cfg");
            if (custPolicyDirsFile.exists()) {
                refreshCustDirPolicy(getCustPolicyDirs(custPolicyDirsFile));
            }
        }
        if (type == 1) {
            dirs = (String[]) MEDIA_DIRS.clone();
        } else {
            dirs = (String[]) CFG_DIRS.clone();
        }
        if (slotId != -2) {
            return parseCarrierPath(dirs, getOpKey(slotId));
        }
        try {
            return parseCarrierPath(dirs, getOpKey());
        } catch (Exception e) {
            Log.e(TAG, "parseCarrierPath fail.");
            return dirs;
        }
    }

    public static String getCfgVersion(int cfgType) throws NoClassDefFoundError {
        String version = null;
        switch (cfgType) {
            case 0:
            case 1:
            case 2:
            case 4:
                if (!mCfgVersions.containsKey(VERSION_MARK[cfgType])) {
                    initFileVersions(getCfgFileList("version.txt", 0));
                }
                return mCfgVersions.get(VERSION_MARK[cfgType]);
            case 3:
                return SystemProperties.get("ro.product.BaseVersion", null);
            case 5:
                String[] mccInfo = getDownloadCfgFile("/cloud/mcc", "cloud/mcc/version.txt");
                if (mccInfo != null) {
                    version = mccInfo[1];
                }
                return version;
            case 6:
                String[] dplmnInfo = getDownloadCfgFile("/cloud/dplmn", "cloud/dplmn/version.txt");
                if (dplmnInfo != null) {
                    version = dplmnInfo[1];
                }
                return version;
            case 7:
                String[] apnInfo = getDownloadCfgFile("/cloud/apn", "cloud/apn/version.txt");
                if (apnInfo != null) {
                    version = apnInfo[1];
                }
                return version;
            default:
                return null;
        }
    }

    private static void initFileVersions(ArrayList<File> cfgFileList) {
        Iterator<File> it = cfgFileList.iterator();
        while (it.hasNext()) {
            String[] versions = getVersionsFromFile(it.next());
            if (versions != null) {
                String oldversion = mCfgVersions.get(versions[0]);
                if (oldversion == null || oldversion.compareTo(versions[1]) < 0) {
                    mCfgVersions.put(versions[0], versions[1]);
                }
            }
        }
    }

    private static String[] getVersionsFromFile(File file) {
        Scanner sc = null;
        try {
            Scanner sc2 = new Scanner(file, "UTF-8");
            while (sc2.hasNextLine()) {
                String[] versions = sc2.nextLine().split("=");
                if (2 == versions.length) {
                    sc2.close();
                    return versions;
                }
            }
            sc2.close();
            Log.e(TAG, "version file format is wrong.");
            return null;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "version file is not found.");
            if (sc != null) {
                sc.close();
            }
            return null;
        } catch (NullPointerException e2) {
            Log.e(TAG, "version file format is wrong.");
            if (sc != null) {
                sc.close();
            }
            return null;
        } catch (Throwable th) {
            if (sc != null) {
                sc.close();
            }
            throw th;
        }
    }

    private static String[] getFileInfo(String baseDir, String verDir, String filePath) {
        File cfgPath = new File(baseDir, filePath);
        if (!cfgPath.exists()) {
            return null;
        }
        String[] info = {cfgPath.getPath(), ""};
        String[] vers = getVersionsFromFile(new File(baseDir, verDir + "/version.txt"));
        if (vers != null) {
            info[1] = vers[1];
        }
        return info;
    }

    public static String[] getDownloadCfgFile(String verDir, String filePath) throws NoClassDefFoundError {
        String[] cotaInfo = getFileInfo("/data/cota/para/", verDir, filePath);
        for (String dir : getCfgPolicyDir(0)) {
            String[] info = getFileInfo(dir, verDir, filePath);
            if (info != null && (cotaInfo == null || info[1].compareTo(cotaInfo[1]) > 0)) {
                cotaInfo = info;
            }
        }
        return cotaInfo;
    }

    public static String getOpKey() {
        if (hwCarrierConfigPolicy != null) {
            return hwCarrierConfigPolicy.getOpKey();
        }
        Log.e(TAG, "Error: hwCarrierConfigPolicy is null");
        return null;
    }

    public static String getOpKey(int slotId) {
        if (hwCarrierConfigPolicy != null) {
            return hwCarrierConfigPolicy.getOpKey(slotId);
        }
        Log.e(TAG, "Error: hwCarrierConfigPolicy is null");
        return null;
    }

    public static <T> T getValue(String key, Class<T> clazz) {
        if (hwCarrierConfigPolicy != null) {
            return hwCarrierConfigPolicy.getValue(key, clazz);
        }
        Log.e(TAG, "Error: hwCarrierConfigPolicy is null");
        return null;
    }

    public static <T> T getValue(String key, int slotId, Class<T> clazz) {
        if (hwCarrierConfigPolicy != null) {
            return hwCarrierConfigPolicy.getValue(key, slotId, clazz);
        }
        Log.e(TAG, "Error: hwCarrierConfigPolicy is null");
        return null;
    }

    public static Map getFileConfig(String fileName) {
        if (hwCarrierConfigPolicy != null) {
            return hwCarrierConfigPolicy.getFileConfig(fileName);
        }
        Log.e(TAG, "Error: hwCarrierConfigPolicy is null");
        return null;
    }

    public static Map getFileConfig(String fileName, int slotId) {
        if (hwCarrierConfigPolicy != null) {
            return hwCarrierConfigPolicy.getFileConfig(fileName, slotId);
        }
        Log.e(TAG, "Error: hwCarrierConfigPolicy is null");
        return null;
    }

    private static String[] parseCarrierPath(String[] dirs, String opKey) {
        if (opKey == null) {
            return (String[]) dirs.clone();
        }
        ArrayList<String> paths = new ArrayList<>();
        for (int i = 0; i < dirs.length; i++) {
            paths.add(dirs[i]);
            if (new File(dirs[i], "carrier").exists()) {
                paths.add(dirs[i] + "/carrier/" + opKey);
            }
        }
        return (String[]) paths.toArray(new String[0]);
    }

    private static void refreshCustDirPolicy(String policy) {
        if (!TextUtils.isEmpty(policy)) {
            CFG_DIRS = policy.split(":");
            MEDIA_DIRS = (String[]) CFG_DIRS.clone();
            for (int i = 0; i < MEDIA_DIRS.length; i++) {
                if (MEDIA_DIRS[i].endsWith("/etc") && !MEDIA_DIRS[i].equals("/etc")) {
                    MEDIA_DIRS[i] = MEDIA_DIRS[i].replace("/etc", "");
                }
            }
        }
    }

    private static String getCustPolicyDirs(File file) {
        Scanner sc = null;
        try {
            Scanner sc2 = new Scanner(file, "UTF-8");
            if (sc2.hasNextLine()) {
                mCotaFlag = 1;
                Log.d(TAG, "CustPolicyDirs file is found");
                String nextLine = sc2.nextLine();
                sc2.close();
                return nextLine;
            }
            sc2.close();
            Log.e(TAG, "CustPolicyDirs file format is wrong.");
            return null;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "CustPolicyDirs file is not found.");
            if (sc != null) {
                sc.close();
            }
            return null;
        } catch (NullPointerException e2) {
            Log.e(TAG, "CustPolicyDirs file format is wrong.");
            if (sc != null) {
                sc.close();
            }
            return null;
        } catch (Throwable th) {
            if (sc != null) {
                sc.close();
            }
            throw th;
        }
    }
}
