package org.wso2.internalapps.pqd.functionalcoverage;

import com.esotericsoftware.yamlbeans.YamlReader;

import java.io.FileReader;
import java.io.IOException;

public class ReaderYaml {
    public Config readYaml() throws IOException{

        YamlReader reader = new YamlReader(new FileReader("config/ConfigFile.yml"));

        Config remoteRepoConfigurations= reader.read(Config.class);

        return remoteRepoConfigurations;
    }

}