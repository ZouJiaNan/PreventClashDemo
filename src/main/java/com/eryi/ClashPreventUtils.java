package com.eryi;

import org.springframework.boot.loader.jar.JarFile;

import java.io.File;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;


/**
 * @description:依赖冲突检测工具类
 * @author: ZouJiaNan
 * @date: 2022/1/24 15:29
 */

public class ClashPreventUtils {
    static class GavInfo {
        private String groupId;
        private String artifactId;
        private String version;

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public void setArtifactId(String artifactId) {
            this.artifactId = artifactId;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        @Override
        public String toString() {
            return "GavInfo:{" +
                    "groupId='" + groupId + '\'' +
                    ", artifactId='" + artifactId + '\'' +
                    ", version='" + version + '\'' +
                    '}';
        }
    }
    //当前jar依赖的jar
    private static Set<JarEntry> parentdepJars=new HashSet<>();
    //jar包的GAV坐标信息
    private static Map<String,GavInfo> gavInfos=new HashMap<>();
    //冲突jar包列表
    private static List<String> conflictJar=new ArrayList<>();
    public static void check() throws Exception{
            //1.获取当前打包出的jar包
            String parentJarPath=ClashPreventUtils.class.getClassLoader().getResource("").getPath().split("!")[0];
            if(parentJarPath.startsWith("file:/")){
                parentJarPath=parentJarPath.replaceAll("file:/","");
            }
        String conflictJarList= null;
        try {
            JarFile jar=new JarFile(new File(parentJarPath));
            //2.遍历当前jar包中所有资源，获得依赖的jar包列表
            Enumeration<JarEntry> parentEntries=jar.entries();
            while(parentEntries.hasMoreElements()){
                JarEntry jarEntryTemp1=parentEntries.nextElement();
                if(jarEntryTemp1.getName().endsWith(".jar")){
                    if(parentdepJars.contains(jarEntryTemp1.getName())){
                        System.out.println("warn: jar is exist:" + jarEntryTemp1.getName());
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
                String key=groupId+"-"+artifactId;
                System.out.println("debug:key-----"+key);
                if(key!=null&&"-".equals(key)&&gavInfos.containsKey(key)){
                    conflictJar.add(innerJar.getName());
                    //System.out.println("warn: Jar ["+innerJar.getName()+"] package conflict ! please check:");
                }
                gavInfos.put(groupId+"-"+artifactId,gavInfo);
            }
            //抛出冲突列表信息
            conflictJarList = "jar conflici please check:\n";
            for (String conflictJar:conflictJar){
                conflictJarList+=conflictJar+"\n";
            }
        } catch (NoClassDefFoundError e) {
            throw new RuntimeException("The function of preventing dependency conflict is closed!");
        }
        throw new RuntimeException(conflictJarList);
    }

}
