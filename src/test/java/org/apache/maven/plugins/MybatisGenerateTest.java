package org.apache.maven.plugins;

import org.apache.maven.plugins.other.SqlScriptRunner;
import org.junit.Test;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.mybatis.generator.internal.ObjectFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.mybatis.generator.internal.util.ClassloaderUtility.getCustomClassloader;

/**
 * Unit test for simple App.
 */
public class MybatisGenerateTest {

    //这里是在模拟mybatis generator插件执行的过程

    @Test
    public void testGenerate() throws Exception {
        List<String> warnings = new ArrayList<String>();
        boolean overite = true;
        File configFile = new File("D:\\github-code\\mymavenplugintest\\src\\main\\resources\\generatorConfig.xml");
        ConfigurationParser parser = new ConfigurationParser(warnings);
        Configuration config = parser.parseConfiguration(configFile);
        DefaultShellCallback callback = new DefaultShellCallback(overite);
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
        myBatisGenerator.generate(null);
        //注意：这行代码中因为要实例化jdbcDriver(com.mysql.jdbc.Driver)，而Driver是在mysql-connector-java-5.1.36.jar中定义的
        //所以这行必须在用jar包构造classLoader后，才能执行，否则是找不到Driver类的
        //有了classLoader后，在构造com.mysql.jdbc.Driver类的时候，才能从classLoader中读到
        //myBatisGenerator.generate方法中有如下两行代码：
        //      ClassLoader classLoader = getCustomClassloader(configuration.getClassPathEntries());
        //      ObjectFactory.addExternalClassLoader(classLoader);
        //其中configuration.getClassPathEntries()的值就是mysql-connector-java-5.1.36.jar
        //这个参数是在generatorConfig.xml文件中classPathEntry标签中配置的信息
        runScriptIfNecessary();

    }

    /**
     * 执行一个sql脚本文件
     * @throws Exception
     */
    private void runScriptIfNecessary() throws Exception {
//        String sqlScript = "D:\\github-code\\mymavenplugintest\\src\\main\\resources\\sql\\script.sql";
        String sqlScript = "classpath:sql\\script.sql";
        String jdbcDriver = "com.mysql.jdbc.Driver";
        String jdbcURL = "jdbc:mysql://localhost:3306/xfrd?useUnicode=true&characterEncoding=UTF-8";
        String jdbcUserId = "root";
        String jdbcPassword = "root";

        SqlScriptRunner scriptRunner = new SqlScriptRunner(sqlScript,
                jdbcDriver, jdbcURL, jdbcUserId, jdbcPassword);
        scriptRunner.executeScript();
    }
}
