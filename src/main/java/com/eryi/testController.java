package com.eryi;

import org.springframework.boot.loader.jar.JarFile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;


/**
 * @description:
 * @author: ZouJiaNan
 * @date: 2022/1/24 15:29
 */
@RestController
public class testController {

    private Set<JarEntry> parentdepJars=new HashSet<>();
    private Map<String,GavInfo> gavInfos=new HashMap<>();

    @RequestMapping("/test")
    public void test() throws Exception{
        try {
            //1.获取当前打包出的jar包
            String parentJarPath=this.getClass().getClassLoader().getResource("").getPath().split("!")[0];
            if(parentJarPath.startsWith("file:/")){
                parentJarPath=parentJarPath.replaceAll("file:/","");
            }
            JarFile jar=new JarFile(new File(parentJarPath));
            //2.遍历当前jar包中所有资源，获得依赖的jar包列表
            Enumeration<JarEntry> parentEntries=jar.entries();
            while(parentEntries.hasMoreElements()){
                JarEntry jarEntryTemp1=parentEntries.nextElement();
                if(jarEntryTemp1.getName().endsWith(".jar")){
                    if(parentdepJars.contains(jarEntryTemp1.getName())){
                        System.out.println("jar is exist:" + jarEntryTemp1.getName());
                        continue;
                    }
                    parentdepJars.add(jarEntryTemp1);
                }
            }

            //3.遍历当前jar包的jar依赖列表,获取每个依赖的GAV坐标
            for (JarEntry jarEntry:parentdepJars){
                JarFile innerJar=jar.getNestedJarFile(jarEntry);
                GavInfo gavInfo=new GavInfo();
                Attributes attributes=innerJar.getManifest().getMainAttributes();
                Set set=attributes.keySet();
                Object groupIdKey=null;
                Object artifactIdKey=null;
                Object versionKey=null;
                for (Object o:set){
                    if("Implementation-Vendor-Id".equals(o.toString())){
                        groupIdKey=o;
                    }
                    if("Implementation-Title".equals(o.toString())){
                        artifactIdKey=o;
                    }
                    if("Implementation-Version".equals(o.toString())){
                        versionKey=o;
                    }
                }
                Object groupId=attributes.get(groupIdKey);
                Object artifactId=attributes.get(artifactIdKey);
                Object version=attributes.get(versionKey);
                if(groupId!=null) {
                    gavInfo.setGroupId(groupId.toString());
                }
                if(artifactId!=null){
                    gavInfo.setArtifactId(artifactId.toString());
                }
                if(version!=null){
                    gavInfo.setVersion(version.toString());
                }
                gavInfos.put(groupId+"-"+artifactId,gavInfo);
            }
//            Set<String> keys=gavInfos.keySet();
//            for (String key:keys) {
//                System.out.println(gavInfos.get(key));
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
