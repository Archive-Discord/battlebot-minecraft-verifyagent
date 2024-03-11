package kr.battlebot.battlebotverifyagent.core;

import kr.battlebot.battlebotverifyagent.BattlebotVerifyAgent;
import kr.battlebot.battlebotverifyagent.api.Agent;
import kr.battlebot.battlebotverifyagent.api.Authorization;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class AgentCore {
    private File credentialsFile = null;
    public Authorization authorization = new Authorization();
    public Agent agent = new Agent();
    public static FileConfiguration config;

    public void setCredentialsFile(File file) {
        this.credentialsFile = file;
    }

    public boolean load() {
        if (this.credentialsFile != null) {
            try {
                YamlConfiguration config = new YamlConfiguration();
                config.load(this.credentialsFile);

                this.authorization.importConfig(config);

                return true;
            } catch (IOException | InvalidConfigurationException e) {
                return false;
            }
        }

        return false;
    }

    public void start() {
        this.load();

        try {
            this.authorization.isAuthorized().get(3, TimeUnit.SECONDS);

            BattlebotVerifyAgent.logger.info("배틀이 봇 서버와 연결되었습니다.");
        } catch (Exception e) {
            BattlebotVerifyAgent.logger.warning("배틀이 봇 서버와 연결에 실패했습니다.");
        }
    }

    public boolean save() {
        if (this.credentialsFile != null) {
            BattlebotVerifyAgent.logger.info("로그인 정보 저장 중...");

            try {
                YamlConfiguration config = new YamlConfiguration();
                config.load(this.credentialsFile);

                this.authorization.exportConfig(config);

                config.save(this.credentialsFile);
                return true;
            } catch (IOException | InvalidConfigurationException e) {
                return false;
            }
        }

        return false;
    }
}
