package top.niunaijun.blackbox.server.pm.installer;

import top.niunaijun.blackbox.BEnvironment;
import top.niunaijun.blackbox.entity.pm.InstallOption;
import top.niunaijun.blackbox.server.pm.BPackageSettings;
import top.niunaijun.blackbox.server.pm.BPackageUserState;
import top.niunaijun.blackbox.utils.FileUtils;

/**
 * Created by Milk on 4/24/21.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * 此处无Bug
 * 创建用户相关
 */
public class RemoveUserExecutor implements Executor {

    @Override
    public int exec(BPackageSettings ps, InstallOption option, int userId) {
        String packageName = ps.pkg.packageName;
        // delete user dir
        FileUtils.deleteDir(BEnvironment.getDataDir(packageName, userId));
        FileUtils.deleteDir(BEnvironment.getDeDataDir(packageName, userId));
        return 0;
    }
}
