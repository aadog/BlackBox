package top.niunaijun.blackbox.server.pm.installer;

import top.niunaijun.blackbox.BEnvironment;
import top.niunaijun.blackbox.entity.pm.InstallOption;
import top.niunaijun.blackbox.server.pm.BPackageSettings;
import top.niunaijun.blackbox.utils.FileUtils;

/**
 * Created by Milk on 4/27/21.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * 此处无Bug
 */
public class RemoveAppExecutor implements Executor {
    @Override
    public int exec(BPackageSettings ps, InstallOption option, int userId) {
        FileUtils.deleteDir(BEnvironment.getAppDir(ps.pkg.packageName));
        return 0;
    }
}
