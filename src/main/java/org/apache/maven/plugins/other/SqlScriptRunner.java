/**
 *    Copyright 2006-2017 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.maven.plugins.other;

import org.mybatis.generator.internal.ObjectFactory;
import org.mybatis.generator.internal.util.StringUtility;

import java.io.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * This class is used to execute an SQL script before a code generation
 * run if necessary.  Note that this class mainly exists to support the
 * MyBatis Generator build.  It is intentionally not documented and not
 * supported.
 * 
 * @author Jeff Butler
 */
public class SqlScriptRunner {
    private String driver;
    private String url;
    private String userid;
    private String password;
    private String sourceFile;

    public SqlScriptRunner(String sourceFile, String driver, String url,
                           String userId, String password) throws Exception {
        
        this.sourceFile = sourceFile;
        this.driver = driver;
        this.url = url;
        this.userid = userId;
        this.password = password;
    }

    public void executeScript() throws Exception {

        Connection connection = null;

        try {
            Class<?> driverClass = ObjectFactory.externalClassForName(driver);
            Driver theDriver = (Driver) driverClass.newInstance();
            
            Properties properties = new Properties();
            if (userid != null) {
                properties.setProperty("user", userid);
            }
            
            if (password != null) {
                properties.setProperty("password", password);
            }
            
            connection = theDriver.connect(url, properties);
            connection.setAutoCommit(false);

            Statement statement = connection.createStatement();

            BufferedReader br = getScriptReader();

            String sql;

            while ((sql = readStatement(br)) != null) {
                statement.execute(sql);
            }

            closeStatement(statement);
            connection.commit();
            br.close();
        } catch (ClassNotFoundException e) {
            throw new Exception("Class not found: " + e.getMessage());
        } finally {
            closeConnection(connection);
        }
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {

            }
        }
    }

    private void closeStatement(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {

            }
        }
    }

    private String readStatement(BufferedReader br) throws IOException {
        StringBuilder sb = new StringBuilder();

        String line;

        while ((line = br.readLine()) != null) {
            if (line.startsWith("--")) { //$NON-NLS-1$
                continue;
            }

            if (!StringUtility.stringHasValue(line)) {
                continue;
            }

            if (line.endsWith(";")) { //$NON-NLS-1$
                sb.append(' ');
                sb.append(line.substring(0, line.length() - 1));
                break;
            } else {
                sb.append(' ');
                sb.append(line);
            }
        }

        String s = sb.toString().trim();

        return s.length() > 0 ? s : null;
    }

    private BufferedReader getScriptReader() throws Exception, IOException {
        BufferedReader answer;
        
        if (sourceFile.startsWith("classpath:")) {
            String resource = sourceFile.substring("classpath:".length());
            URL url = ObjectFactory.getResource(resource);
            InputStream is = url.openStream();
            answer = new BufferedReader(new InputStreamReader(is));
        } else {
            File file = new File(sourceFile);
            if (!file.exists()) {
                throw new Exception("SQL script file does not exist");
            }
            answer = new BufferedReader(new FileReader(file));
        }
        
        return answer;
    }
}
