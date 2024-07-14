package com.t13max.game.config;

import com.t13max.game.consts.Const;
import com.t13max.game.exception.GameException;
import lombok.Data;
import org.yaml.snakeyaml.Yaml;

/**
 * Block服务端配置类
 *
 * @Author t13max
 * @Since 20:43 2024/7/14
 */
@Data
public class BlockConfig {

    //唯一静态实例
    public final static BlockConfig INSTANCE;

    private boolean test;

    static {

        Yaml yaml = new Yaml();

        INSTANCE = yaml.loadAs(BlockConfig.class.getClassLoader().getResourceAsStream(Const.CONFIG_NAME), BlockConfig.class);

        //校验配置
        if (!configCheck()) {
            throw new GameException("配置文件校验异常");
        }
    }

    public BlockConfig() {
    }

    private static boolean configCheck() {
        return true;
    }
}
